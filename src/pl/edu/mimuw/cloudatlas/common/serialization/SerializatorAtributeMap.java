package pl.edu.mimuw.cloudatlas.common.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializatorAtributeMap implements Serializator<AttributesMap> {
	final Kryo kryo = new Kryo();

	// Init should be called before serialization/deserialization.
	public void Init() {
		kryo.addDefaultSerializer(Inet4Address.class,
				Inet4AddressSerializer.class);
		kryo.addDefaultSerializer(ValueList.class, ValueListSerializer.class);
		kryo.addDefaultSerializer(ValueSet.class, ValueSetSerializer.class);
	}

	@Override
	public void serialize(AttributesMap object, OutputStream outputStream)
			throws IOException {
		Output output = new Output(outputStream);
		kryo.writeObject(output, object);
		output.close();
	}

	@Override
	public AttributesMap deserialize(InputStream inputStream)
			throws IOException {
		Input input = new Input(inputStream);
		AttributesMap object = kryo.readObject(input, AttributesMap.class);
		return object;
	}

}
