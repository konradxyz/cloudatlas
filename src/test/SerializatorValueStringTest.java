package test;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.common.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.common.serialization.SerializatorValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;

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
