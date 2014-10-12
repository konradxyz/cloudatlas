package cloudatlas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.edu.mimuw.cloudatlas.serialization.Serializator;

public class ByteSerializator implements Serializator<Byte> {

	@Override
	public void serialize(Byte object, OutputStream output)
			throws IOException {
		output.write(object.byteValue());
	}

	@Override
	public Byte deserialize(InputStream input) throws IOException {
		int tmp = input.read();
		Byte result = new Byte((byte) tmp);
		return result;
	}

}
