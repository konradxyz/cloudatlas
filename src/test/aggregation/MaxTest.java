package test.aggregation;

import static org.junit.Assert.fail;

import java.text.ParseException;

import pl.edu.mimuw.cloudatlas.interpreter.Functions;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class MaxTest extends AggregationTest {

	public MaxTest() {
		super(Functions.MAX);
	}

	@Override
	public void fillTests() {
		prepareTest(new Value[] {}, TypePrimitive.NULL, ValueNull.getInstance());

		prepareTest(new Value[] {}, TypePrimitive.INTEGER,
				TypePrimitive.INTEGER.getNull());
		prepareTestFromStrings(new String[] { "1", "2", "-1", "18" },
				TypePrimitive.INTEGER, new ValueInt(18l));

		prepareTest(new Value[] {}, TypePrimitive.DOUBLE,
				TypePrimitive.DOUBLE.getNull());
		prepareTestFromStrings(new String[] { "1.0", "2.0", "-1.0", "-1.0",
				"18.0" }, TypePrimitive.DOUBLE, new ValueDouble(18.0));

		prepareTest(new Value[] {}, TypePrimitive.STRING,
				TypePrimitive.STRING.getNull());
		prepareTestFromStrings(new String[] { "1.0", "2.0", "1.0", "1.0",
				"0000", "18.0" }, TypePrimitive.STRING, new ValueString("2.0"));

		prepareTest(new Value[] {}, TypePrimitive.DURATION,
				TypePrimitive.DURATION.getNull());
		prepareTestFromStrings(new String[] { "+0 12:05:10.120",
				"-1 10:05:10.120", "+0", "+0" }, TypePrimitive.DURATION,
				new ValueDuration("+0 12:05:10.120"));

		
		prepareTest(new Value[] {}, TypePrimitive.TIME,
				TypePrimitive.TIME.getNull());
		try {
			prepareTestFromStrings(new String[] { "2012/11/09 21:12:00.000",
					"2012/11/09 21:12:00.000", "2012/12/09 21:12:00.000",
					"2014/10/09 21:12:00.000" }, TypePrimitive.TIME,
					new ValueTime("2014/10/09 21:12:00.000"));
		} catch (ParseException e) {
			fail("Could not parse time");
			e.printStackTrace();
		}

	}

}
