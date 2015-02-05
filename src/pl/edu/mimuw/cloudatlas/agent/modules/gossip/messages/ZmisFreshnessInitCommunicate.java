package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;
import pl.edu.mimuw.cloudatlas.common.Certificate;

// It stores timestampMsec data.
public class ZmisFreshnessInitCommunicate extends ZmisFreshnessCommunicate {

	public ZmisFreshnessInitCommunicate(ZmisFreshness content, TravelTime time, Certificate certificate, Integer gossipLevel) {
		super(content, time, certificate, gossipLevel);
	}

	@Override
	public Type getType() {
		return Type.ZMIS_FRESHNESS_INIT;
	}

	@Override
	public String toString() {
		return "Init: " + super.toString();
	}
}
