package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;

public class ResultList extends Result {
	
	private final ValueList list;
	
	public ResultList(ValueList list) {
		assert(list != null);
		this.list = list;
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultSingle right) {
		return new ResultList( binaryOperationTypedValueList(list, operation, right));
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultColumn right) {
		throw new InternalInterpreterException("binary operation type not supported types");
	}
	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultList right) {
		throw new InternalInterpreterException("binary operation type not supported types");
	}

	

	@Override
	public Result unaryOperation(UnaryOperation operation) {
		return new ResultList(unaryOperation(list, operation));
	}

	@Override
	protected Result callMe(BinaryOperation operation, Result left) {
		return left.binaryOperationTyped(operation, this);
	}

	@Override
	public Value getValue() {
		throw new InternalInterpreterException("OneResult expected, ListResult given");
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
