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
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;

import com.esotericsoftware.kryo.Kryo;

public class CreateClientCC extends CommandReader {

	@Override
	public void perform(String caPath, String[] args)
			throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			FileNotFoundException, IOException, NoSuchProviderException {
		String zoneName;
		zoneName = args[1];
		AttributesMap attributesMap = new AttributesMap();
		attributesMap.add("zoneName", new ValueString(zoneName));
		KeyPair keyPair = CAUtils.getKeyPair();
		
		Certificate certificate = new Certificate(attributesMap, null, KryoUtils.getKryo());
	}

}
