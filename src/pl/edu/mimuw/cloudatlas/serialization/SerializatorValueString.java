package pl.edu.mimuw.cloudatlas.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializatorValueString implements Serializator<ValueString> {
	final Kryo kryo=new Kryo();
	@Override
	public void serialize(ValueString object, OutputStream outputStream) throws IOException {
		kryo.register(ValueString.class);
		Output output = new Output(outputStream);
		kryo.writeObject(output, object);
		output.close();
		
	}

	@Override
	public ValueString deserialize(InputStream inputStream) throws IOException {
		Input input = new Input(inputStream);
		ValueString object = kryo.readObject(input, ValueString.class);
		return object;
	}

}
