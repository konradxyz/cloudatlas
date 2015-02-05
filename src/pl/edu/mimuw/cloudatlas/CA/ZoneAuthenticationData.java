package pl.edu.mimuw.cloudatlas.CA;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ZoneAuthenticationData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6509250518652507275L;
	PublicKey childrenAuthenticationKey;
	PrivateKey zmiAuthenticationKey;
	Certificate certificate;
	
	public ZoneAuthenticationData(PublicKey childrenAuthenticationKey, PrivateKey zmiAuthenticationKey, Certificate certificate) {
		this.childrenAuthenticationKey = childrenAuthenticationKey;
		this.zmiAuthenticationKey = zmiAuthenticationKey;
		this.certificate = certificate;
	}
	
	public PrivateKey getZmiAuthenticationKey() {
		return zmiAuthenticationKey;
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public PublicKey getChildrenAuthenticationKey() {
		return childrenAuthenticationKey;
	}
}
