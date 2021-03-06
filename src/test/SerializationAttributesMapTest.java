package test;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.agent.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.agent.serialization.SerializatorAtributeMap;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.common.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;

public class SerializationAttributesMapTest extends SerializatorTest<AttributesMap> {

	public void addToList(List<AttributesMap> list, AttributesMap attributeMapGlobal, String s, Value value){
		Attribute attribute = new Attribute(s);
		AttributesMap attributesMap = new AttributesMap();
		attributesMap.add(attribute, value);
		attributeMapGlobal.add(attribute, value);
		list.add(attributesMap);
	}
	
	@Override
	public List<AttributesMap> getObjects() {
		List<AttributesMap> result = new ArrayList<AttributesMap>();
		// Empty case:
		AttributesMap attributesMap = new AttributesMap();
		AttributesMap attributesMapGlobal = new AttributesMap();
		result.add(attributesMap);
		
		
		//value String
		addToList(result, attributesMapGlobal, "attribute1", new ValueString("value1"));
		
		//value Boolean
		addToList(result, attributesMapGlobal, "attribute2", new ValueBoolean(true));
				
		//value Contact
		PathName pathName = new PathName("/");
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName("127.0.0.1");
			Value valueContact = new ValueContact(pathName, inetAddress);
			addToList(result, attributesMapGlobal, "attribute3", valueContact);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		//value Double
		
		addToList(result, attributesMapGlobal, "attributeDouble", new ValueDouble(4.4));
		
		//valueDuration
		addToList(result, attributesMapGlobal, "attributeDuration", new ValueDuration(5L));
		
		//valueInt
		addToList(result, attributesMapGlobal, "attributeInt", new ValueInt(14L));
			
		//valueList

		List<Value> list = new ArrayList<Value>();
		list.add(new ValueDouble(8.8));
		addToList(result, attributesMapGlobal, "attributeList", new ValueList(list, TypePrimitive.DOUBLE));
		
		//value null
		addToList(result, attributesMapGlobal, "attributeNull", ValueNull.getInstance());
		
		//valueSet
		Set<Value> set = new HashSet<Value>();
		set.add(new ValueDouble(8.8));
		addToList(result, attributesMapGlobal, "attributeSet", new ValueSet(set, TypePrimitive.DOUBLE));
		
		//value Time
		addToList(result, attributesMapGlobal, "attributeTime", new ValueTime(3L));
		result.add(attributesMapGlobal);
		return result;
	}
	
	@Override
	public void customCompare(AttributesMap object, AttributesMap deserialized) {
		for ( Entry<Attribute, Value> entry : object ){
			assertEquals(entry.getValue().getType(), deserialized.get(entry.getKey()).getType());
		}
	}

	@Override
	public Serializator<AttributesMap> getSerializator() {
		SerializatorAtributeMap serializatorAtributeMap = new SerializatorAtributeMap();
		serializatorAtributeMap.Init();
		return serializatorAtributeMap;
	}

}
