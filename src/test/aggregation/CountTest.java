package test.aggregation;

import pl.edu.mimuw.cloudatlas.agent.interpreter.Functions;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;

public class CountTest extends AggregationTest {

	public CountTest() {
		super(Functions.COUNT);
	}

	@Override
	public void fillTests() {
		prepareTest(new Value[] {}, TypePrimitive.INTEGER, new ValueInt(0l));

		prepareTest(new Value[] { ValueNull.getInstance(),
				new ValueString("a"), new ValueString("b"),
				new ValueString(null) }, TypePrimitive.STRING, new ValueInt(2l));

	}

}
