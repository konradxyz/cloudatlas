package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Type;

@SuppressWarnings("serial")
public class InvalidBinaryOperationException extends InterpreterException {

	protected InvalidBinaryOperationException(String operationName, Type left, Type right) {
		super("Operation '" + operationName + "' is not supported for types " + left.toString() + " and " + right.toString());
	}

}
