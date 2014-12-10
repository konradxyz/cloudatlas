package test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.agent.serialization.Serializator;

public abstract class SerializatorTest<T> {
	public abstract List<T> getObjects();

	public abstract Serializator<T> getSerializator();
	public void customCompare(T object, T deserialized) {
		
	}

	@Test
	public void test() throws IOException {
		Serializator<T> serializatorS = getSerializator();
		Serializator<T> serializatorD = getSerializator();
		for (T object : getObjects()) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			serializatorS.serialize(object, outputStream);
			InputStream inputStream = new ByteArrayInputStream(
					outputStream.toByteArray());
			T deserialized = serializatorD.deserialize(inputStream);
			System.out.println(object.toString());
			System.out.println(DatatypeConverter.printHexBinary(outputStream
					.toByteArray()));
			assertEquals(object, deserialized);
			assertEquals(deserialized, object);
			customCompare(object, deserialized);
		}
	}
}
