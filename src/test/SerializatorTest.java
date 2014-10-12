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

public abstract class SerializatorTest<T> {
	public abstract List<T> getObjects();

	public abstract Serializator<T> getSerializator();

	@Test
	public void test() throws IOException {
		Serializator<T> serializator = getSerializator();
		for (T object : getObjects()) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			serializator.serialize(object, outputStream);
			InputStream inputStream = new ByteArrayInputStream(
					outputStream.toByteArray());
			T deserialized = serializator.deserialize(inputStream);
			assertEquals(object, deserialized);
			assertEquals(deserialized, object);
		}
	}
}
