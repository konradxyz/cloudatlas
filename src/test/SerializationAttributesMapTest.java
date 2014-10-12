package test;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xerces.internal.dom.AttributeMap;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.serialization.SerializatorAtributeMap;

public class SerializationAttributesMapTest extends SerializatorTest<AttributesMap> {

	@Override
	public List<AttributesMap> getObjects() {
		List<AttributesMap> result = new ArrayList<AttributesMap>();
		AttributesMap attributesMap = new AttributesMap();
		Attribute attribute = new Attribute("attribute1");
		Value valueString = new ValueString("value1");
		attributesMap.add(attribute, valueString);
		result.add(attributesMap);
		return result;
	}

	@Override
	public Serializator<AttributesMap> getSerializator() {
		SerializatorAtributeMap serializatorAtributeMap = new SerializatorAtributeMap();
		return serializatorAtributeMap;
	}

}
