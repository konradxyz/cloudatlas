package pl.edu.mimuw.cloudatlas.serialization;

import java.io.IOError;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class Inet4AddressSerializer extends Serializer<Inet4Address> {
	
	@Override
	public Inet4Address read(Kryo kryo, Input input, Class<Inet4Address>  type) {
		@SuppressWarnings("unchecked")
		Serializer<byte[]> byteSerializator = kryo.getDefaultSerializer(byte[].class);
		byte[] address = byteSerializator.read(kryo,  input, byte[].class);
		try {
			return (Inet4Address)InetAddress.getByAddress(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new IOError(e); 
		}
	}

	@Override
	public void write(Kryo kryo, Output output, Inet4Address object) {
		@SuppressWarnings("unchecked")
		Serializer<byte[]> byteSerializator = kryo.getDefaultSerializer(byte[].class);
		byteSerializator.write(kryo, output, object.getAddress());
	}

}
