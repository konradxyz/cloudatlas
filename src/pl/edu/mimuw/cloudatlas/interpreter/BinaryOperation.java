package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;

public abstract class BinaryOperation {

	public abstract Value perform(Value v1, Value v2);

	// Should return null if such operation is not supported.
	public abstract Type getResultType(Type left, Type right);
	
	protected abstract String getName();
	protected  InvalidTypeOperationException generateException(Type left, Type right) {
		return new InvalidTypeOperationException(getName(), left, right);	
	}
	
	protected Type throwIfNull(Type result, Type left, Type right) {
		if ( result == null ) {
			throw generateException(left, right);
		}
		return result;
	}

	private static void insertOperationType(
			Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> map,
			TypePrimitive left, TypePrimitive right, TypePrimitive result) {
		if (!map.containsKey(left)) {
			map.put(left, new HashMap<TypePrimitive, TypePrimitive>());
		}
		map.get(left).put(right, result);
	}

	private static Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> getSupportedPrimitivesMap(
			TypePrimitive[][] supportedPrimitives) {
		Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> result =
				new HashMap<TypePrimitive, Map<TypePrimitive, TypePrimitive>>();
		for (TypePrimitive[] operation : supportedPrimitives) {
			assert (operation.length == 3);
			insertOperationType(result, operation[0], operation[1],
					operation[2]);
		}
		return result;
	}

	private static Type getResultType(
			Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> supportedPrimitives,
			Type left, Type right) {

		// If one of types is untyped null it might be possible that we will
		// have multiple types that could be used in place of this null.
		// Consider 2 * x, where x is untyped NULL.
		// x might be either INTEGER or DURATION.
		// Following function chooses somehow arbitrary proper result type.
		// Global, untyped NULL is terrible idea but it is necessary.
		if ( left.equals(TypePrimitive.NULL)) {
			if ( right.equals(TypePrimitive.NULL))
				return TypePrimitive.NULL;
			List<Type> possibleTypes = new ArrayList<Type>();
			for ( Entry<TypePrimitive, Map<TypePrimitive, TypePrimitive>> entry : supportedPrimitives.entrySet() ) {
				if ( entry.getValue().containsKey(right))
					possibleTypes.add(entry.getValue().get(right));
			}
			if ( possibleTypes.contains(right))
				return right;
			if ( possibleTypes.size() == 0 )
				return null;
			return possibleTypes.get(0);
			
		}
		if ( right.equals(TypePrimitive.NULL)) {
			if ( supportedPrimitives.containsKey(left) ){
				Collection<TypePrimitive> possibleTypes = supportedPrimitives.get(left).values();
				if ( possibleTypes.contains(left) )
					return left;
				if ( possibleTypes.size() == 0 )
					return null;
				return possibleTypes.iterator().next();
			}
			return null;
		}
		if (supportedPrimitives.containsKey(left)
				&& supportedPrimitives.get(left).containsKey(right)) {
			return supportedPrimitives.get(left).get(right);
		}
		return null;
	}

	public static final BinaryOperation ADD_VALUE = new BinaryOperation() {
		private final TypePrimitive[][] supportedPrimitives = {
				{ TypePrimitive.INTEGER, TypePrimitive.INTEGER,
						TypePrimitive.INTEGER },
				{ TypePrimitive.DOUBLE, TypePrimitive.DOUBLE,
						TypePrimitive.DOUBLE },
				{ TypePrimitive.DURATION, TypePrimitive.DURATION,
						TypePrimitive.DURATION },
				{ TypePrimitive.STRING, TypePrimitive.STRING,
						TypePrimitive.STRING },
				{ TypePrimitive.TIME, TypePrimitive.DURATION,
						TypePrimitive.TIME },
				{ TypePrimitive.DURATION, TypePrimitive.TIME,
						TypePrimitive.TIME } };
		private final Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> supportedPrimitivesMap = 
				getSupportedPrimitivesMap(supportedPrimitives);

		@Override
		public Value perform(Value v1, Value v2) {
			return v1.addValue(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCollection() && left.isCompatible(right)) {
				return left;
			}
			return throwIfNull(BinaryOperation.getResultType(supportedPrimitivesMap, left,
					right), left, right);
		}

		@Override
		public String getName() {
			return "addition";
		}
	};

