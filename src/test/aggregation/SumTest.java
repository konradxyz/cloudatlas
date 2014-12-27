package test.aggregation;

import pl.edu.mimuw.cloudatlas.common.interpreter.Functions;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.common.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;

public class SumTest extends AggregationTest {

	public SumTest() {
		super(Functions.SUM);
	}

	@Override
	public void fillTests() {
		prepareTest(new Value[]{}, TypePrimitive.NULL, ValueNull.getInstance());
		
		prepareTest(new Value[]{}, TypePrimitive.INTEGER, TypePrimitive.INTEGER.getNull());
		prepareTest(new Value[]{}, TypePrimitive.DOUBLE, TypePrimitive.DOUBLE.getNull());
		prepareTest(new Value[]{}, TypePrimitive.DURATION, TypePrimitive.DURATION.getNull());
		
		
		prepareTestFromStrings(new String[] { "-1", "6", "0", "13" },
				TypePrimitive.INTEGER, new ValueInt(18l));

		prepareTestFromStrings(new String[] { "-1.0", "6.0", "0.0", "13.0" },
				TypePrimitive.DOUBLE, new ValueDouble(18.0));
		
		prepareTestFromStrings(new String[] { "+0 12:05:10.120",
				"-1 10:05:10.120", "+0", "+0" }, TypePrimitive.DURATION,
				new ValueDuration("-0 22:00:00.000"));

	}

}
