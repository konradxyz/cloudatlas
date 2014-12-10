package test.aggregation;

import pl.edu.mimuw.cloudatlas.agent.interpreter.Functions;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueBoolean;

public class OrTest extends AggregationTest {

	public OrTest() {
		super(Functions.OR);
	}

	@Override
	public void fillTests() {
		Value t = new ValueBoolean(true);
		Value f = new ValueBoolean(false);
		Value n = new ValueBoolean(null);
		prepareTest(new Value[]{}, TypePrimitive.NULL, n);
		
		prepareTest(new Value[]{t,t,t}, TypePrimitive.BOOLEAN, t);
		prepareTest(new Value[]{f,t,t}, TypePrimitive.BOOLEAN, t);
		prepareTest(new Value[]{f,f,f}, TypePrimitive.BOOLEAN, f);
		
		prepareTest(new Value[]{n,t,t}, TypePrimitive.BOOLEAN, t);
	}
}
