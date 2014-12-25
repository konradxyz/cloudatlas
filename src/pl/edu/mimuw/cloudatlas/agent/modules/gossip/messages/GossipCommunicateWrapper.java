package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

public class GossipCommunicateWrapper {
	private GossipCommunicate communicate;

	public GossipCommunicateWrapper(GossipCommunicate communicate) {
		super();
		this.communicate = communicate;
	}

	public GossipCommunicate getCommunicate() {
		return communicate;
	}
}
