package test;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.serialization.SerializatorValueString;

public class SerializatorValueStringTest extends SerializatorTest<ValueString> {

	@Override
	public List<ValueString> getObjects() {
		List<ValueString> result = new ArrayList<ValueString>();
		result.add(new ValueString("a"));
		return result;
	}

	@Override
	public Serializator<ValueString> getSerializator() {
		SerializatorValueString serializatorString = new SerializatorValueString();
		return serializatorString;
	}

}
