package pl.edu.mimuw.cloudatlas.CA;

import java.security.PrivateKey;
import java.security.PublicKey;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ZoneAuthenticationData {
	PublicKey siblingAuthenticationKey;
	PrivateKey zmiAuthenticationKey;
	Certificate certificate;
	
	public ZoneAuthenticationData(PublicKey siblingAuthenticationKey, PrivateKey zmiAuthenticationKey, Certificate certificate) {
		this.siblingAuthenticationKey = siblingAuthenticationKey;
		this.zmiAuthenticationKey = zmiAuthenticationKey;
		this.certificate = certificate;
	}
	
	public PrivateKey getZmiAuthenticationKey() {
		return zmiAuthenticationKey;
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public PublicKey getSiblingAuthenticationKey() {
		return siblingAuthenticationKey;
	}
}
