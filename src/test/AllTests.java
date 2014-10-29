package test;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cloudatlas.ByteSerializatorTest;

@RunWith(Suite.class)
@SuiteClasses({ Success.class, ZMITest.class, ByteSerializatorTest.class,
		SerializationAttributesMapTest.class, SerializationStringTest.class,
		SerializatorValueStringTest.class, ExpressionInterpreterTest.class, BinaryOperationsTypesTest.class })
public class AllTests {

}
