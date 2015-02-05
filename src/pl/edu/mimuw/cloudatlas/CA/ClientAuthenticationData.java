package pl.edu.mimuw.cloudatlas.CA;

import java.io.Serializable;
import java.security.PrivateKey;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ClientAuthenticationData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6509250518652507275L;
	PrivateKey clientAuthenticationKey;
	Certificate certificate;
	
	public ClientAuthenticationData(PrivateKey clientAuthenticationKey, Certificate certificate) {
		this.certificate = certificate;
		this.clientAuthenticationKey = clientAuthenticationKey;
	}
	
	public PrivateKey getClientAuthenticationKey() {
		return clientAuthenticationKey;
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
}
