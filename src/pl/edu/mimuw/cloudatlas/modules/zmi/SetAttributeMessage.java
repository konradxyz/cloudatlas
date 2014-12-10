package pl.edu.mimuw.cloudatlas.modules.zmi;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;

public class SetAttributeMessage extends Message {
	private Attribute attribute;
	private Value value;

	public Attribute getAttribute() {
		return attribute;
	}

	public Value getValue() {
		return value;
	}

	public SetAttributeMessage(Attribute attribute, Value value) {
		this.attribute = attribute;
		this.value = value;
	}
}
