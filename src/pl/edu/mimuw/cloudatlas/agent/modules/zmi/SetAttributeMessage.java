package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.Value;

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
