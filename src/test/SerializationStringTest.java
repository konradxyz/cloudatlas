package test;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.common.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.common.serialization.SerializatorString;

public class SerializationStringTest extends SerializatorTest<String> {

	@Override
	public List<String> getObjects() {
		List<String> result = new ArrayList<String>();
		result.add("a");
		return result;
	}

	@Override
	public Serializator<String> getSerializator() {
		SerializatorString serializatorString = new SerializatorString();
		return serializatorString;
	}

}
