package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;
import pl.edu.mimuw.cloudatlas.common.Certificate;

// It stores timestampMsec data.
public abstract class ZmisFreshnessCommunicate extends WithCertificateCommunicate {
	private final ZmisFreshness content;
	private final TravelTime time;

	public ZmisFreshnessCommunicate(ZmisFreshness content, TravelTime time, Certificate certificate, Integer gossipLevel) {
		super(certificate, gossipLevel);
		this.content = content;
		this.time = time;
	}

	public ZmisFreshness getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return content.toString();
	}

	public TravelTime getTime() {
		return time;
	}
}
