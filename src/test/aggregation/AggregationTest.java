package test.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import pl.edu.mimuw.cloudatlas.common.interpreter.ResultColumn;
import pl.edu.mimuw.cloudatlas.common.interpreter.ResultList;
import pl.edu.mimuw.cloudatlas.common.interpreter.Result.AggregationOperation;
import pl.edu.mimuw.cloudatlas.common.model.Type;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;

public abstract class AggregationTest {
	private List<ValueList> inputs = new ArrayList<ValueList>();
	private List<Value> outputs = new ArrayList<Value>();
	private AggregationOperation operation;
	
	public AggregationTest(AggregationOperation operation) {
		this.operation = operation;
	}
	
	public abstract void fillTests();
	
	public void prepareTestFromStrings(String[] values, Type type, Value output) {
		Value[] vvalues =  new Value[values.length];
		int i = 0;
		for ( String str: values ) {
			System.err.println(str);
			Value converted = new ValueString(str).convertTo(type);
			assertEquals(converted.isNull(), false);
			vvalues[i++] = converted;
		}
		prepareTest(vvalues, type, output);
	}
	
	
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
	
	public void assertIdentical(Value expected, Value got) {
		if ( !expected.identical(got) ) {
			throw new AssertionFailedError(String.format(
					"Assert identical failed: expected %s, got %s", expected, got));
		}
	}
	
	@Test
	public void test() {
		fillTests();
		for ( int i =0; i < inputs.size(); ++i ) {
			System.err.println(inputs.get(i));
			ResultColumn column = new ResultColumn(inputs.get(i));
			Value result = column.aggregationOperation(operation).getValue();
			assertIdentical(outputs.get(i), result);
			
			ResultList list = new ResultList(inputs.get(i));
			Value res = list.aggregationOperation(operation).getValue();
			assertIdentical(outputs.get(i), res);
		}
	}
}
