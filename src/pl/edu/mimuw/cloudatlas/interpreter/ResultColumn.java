package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

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
		if (right.list.size() != this.list.size())
			throw new UnsupportedOperationException(
					"Binary operation on columns of different sizes");
		List<Value> result = new ArrayList<Value>();
		for (int i = 0; i < this.list.size(); ++i) {
			result.add(operation.perform(this.list.get(i), right.list.get(i)));
		}
		Type type = TypeCollection.computeElementType(result);
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
		return list;
	}

	@Override
	public ValueList getValues() {
		return list;
	}

	public ValueList getColumn() {
		return list;
	}

	@Override
	public Result convertTo(Type to) {
		return new ResultColumn(convertTo(list, to));
	}

	@Override
	public ResultSingle isNull() {
		return new ResultSingle(new ValueBoolean(false));
	}

	@Override
	public Type getType() {
		return list.getElementType();
	}

}
