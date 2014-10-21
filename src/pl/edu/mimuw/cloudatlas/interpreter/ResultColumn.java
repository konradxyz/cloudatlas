package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

public class ResultColumn extends Result {
	private final ValueList list;
	
	public ResultColumn(ValueList list) {
		this.list = list;
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultSingle right) {
		List<Value> result = new ArrayList<Value>();
		for ( Value v : list ) {
			result.add(operation.perform(v, right.getValue()));
		}
		Type type = TypeCollection.computeElementType(result);
		// Not sure whether it is correct place to catch NULL.
		if ( right.getValue().isNull() ) 
			return new ResultSingle(ValueNull.getInstance());
		return new ResultColumn(new ValueList(result, type));
	}

	@Override
	public Result unaryOperation(UnaryOperation operation) {
		return new ResultSingle(operation.perform(list));
	}

	@Override
	protected Result callMe(BinaryOperation operation, Result left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value getValue() {
		return list;
	}

	@Override
	public ValueList getList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueList getColumn() {
		return list;
	}

	@Override
	public Result filterNulls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result first(int size) {
		List<Value> result = Collections.unmodifiableList(list.subList(0,
				Math.min(size, list.size())));
		Type elementType = TypeCollection.computeElementType(result);
		return new ResultColumn(new ValueList(result, elementType));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSingle isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
