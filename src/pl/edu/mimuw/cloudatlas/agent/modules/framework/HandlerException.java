package pl.edu.mimuw.cloudatlas.agent.modules.framework;

@SuppressWarnings("serial")
public class HandlerException extends Exception {
	public HandlerException(Exception cause) {
		super(cause);
	}

	public HandlerException(String string) {
		super(string);
	}
}
