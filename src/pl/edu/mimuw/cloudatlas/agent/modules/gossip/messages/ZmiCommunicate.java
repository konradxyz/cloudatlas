package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class ZmiCommunicate extends WithCertificateCommunicate {
	private PathName pathName;
	private AttributesMap attributes;

	@Override
	public Type getType() {
		return Type.ZMI;
	}

	public ZmiCommunicate(PathName pathName, AttributesMap attributes, Certificate certificate, Integer gossipLevel) {
		super(certificate, gossipLevel);
		this.pathName = pathName;
		this.attributes = attributes;
	}

	public PathName getPathName() {
		return pathName;
	}

	public AttributesMap getAttributes() {
		return attributes;
	}
	
	@Override
	public String toString() {
		return "zmi: " + pathName.toString() + ": " + attributes.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((pathName == null) ? 0 : pathName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZmiCommunicate other = (ZmiCommunicate) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (pathName == null) {
			if (other.pathName != null)
				return false;
		} else if (!pathName.equals(other.pathName))
			return false;
		return true;
	}

}
