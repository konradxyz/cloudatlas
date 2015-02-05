package pl.edu.mimuw.cloudatlas.common.serialization;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import pl.edu.mimuw.cloudatlas.common.model.ValueKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ValuePublicKeySerializer extends Serializer<ValueKey> {

	@Override
	public ValueKey read(Kryo kryo, Input input, Class<ValueKey> type) {
		byte[] address = kryo.readObject(input, byte[].class);
		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = factory
					.generatePublic(new X509EncodedKeySpec(address));
			return new ValueKey(publicKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void write(Kryo kryo, Output output, ValueKey object) {
		kryo.writeObject(output, object.getValue().getEncoded());
	}

}
