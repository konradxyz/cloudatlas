package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Value;

public interface BinaryOperation {

	public abstract Value perform(Value v1, Value v2);
	
	public static final BinaryOperation ADD_VALUE = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.addValue(v2);
		}
	};
	public static final BinaryOperation IS_EQUAL = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.isEqual(v2);
		}
	};

	public static final BinaryOperation IS_LOWER_THAN = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.isLowerThan(v2);
		}
	};


	public static final BinaryOperation SUBTRACT = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.subtract(v2);
		}
	};

	public static final BinaryOperation MULTIPLY = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.multiply(v2);
		}
	};

	public static final BinaryOperation DIVIDE = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.divide(v2);
		}
	};

	public static final BinaryOperation MODULO = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.modulo(v2);
		}
	};

	public static final BinaryOperation AND = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.and(v2);
		}
	};

	public static final BinaryOperation OR = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.or(v2);
		}
	};

	public static final BinaryOperation REG_EXPR = new BinaryOperation() {
		@Override
		public Value perform(Value v1, Value v2) {
			return v1.regExpr(v2);
		}
	};


}