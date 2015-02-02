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
	public Type getType() {
		return Type.FALLBACK;
	}
	
	public PathName getPathName() {
		return pathName;
	}

}
