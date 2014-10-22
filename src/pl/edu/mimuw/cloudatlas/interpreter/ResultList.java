package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueList;

public class ResultList extends Result {
	
	private final ValueList list;
	
	public ResultList(ValueList list) {
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
		return list;
	}
	
	@Override
	public ValueList getValues() {
		return list;
	}


	public ValueList getList() {
		return list;
	}

	@Override
	public Result filterNulls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result first(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result last(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result random(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result convertTo(Type to) {
		return new ResultList(convertTo(list, to));
	}

	@Override
	public ResultSingle isNull() {
		return new ResultSingle(new ValueBoolean(false));
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}



}
