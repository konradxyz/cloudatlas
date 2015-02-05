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

	String zoneAuthenticationName = "singletonZoneAuthentication";
	@Override
	public void perform(String caPath, String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException, NoSuchProviderException {
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
			boolean done = false;
			while (!done) {	
				String childrenAuthKeyPath = caPath + pathName.toString()
						+ "/" + CAUtils.publicKeyName;
				PublicKey childrenAuthKey = CAUtils.readPublicKey(childrenAuthKeyPath);
				
				PrivateKey zmiAuthKey = null;
				
				Certificate certificate = null;
				if ( !pathName.equals(PathName.ROOT)) {
					String zmiAuthKeyPath = caPath + pathName.toString() + "/" + CAUtils.privateKeyZoneName;
					zmiAuthKey = CAUtils.readPrivateKey(zmiAuthKeyPath);
					String certificateZonePath = caPath + pathName.toString()
							+ "/" + CAUtils.certificateName;
					byte[] certificateText = KryoUtils.readFile(certificateZonePath);
					certificate = KryoUtils.deserialize(certificateText, kryo, Certificate.class);
				}
				
				authenticationList.add(new ZoneAuthenticationData(childrenAuthKey, zmiAuthKey, certificate));
				if ( pathName.equals(PathName.ROOT) )
					done = true;
				else
					pathName = pathName.levelUp();
			}
			Collections.reverse(authenticationList);
			byte[] toFile = KryoUtils.serialize(authenticationList, kryo);
			String zoneAuthenticationPath = caPath + (new PathName(args[1])).toString()
					+ "/" + zoneAuthenticationName;
			CAUtils.createFile(zoneAuthenticationPath, toFile);
		}
	}


}
