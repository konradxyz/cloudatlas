package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.interpreter.BinaryOperation;
import pl.edu.mimuw.cloudatlas.interpreter.Result;
import pl.edu.mimuw.cloudatlas.interpreter.ResultSingle;
import pl.edu.mimuw.cloudatlas.model.Type.PrimaryType;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueSet;

public class AdditionTest {

	@Test
	public void test() {
		ValueSet setOfNull = new ValueSet(new HashSet<Value>(Arrays.asList(ValueNull
				.getInstance())), TypePrimitive.NULL);
		
		ValueSet setOfInt = new ValueSet(new HashSet<Value>(Arrays.asList(new ValueInt(0l))), TypePrimitive.INTEGER);
		
		ValueSet setOfSetOfInt = new ValueSet(new HashSet<Value>(
				Arrays.asList(setOfInt)), new TypeCollection(PrimaryType.SET,
				TypePrimitive.INTEGER));

		Result left = new ResultSingle(setOfNull);
		Result right = new ResultSingle(setOfSetOfInt);
		
		Result resL = left.binaryOperation(BinaryOperation.ADD_VALUE, right);
		Result resR = right.binaryOperation(BinaryOperation.ADD_VALUE, left);
		assertEquals(resL.getValue(), resR.getValue());		
	}
}
