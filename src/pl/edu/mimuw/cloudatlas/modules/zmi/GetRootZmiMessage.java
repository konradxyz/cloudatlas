package pl.edu.mimuw.cloudatlas.modules.zmi;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;

public class GetRootZmiMessage extends Message {
	private Address responseTarget;
	private Integer responseMessageType;

	public Address getResponseTarget() {
		return responseTarget;
	}
	public Integer getResponseMessageType() {
		return responseMessageType;
	}

	public GetRootZmiMessage(Address responseTarget, Integer responseMessageType) {
		super();
		this.responseTarget = responseTarget;
		this.responseMessageType = responseMessageType;
	}
	
}
