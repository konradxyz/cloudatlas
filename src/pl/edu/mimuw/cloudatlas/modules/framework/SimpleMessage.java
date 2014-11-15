package pl.edu.mimuw.cloudatlas.modules.framework;

public class SimpleMessage<T> extends Message {
	private T content;

	public T getContent() {
		return content;
	}

	public SimpleMessage(T content) {
		super();
		this.content = content;
	}
	
	@Override
	public String toString() {
		return content.toString();
	}
}
