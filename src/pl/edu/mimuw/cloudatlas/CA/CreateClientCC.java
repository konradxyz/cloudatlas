package pl.edu.mimuw.cloudatlas.CA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;

import com.esotericsoftware.kryo.Kryo;

public class CreateClientCC extends CommandReader {

	public static ClientAuthenticationData generateCC(PrivateKey parentKey, String pathName) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		AttributesMap attributesMap = new AttributesMap();
		attributesMap.add("zoneName", new ValueString(pathName.toString()));
		Kryo kryo = KryoUtils.getKryo();
		KeyPair keyPair = CAUtils.getKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
		attributesMap.add("publicKey", new ValueKey(publicKey));
		Certificate certificate = new Certificate(attributesMap, parentKey, kryo);
		ClientAuthenticationData clientAuthenticationData = new ClientAuthenticationData(privateKey, certificate);
		return clientAuthenticationData;
	}
	
	@Override
	public void perform(String caPath, String[] args)
			throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			FileNotFoundException, IOException, NoSuchProviderException {
		String pathName = args[1];
		String pathFile;
		if (args.length == 2)
			pathFile = CAUtils.ccPath + "/" + CAUtils.clientAuthenticationName;
		else 
			pathFile = args[2];
			
		PrivateKey clientAuthenticationKey = CAUtils.readPrivateKey(caPath + "/" +pathName +"/"+ CAUtils.privateKeyName );
		ClientAuthenticationData clientAuthenticationData = generateCC(clientAuthenticationKey, pathName);
		PublicKey clientCheckKey = CAUtils.readPublicKey(caPath + "/" +pathName +"/"+ CAUtils.publicKeyName);
		System.err.println(DatatypeConverter.printHexBinary(clientCheckKey.getEncoded()));
		if ( !clientAuthenticationData.getCertificate().isValid(clientCheckKey) ) {
			throw new RuntimeException("generated invalid certificate");
		}
		byte[] toFile = KryoUtils.serialize(clientAuthenticationData, KryoUtils.getKryo());
		CAUtils.createFile(pathFile , toFile);
	}
}
