package pl.edu.mimuw.cloudatlas.common.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializator<T>{
	public void serialize(T object, OutputStream output) throws IOException;
	public T deserialize(InputStream input) throws IOException;

}
