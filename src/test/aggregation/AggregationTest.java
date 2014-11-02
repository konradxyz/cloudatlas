package test.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import pl.edu.mimuw.cloudatlas.interpreter.Result.AggregationOperation;
import pl.edu.mimuw.cloudatlas.interpreter.ResultColumn;
import pl.edu.mimuw.cloudatlas.interpreter.ResultList;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

public abstract class AggregationTest {
	private List<ValueList> inputs = new ArrayList<ValueList>();
	private List<Value> outputs = new ArrayList<Value>();
	private AggregationOperation operation;
	
	public AggregationTest(AggregationOperation operation) {
		this.operation = operation;
	}
	
	public abstract void fillTests();
	
	public void prepareTest(Value[] values, Type type, Value output) {
		ValueList in = new ValueList(type);
		in.add(ValueNull.getInstance());
		in.addAll(Arrays.asList(values));
		if ( !type.isCollection() ){
			TypePrimitive tp = (TypePrimitive) type;
			in.add(tp.getNull());
		}
		inputs.add(in);
		outputs.add(output);
	}
	@Test
	public void test() {
		fillTests();
		for ( int i =0; i < inputs.size(); ++i ) {
			System.err.println(inputs.get(i));
			ResultColumn column = new ResultColumn(inputs.get(i));
			Value result = column.aggregationOperation(operation).getValue();
			assertEquals(outputs.get(i), result);
			
			ResultList list = new ResultList(inputs.get(i));
			Value res = list.aggregationOperation(operation).getValue();
			assertEquals(outputs.get(i), res);
		}
	}
}
