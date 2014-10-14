package pl.edu.mimuw.cloudatlas.serialization;

import java.io.IOError;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;




import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;



public class ValueListSerializer extends Serializer<ValueList> {
	
	@Override
	public ValueList read(Kryo kryo, Input input, Class<ValueList>  type) {
		Type collectionType = kryo.readObject(input, Type.class);
		//List<Value> values = kryo.readObject(input, ArrayList<Value> .class);
		//return new ValueList(collectionType, values);
		return null;
	}

	@Override
	public void write(Kryo kryo, Output output, ValueList object) {
		kryo.writeObject(output, object.getType());
		kryo.writeObject(output, object.getValue());
	}

}
