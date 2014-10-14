package pl.edu.mimuw.cloudatlas.serialization;

import java.io.IOError;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;





import java.util.Set;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;



public class ValueSetSerializer extends Serializer<ValueSet> {
	
	@Override
	public ValueSet read(Kryo kryo, Input input, Class<ValueSet>  type) {
		TypeCollection collectionType = kryo.readObject(input, TypeCollection.class);
		@SuppressWarnings("unchecked")
		Set<Value> values = kryo.readObject(input, HashSet.class);
		return new ValueSet(values, collectionType.getElementType());
		
	}

	@Override
	public void write(Kryo kryo, Output output, ValueSet object) {
		kryo.writeObject(output, object.getType());
		kryo.writeObject(output, object.getValue());
	}

}
