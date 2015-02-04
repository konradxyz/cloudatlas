package pl.edu.mimuw.cloudatlas.CA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class CreateZone extends CommandReader {

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
			if (!(new File(caPath).exists())) {
				System.err.println("Base dir doesn't exist: " + caPath);
				return;
			}
			File publicKey = new File(caPath + "/" + CAUtils.publicKeyName);
			if (publicKey.exists()) {
				System.err.println("Public key of the zone " + pathName
						+ " exists");
				return;
			}
			CAUtils.generateKeys(caPath + "/");
		} else {
			PathName levelUp = pathName.levelUp();
			String privateKeyLevelUpPath = caPath + levelUp.toString()
					+ "/" + CAUtils.privateKeyName;
			// System.out.println(levelUp);
			File privateKeyFile = new File(privateKeyLevelUpPath);
			if (!privateKeyFile.exists()) {
				System.err.println("There is no private key for the zone "
						+ levelUp);
				System.err
						.println("This zone doesn't exist or this CA has no authorization to create subzones of this zone");
				return;
			}
			File zoneDirectory = new File(caPath + pathName.toString());
			if (zoneDirectory.exists()) {
				System.err.println("This zone " + pathName + " exists");
				return;
			}
			if (!(zoneDirectory.mkdir())) {
				System.err.println("Cannot create " + zoneDirectory);
				return;
			}
			String path = caPath + pathName.toString() + "/";
			CAUtils.generateKeys(path);
			PublicKey pkz = CAUtils.generateKeysZone(path);
			Date date = new Date();
			CAUtils.generateCertificate(CAUtils.readPrivateKey(privateKeyLevelUpPath), pkz, date,
					path);
		}
	}

}
