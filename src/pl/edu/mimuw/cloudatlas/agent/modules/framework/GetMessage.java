package pl.edu.mimuw.cloudatlas.agent.modules.framework;

// This message contains only information necessary to send information to another module.
// It will be used as common getter message.
public class GetMessage extends Message {
	private Address responseTarget;
	private Integer responseMessageType;

	public Address getResponseTarget() {
		return responseTarget;
	}
	public Integer getResponseMessageType() {
		return responseMessageType;
	}

	public GetMessage(Address responseTarget, Integer responseMessageType) {
		super();
		this.responseTarget = responseTarget;
		this.responseMessageType = responseMessageType;
	}
}
