package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

public abstract class GossipCommunicate {
	public static enum Type {
		ZMIS_FRESHNESS_INIT, ZMIS_FRESHNESS_ANSWER, ZMI, QUERIES_INIT, QUERIES_ANSWER
	}

	public abstract Type getType();
}
