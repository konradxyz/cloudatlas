package pl.edu.mimuw.cloudatlas.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SecurityUtils {
	private static final long HASH_LENGTH = 64;
	
	public static class SignatureMessage {
		byte[] signature;
		byte[] message;
		SignatureMessage(byte[] signature, byte[] message) {
			this.signature = signature;
			this.message = message;
		}
		
		public byte[] getMessage() {
			return message;
		}
		
		public byte[] getSignature() {
			return signature;
		}
	}
	
	public static byte[] computeHash(byte[] value) {
		try {
			MessageDigest digestGenerator = MessageDigest
					.getInstance("SHA-256");
			return digestGenerator.digest(value);
		} catch (NoSuchAlgorithmException e) {
			throw new InternalError(e.getMessage());
		}
	}
	
	public static Boolean ifEqualMessages(byte[] message, PublicKey publicKey, byte[] signature) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		byte[] communicateTimestampHash = SecurityUtils.computeHash(message);
		Cipher verifyCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		verifyCipher.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] decryptedSignature = verifyCipher.doFinal(signature);
		return (Arrays.equals(communicateTimestampHash, decryptedSignature));
	}

	public static byte[] computeSignature(byte[] value, PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] hash = computeHash(value);
		Cipher signCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		signCipher.init(Cipher.ENCRYPT_MODE, privateKey);

		byte[] signature = signCipher.doFinal(hash);
		return signature;

	}

	public static byte[] prependSignature(byte[] value, PrivateKey privateKey)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		byte[] signature = computeSignature(value, privateKey);

		ByteArrayOutputStream prepareStreamByte = new ByteArrayOutputStream();
		DataOutputStream prepareStream = new DataOutputStream(prepareStreamByte);
		prepareStream.write(signature);
		prepareStream.write(value);
		byte[] toSend = prepareStreamByte.toByteArray();
		return toSend;
	}
	
	// Trzeba zwrocic pare
	// lepiej ja nazwac - czyli stworzyc klase <- to
	// nawet w signatureutils
	public static SignatureMessage divideMessage(byte[] message) {
		byte[] keyByte = Arrays.copyOfRange(message, 0, 64);
		byte[] messageByte = Arrays.copyOfRange(message, 64, message.length);
		return new SignatureMessage(keyByte, messageByte);
	}
	// tutaj mozna wzrucoc kod do podzialy na sygnature i wiadomosc

	public static final String ENCRYPTION_ALGORITHM = "RSA";
}