	public static final BinaryOperation IS_EQUAL = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.isEqual(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCompatible(right)) {
				return TypePrimitive.BOOLEAN;
			}
			throw generateException(left, right);
		}

		@Override
		public String getName() {
			return "equals";
		}
	};

	public static final BinaryOperation IS_LOWER_THAN = new BinaryOperation() {
		private final TypePrimitive[] supportedPrimitives = {
				TypePrimitive.INTEGER, TypePrimitive.DOUBLE,
				TypePrimitive.DURATION, TypePrimitive.TIME,
				TypePrimitive.STRING };

		Set<TypePrimitive> supportedPrimitivesSet = new HashSet<TypePrimitive>(
				Arrays.asList(supportedPrimitives));

		@Override
		public Value perform(Value v1, Value v2) {
			return v1.isLowerThan(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCompatible(right)
					&& supportedPrimitivesSet.contains(left)) {
				return TypePrimitive.BOOLEAN;
			}
			throw generateException(left, right);
		}

		@Override
		public String getName() {
			return "lower than";
		}
	};

	public static final BinaryOperation SUBTRACT = new BinaryOperation() {
		private final TypePrimitive[][] supportedPrimitives = {
				{ TypePrimitive.INTEGER, TypePrimitive.INTEGER,
						TypePrimitive.INTEGER },
				{ TypePrimitive.DOUBLE, TypePrimitive.DOUBLE,
						TypePrimitive.DOUBLE },
				{ TypePrimitive.DURATION, TypePrimitive.DURATION,
						TypePrimitive.DURATION },
				{ TypePrimitive.TIME, TypePrimitive.DURATION,
						TypePrimitive.TIME },
				{ TypePrimitive.TIME, TypePrimitive.TIME,
						TypePrimitive.DURATION } };
		private final Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> supportedPrimitivesMap = 
				getSupportedPrimitivesMap(supportedPrimitives);

		@Override
		public Value perform(Value v1, Value v2) {
			return v1.subtract(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			return throwIfNull( BinaryOperation.getResultType(supportedPrimitivesMap, left,
					right), left, right);
		}

		@Override
		public String getName() {
			return "subtraction";
		}
	};

	public static final BinaryOperation MULTIPLY = new BinaryOperation() {
		private final TypePrimitive[][] supportedPrimitives = {
				{ TypePrimitive.INTEGER, TypePrimitive.INTEGER,
						TypePrimitive.INTEGER },
				{ TypePrimitive.DOUBLE, TypePrimitive.DOUBLE,
						TypePrimitive.DOUBLE },
				{ TypePrimitive.DURATION, TypePrimitive.INTEGER,
						TypePrimitive.DURATION },
				{ TypePrimitive.DURATION, TypePrimitive.DOUBLE,
						TypePrimitive.DURATION },
				{ TypePrimitive.INTEGER, TypePrimitive.DURATION,
						TypePrimitive.DURATION },
				{ TypePrimitive.DOUBLE, TypePrimitive.DURATION,
						TypePrimitive.TIME } };
		private final Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> supportedPrimitivesMap = 
				getSupportedPrimitivesMap(supportedPrimitives);

		@Override
		public Value perform(Value v1, Value v2) {
			return v1.multiply(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			return throwIfNull(BinaryOperation.getResultType(supportedPrimitivesMap, left,
					right), left, right);
		}

		@Override
		public String getName() {
			return "multiplication";
		}
	};

	public static final BinaryOperation DIVIDE = new BinaryOperation() {
		private final TypePrimitive[][] supportedPrimitives = {
				{ TypePrimitive.INTEGER, TypePrimitive.INTEGER,
						TypePrimitive.DOUBLE },
				{ TypePrimitive.DOUBLE, TypePrimitive.DOUBLE,
						TypePrimitive.DOUBLE },
				{ TypePrimitive.DURATION, TypePrimitive.INTEGER,
						TypePrimitive.DURATION },
				{ TypePrimitive.DURATION, TypePrimitive.DOUBLE,
						TypePrimitive.DURATION } };
		private final Map<TypePrimitive, Map<TypePrimitive, TypePrimitive>> supportedPrimitivesMap = 
				getSupportedPrimitivesMap(supportedPrimitives);

		@Override
		public Value perform(Value v1, Value v2) {
			return v1.divide(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			return throwIfNull(BinaryOperation.getResultType(supportedPrimitivesMap, left,
					right), left, right);
		}

		@Override
		public String getName() {
			return "division";
		}
	};

	public static final BinaryOperation MODULO = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.modulo(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCompatible(TypePrimitive.INTEGER)
					&& left.isCompatible(TypePrimitive.INTEGER)) {
				return TypePrimitive.INTEGER;
			}
			throw generateException(left, right);
		}

		@Override
		public String getName() {
			return "modulo";
		}
	};

	public static final BinaryOperation AND = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.and(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCompatible(TypePrimitive.BOOLEAN)
					&& left.isCompatible(TypePrimitive.BOOLEAN)) {
				return TypePrimitive.BOOLEAN;
			}
			throw generateException(left, right);
		}

		@Override
		public String getName() {
			return "and";
		}
	};

	public static final BinaryOperation OR = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.or(v2);
		}

		@Override
		public Type getResultType(Type left, Type right) {
			if (left.isCompatible(TypePrimitive.BOOLEAN)
					&& left.isCompatible(TypePrimitive.BOOLEAN)) {
				return TypePrimitive.BOOLEAN;
			}
			throw generateException(left, right);
		}

		@Override
		public String getName() {
			return "or";
		}
	};
}