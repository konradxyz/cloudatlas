package pl.edu.mimuw.cloudatlas.serialization;

import java.util.ArrayList;
import java.util.List;

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
		TypeCollection collectionType = kryo.readObject(input, TypeCollection.class);
		@SuppressWarnings("unchecked")
		List<Value> values = kryo.readObject(input, ArrayList.class);
		return new ValueList(values, collectionType.getElementType());
		
	}

	@Override
	public void write(Kryo kryo, Output output, ValueList object) {
		kryo.writeObject(output, object.getType());
		kryo.writeObject(output, object.getValue());
	}

}
