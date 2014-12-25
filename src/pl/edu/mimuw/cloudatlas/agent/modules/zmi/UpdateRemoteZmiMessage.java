package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class UpdateRemoteZmiMessage extends Message {
	private final PathName path;
	private final AttributesMap attributes;

	public UpdateRemoteZmiMessage(PathName path, AttributesMap attributes) {
		super();
		this.path = path;
		this.attributes = attributes;
	}

	public PathName getPath() {
		return path;
	}

	public AttributesMap getAttributes() {
		return attributes;
	}
}
