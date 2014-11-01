package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueString;

public abstract class UnaryOperation {

	public abstract Value perform(Value v);
	public abstract Type getResultTypeOrNull(Type type);
	public abstract String getName();
	
	public Type getResultType(Type type) {
		Type result = getResultTypeOrNull(type);
		if ( result == null )
			throw new InvalidTypeOperationException(getName(), type);
		return result;
	}
	
	public static final UnaryOperation NOT = new UnaryOperation() {
		@Override
		public Value perform(Value v) {
			return v.negate();
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.BOOLEAN))
				return TypePrimitive.BOOLEAN;
			return null;
		}

		@Override
		public String getName() {
			return "not";
		}
	};


	// Again, a bit awkward piece of code.
	// If input type is untyped null we choose to return int typed null.
	// We could choose untyped NULL as result but it would allow expressions
	// like (-x + "something") as long x is null. We would like to avoid it.
	public static final UnaryOperation NEGATE = new UnaryOperation() {
		@Override
		public Value perform(Value v) {
			return v.negate();
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.INTEGER))
				return TypePrimitive.INTEGER;
			if ( type.isCompatible(TypePrimitive.DOUBLE))
				return TypePrimitive.DOUBLE;
			return null;
		}

		@Override
		public String getName() {
			return "negation";
		}
	};

	public static final UnaryOperation VALUE_SIZE = new UnaryOperation() {
		
		@Override
		public Value perform(Value v) {
			return v.valueSize();
		}
		
		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.equals(TypePrimitive.NULL) || type.equals(TypePrimitive.STRING) || type.isCollection() )
				return TypePrimitive.INTEGER;
			return null;
		}
		

		@Override
		public String getName() {
			return "size";
		}

	};

	public static final UnaryOperation ROUND = new UnaryOperation() {
		@Override
		public Value perform(Value v) {
			if (v.getType().isCompatible(TypePrimitive.DOUBLE)) {
				if (v.isNull())
					return new ValueDouble(null);
				return new ValueDouble((double) Math.round(((ValueDouble) v)
						.getValue()));
			}
			throw new IllegalArgumentException("Value must have type "
					+ TypePrimitive.DOUBLE + ".");
		}
		

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.DOUBLE))
				return TypePrimitive.DOUBLE;
			return null;
		}

		@Override
		public String getName() {
			return "round";
		}

	};

	public static final UnaryOperation FLOOR = new UnaryOperation() {
		@Override
		public Value perform(Value v) {
			if (v.getType().isCompatible(TypePrimitive.DOUBLE)) {
				if (v.isNull())
					return new ValueDouble(null);
				return new ValueDouble((double) Math.floor(((ValueDouble) v)
						.getValue()));
			}
			throw new IllegalArgumentException("Value must have type "
					+ TypePrimitive.DOUBLE + ".");
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.DOUBLE))
				return TypePrimitive.DOUBLE;
			return null;
		}
		

		@Override
		public String getName() {
			return "floor";
		}

	};

	public static final UnaryOperation CEIL = new UnaryOperation() {
		@Override
		public Value perform(Value v) {
			if (v.getType().isCompatible(TypePrimitive.DOUBLE)) {
				if (v.isNull())
					return new ValueDouble(null);
				return new ValueDouble((double) Math.ceil(((ValueDouble) v)
						.getValue()));
			}
			throw new IllegalArgumentException("Value must have type "
					+ TypePrimitive.DOUBLE + ".");
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.DOUBLE))
				return TypePrimitive.DOUBLE;
			return null;
		}
		

		@Override
		public String getName() {
			return "ceil";
		}

	};

	public static class RegexpOperation extends UnaryOperation {
		private String regexp;

		public RegexpOperation(String regexp) {
			this.regexp = regexp;
		}

		@Override
		public Value perform(Value v) {
			return v.regExpr(new ValueString(regexp));
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( type.isCompatible(TypePrimitive.STRING))
				return TypePrimitive.BOOLEAN;
			return null;
		}
		

		@Override
		public String getName() {
			return "regexp";
		}

	};
	
	public static final UnaryOperation IS_NULL = new UnaryOperation() {

		@Override
		public Value perform(Value v) {
			return new ValueBoolean(v.isNull());
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			return TypePrimitive.BOOLEAN;
		}

		@Override
		public String getName() {
			return "isNull";
		}
	};
	
	public static class ConversionOperation extends UnaryOperation {
		private Type target;
		 

		public ConversionOperation(Type target) {
			this.target = target;
		}

		@Override
		public Value perform(Value v) {
			return v.convertTo(target);
		}

		@Override
		public Type getResultTypeOrNull(Type type) {
			if ( target.equals(TypePrimitive.NULL) || type.equals(TypePrimitive.NULL))
				return target.equals(TypePrimitive.NULL) ? type : target;
			if ( type.equals(target))
				return target;
			// Anything can be converted to string.
			if( target.equals(TypePrimitive.STRING))
				return target;
			if( target.isCollection() ) {
				if ( type.isCollection() ) {
					TypeCollection targetC = (TypeCollection) target;
					TypeCollection typeC = (TypeCollection) type;
					return typeC.getElementType().isCompatible(targetC.getElementType()) ? target : null;	
				} else
					return null;
			}
			
			// String can be converted to almost anything.
			if (type.equals(TypePrimitive.STRING) && !target.isCollection()
					&& !target.equals(TypePrimitive.CONTACT))
				return target;
			
			if (type.equals(TypePrimitive.INTEGER) && (target.equals(TypePrimitive.DOUBLE) || target.equals(TypePrimitive.DURATION))) {
				return target;
			}
			if ( (type.equals(TypePrimitive.DOUBLE) || type.equals(TypePrimitive.DURATION) && target.equals(TypePrimitive.INTEGER))) {
				return target;
			}
				
			return null;
		}

		@Override
		public String getName() {
			return "conversion to " + target.toString();
		}

	};
}