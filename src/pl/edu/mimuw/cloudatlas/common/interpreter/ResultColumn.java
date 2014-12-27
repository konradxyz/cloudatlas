package pl.edu.mimuw.cloudatlas.common.interpreter;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.Type;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;

public class ResultColumn extends Result {
	private final ValueList list;

	public ResultColumn(ValueList list) {
		assert(list != null);
		this.list = list;
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultSingle right) {
		return new ResultColumn(binaryOperationTypedValueList(list, operation,
				right));
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultColumn right) {
		Type type = operation.getResultType(this.list.getElementType(), right.getValues().getElementType());
		if (right.list.size() != this.list.size())
			throw new UnsupportedOperationException(
					"Binary operation on columns of different sizes");
		List<Value> result = new ArrayList<Value>();
		for (int i = 0; i < this.list.size(); ++i) {
			result.add(operation.perform(this.list.get(i), right.list.get(i)));
		}
		return new ResultColumn(new ValueList(result, type));

	}

	@Override
	public Result binaryOperationTyped(BinaryOperation operation,
			ResultList resultList) {
		throw new InternalInterpreterException(
				"binary operation type not supported types");
	}

	@Override
	public Result unaryOperation(UnaryOperation operation) {
		return new ResultColumn(unaryOperation(list, operation));
	}

	@Override
	protected Result callMe(BinaryOperation operation, Result left) {
		return left.binaryOperationTyped(operation, this);
	}

	@Override
	public Value getValue() {
		throw new InternalInterpreterException("OneResult expected, ColumnResult given");
	}

	@Override
	public ValueList getValues() {
		return list;
	}

	@Override
	public Type getType() {
		return list.getElementType();
	}

}
