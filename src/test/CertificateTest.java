package test;

import static org.junit.Assert.assertEquals;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.CA.CAUtils;
import pl.edu.mimuw.cloudatlas.CA.ClientAuthenticationData;
import pl.edu.mimuw.cloudatlas.CA.CreateClientCC;

public class CertificateTest {

	@Test
	public void test() throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		KeyPair keys = CAUtils.getKeyPair();
		ClientAuthenticationData cert = CreateClientCC.generateCC(
				keys.getPrivate(), "/a/b/c");
		assertEquals(cert.getCertificate().isValid(keys.getPublic()), true);
	}

}
