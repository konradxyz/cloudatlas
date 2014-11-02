package pl.edu.mimuw.cloudatlas.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializatorString implements Serializator<String> {
	final Kryo kryo=new Kryo();
	@Override
	public void serialize(String object, OutputStream outputStream) throws IOException {
		Output output = new Output(outputStream);
		kryo.writeObject(output, object);
		output.close();
		
	}

	@Override
	public String deserialize(InputStream inputStream) throws IOException {
		Input input = new Input(inputStream);
		String object = kryo.readObject(input, String.class);
		return object;
	}

}
