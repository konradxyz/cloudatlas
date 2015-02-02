package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class FallbackCommunicate extends WithCertificateCommunicate {
	
	private final PathName pathName;
	
	public FallbackCommunicate(Certificate certificate, Integer gossipLevel, PathName pathName) {
		super(certificate, gossipLevel);
		this.pathName = pathName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		FallbackCommunicate other = (FallbackCommunicate) obj;
		if (pathName == null) {
			if (other.pathName != null)
				return false;
		} else if (!pathName.equals(other.pathName))
			return false;
		return true;
	}

	@Override
	public Type getType() {
		return Type.FALLBACK;
	}
	
	public PathName getPathName() {
		return pathName;
	}

}
