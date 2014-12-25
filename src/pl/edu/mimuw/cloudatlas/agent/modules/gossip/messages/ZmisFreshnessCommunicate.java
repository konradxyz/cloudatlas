package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;

// It stores timestampMsec data.
public abstract class ZmisFreshnessCommunicate extends GossipCommunicate {
	private ZmisFreshness content;

	public ZmisFreshnessCommunicate(ZmisFreshness content) {
		this.content = content;
	}

	public ZmisFreshness getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return content.toString();
	}
}
