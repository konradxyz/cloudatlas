package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.aggregation.AverageTest;
import test.aggregation.CountTest;


@RunWith(Suite.class)
@SuiteClasses({ Success.class, ZMITest.class, 
		SerializationAttributesMapTest.class, SerializationStringTest.class,
		SerializatorValueStringTest.class, ExpressionInterpreterTest.class, BinaryOperationsTypesTest.class,
		CountTest.class, AverageTest.class})
public class AllTests {

}
