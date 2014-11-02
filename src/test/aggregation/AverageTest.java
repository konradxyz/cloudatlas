package test.aggregation;

import pl.edu.mimuw.cloudatlas.interpreter.Functions;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

public class AverageTest extends AggregationTest {

	public AverageTest() {
		super(Functions.AVERAGE);
	}

	@Override
	public void fillTests() {
		// Null list.
		prepareTest(new Value[]{ValueNull.getInstance()}, TypePrimitive.NULL, ValueNull.getInstance());
		
		
		prepareTestFromStrings(new String[] {"-1", "2", "4"}, TypePrimitive.INTEGER, new ValueDouble(5./3.));
		prepareTestFromStrings(new String[] {"-1.0", "2.0", "4.0"}, TypePrimitive.DOUBLE, new ValueDouble(5./3.));
		
		prepareTestFromStrings(new String[] {"+0 12:05:10.120", "-1 10:05:10.120", "+0", "+0"}, TypePrimitive.DURATION, 
				new ValueDuration("-0 05:30:00.000"));
		
		
		prepareTestFromStrings(new String[] {}, TypePrimitive.INTEGER, new ValueDouble(null));
		prepareTestFromStrings(new String[] {}, TypePrimitive.DOUBLE, new ValueDouble(null));
		
		prepareTestFromStrings(new String[] {}, TypePrimitive.DURATION, 
				new ValueDuration((Long) null));
		
	}

}
