package test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.serialization.Serializator;
import javax.xml.bind.DatatypeConverter;

public abstract class SerializatorTest<T> {
	public abstract List<T> getObjects();

	public abstract Serializator<T> getSerializator();

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
		}
	}
}
