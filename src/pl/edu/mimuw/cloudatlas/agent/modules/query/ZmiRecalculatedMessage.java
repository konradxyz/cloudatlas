package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class ZmiRecalculatedMessage extends Message {

	private PathName pathName;
	private AttributesMap map;

	public ZmiRecalculatedMessage(AttributesMap map, PathName pathName) {
		this.map = map;
		this.pathName = pathName;
	}

	public PathName getPathName() {
		return pathName;
	}

	public AttributesMap getMap() {
		return map;
	}

}
