package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

public class TravelTime {
	private final Long sent;
	private final Long received;
	public TravelTime(Long sent, Long received) {
		super();
		this.sent = sent;
		this.received = received;
	}
	public Long getSent() {
		return sent;
	}
	public Long getReceived() {
		return received;
	}
}
