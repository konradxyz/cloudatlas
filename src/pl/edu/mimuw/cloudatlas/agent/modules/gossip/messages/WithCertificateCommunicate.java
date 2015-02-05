package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public abstract class WithCertificateCommunicate extends GossipCommunicate{
	private final Certificate certificate;
	private final Integer gossipLevel;
	
	public WithCertificateCommunicate(Certificate certificate, Integer gossipLevel) {
		this.certificate = certificate;
		this.gossipLevel = gossipLevel;
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public Integer getGossipLevel() {
		return gossipLevel;
	}
}
