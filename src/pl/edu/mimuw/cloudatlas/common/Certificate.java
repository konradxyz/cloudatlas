package pl.edu.mimuw.cloudatlas.common;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;

import com.esotericsoftware.kryo.Kryo;

public class Certificate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4583768064796235190L;
	private AttributesMap attributesMap;
	private byte[] signature;

	public Certificate(AttributesMap attributesMap, PrivateKey privateKey,
			Kryo kryo) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.attributesMap = attributesMap;
		byte[] descr = KryoUtils.serialize(attributesMap, kryo);
		byte[] hash = SecurityUtils.computeHash(descr);
		Cipher signCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
		signature = signCipher.doFinal(hash);
	}

	public AttributesMap getAttributesMap() {
		return attributesMap;
	}

	public byte[] getSignature() {
		return signature;
	}

	public boolean isValid(PublicKey validationKey) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		byte[] bytes = KryoUtils.serialize(this.attributesMap,
				KryoUtils.getKryo());
		System.err.println("valid");
		System.err.println(attributesMap);
		System.err.println(DatatypeConverter.printHexBinary(bytes));
		return SecurityUtils.ifEqualMessages(bytes, validationKey,
				getSignature());
	}

}
