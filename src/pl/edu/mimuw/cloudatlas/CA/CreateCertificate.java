package pl.edu.mimuw.cloudatlas.CA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;

import com.esotericsoftware.kryo.Kryo;

public class CreateCertificate extends CommandReader {

	String zoneAuthenticationName = "zoneAuthentication.txt";
	@Override
	public void perform(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException, NoSuchProviderException {
		PathName pathName;
		try {
			pathName = new PathName(args[1]);
		} catch (IllegalArgumentException e) {
			System.err.println("Wrong path name " + args[1]);
			return;
		}
		if (pathName.equals(PathName.ROOT)) {
			return;
		} else {
			ArrayList<ZoneAuthenticationData> authenticationList = new ArrayList<ZoneAuthenticationData>();
			Kryo kryo = KryoUtils.getKryo();
			while (!pathName.equals(PathName.ROOT)) {	
				String publicKeyPath = CAUtils.baseDir + pathName.levelUp().toString()
						+ "/" + CAUtils.publicKeyName;
				String privateKeyZonePath = CAUtils.baseDir + pathName.toString()
						+ "/" + CAUtils.privateKeyZoneName;
				PublicKey publiKey = CAUtils.readPublicKey(publicKeyPath);
				PrivateKey privateKey = CAUtils.readPrivateKey(privateKeyZonePath);
				String certificateZonePath = CAUtils.baseDir + pathName.toString()
						+ "/" + CAUtils.certificateName;
				byte[] certificateText = KryoUtils.readFile(certificateZonePath);
				Certificate certificate = KryoUtils.deserialize(certificateText, kryo, Certificate.class);
				authenticationList.add(new ZoneAuthenticationData(publiKey, privateKey, certificate));//wrong public key
				pathName = pathName.levelUp();
			}
			Collections.reverse(authenticationList);
			byte[] toFile = KryoUtils.serialize(authenticationList, kryo);
			String zoneAuthenticationPath = CAUtils.baseDir + (new PathName(args[1])).toString()
					+ "/" + zoneAuthenticationName;
			CAUtils.createFile(zoneAuthenticationPath, toFile);
		}
	}


}
