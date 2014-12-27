package pl.edu.mimuw.cloudatlas.common.interpreter;

import pl.edu.mimuw.cloudatlas.common.model.Type;

@SuppressWarnings("serial")
public class InvalidTypeOperationException extends InterpreterException {

	protected InvalidTypeOperationException(String operationName, Type left, Type right) {
		super("Operation '" + operationName + "' is not supported for types " + left.toString() + " and " + right.toString());
	}
	
	protected InvalidTypeOperationException(String operationName, Type type) {
		super("Operation '" + operationName + "' is not supported for type " + type.toString());
	}

}
