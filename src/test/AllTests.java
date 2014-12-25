package test;

import junit.framework.AssertionFailedError;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import pl.edu.mimuw.cloudatlas.common.model.Value;
import test.aggregation.AndTest;
import test.aggregation.AverageTest;
import test.aggregation.CountTest;
import test.aggregation.MaxTest;
import test.aggregation.MinTest;
import test.aggregation.OrTest;
import test.aggregation.SumTest;


@RunWith(Suite.class)
@SuiteClasses({ Success.class, ZMITest.class,
		SerializationAttributesMapTest.class, SerializationStringTest.class,
		SerializatorValueStringTest.class, ExpressionInterpreterTest.class,
		BinaryOperationsTypesTest.class, CountTest.class, AverageTest.class,
		SumTest.class, AndTest.class, OrTest.class, MinTest.class,
		MaxTest.class, AdditionTest.class, CommunicateSerializerTest.class })
public class AllTests {
	public static void assertIdentical(Value expected, Value got) {
		if ( !expected.identical(got) ) {
			throw new AssertionFailedError(String.format(
					"Assert identical failed: expected %s: %s, got %s : %s",
					expected, expected.getType(), got, got.getType()));
		}
	}

}
