package pl.edu.mimuw.cloudatlas.common;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;

import com.esotericsoftware.kryo.Kryo;

public class Certificate {
	private AttributesMap attributesMap;
	private byte[] signature;
	public Certificate(AttributesMap attributesMap, PrivateKey privateKey, Kryo kryo) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.attributesMap = attributesMap;
		byte[] descr = KryoUtils.serialize(attributesMap, kryo);
		byte[] hash = SecurityUtils.computeHash(descr);
		Cipher signCipher = Cipher.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
		signature = signCipher.doFinal(hash);
	}
	
	public AttributesMap getAttributesMap() {
		return attributesMap;
	}
	
	public byte[] getSignature() {
		return signature;
	}
	
}
