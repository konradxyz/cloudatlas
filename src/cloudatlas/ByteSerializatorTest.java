package cloudatlas;

import java.util.ArrayList;
import java.util.List;

import cloudatlas.ByteSerializator;
import pl.edu.mimuw.cloudatlas.serialization.Serializator;
import test.SerializatorTest;

public class ByteSerializatorTest extends SerializatorTest<Byte> {

	public List<Byte> getObjects() {
		List<Byte> result = new ArrayList<Byte>();
		result.add((byte) 3);
		result.add((byte) 8);
		result.add((byte) -1);
		return result;
	}

	public Serializator<Byte> getSerializator() {
		return new ByteSerializator();
	}

}