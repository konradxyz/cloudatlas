package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;

// It stores timestampMsec data.
public class ZmisFreshnessInitCommunicate extends ZmisFreshnessCommunicate {

	public ZmisFreshnessInitCommunicate(ZmisFreshness content) {
		super(content);
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
