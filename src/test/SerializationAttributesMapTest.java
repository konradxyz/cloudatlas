package test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.xerces.internal.dom.AttributeMap;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.serialization.Serializator;
import pl.edu.mimuw.cloudatlas.serialization.SerializatorAtributeMap;
import sun.net.InetAddressCachePolicy;

public class SerializationAttributesMapTest extends SerializatorTest<AttributesMap> {

	public void addToList(List<AttributesMap> list, String s, Value value){
		Attribute attribute = new Attribute(s);
		AttributesMap attributesMap = new AttributesMap();
		attributesMap.add(attribute, value);
		list.add(attributesMap);
	}
	
	@Override
	public List<AttributesMap> getObjects() {
		List<AttributesMap> result = new ArrayList<AttributesMap>();
		// Empty case:
		AttributesMap attributesMap = new AttributesMap();
		result.add(attributesMap);
		
		
		//value String
		addToList(result, "attribute1", new ValueString("value1"));
		
		//value Boolean
		addToList(result, "attribute2", new ValueBoolean(true));
				
		//value Contact
		PathName pathName = new PathName("/");
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName("127.0.0.1");
			Value valueContact = new ValueContact(pathName, inetAddress);
			addToList(result, "attribute3", valueContact);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		//value Double
		
		addToList(result, "attributeDouble", new ValueDouble(4.4));
		
		//valueDuration
		addToList(result, "attributeDuration", new ValueDuration(5L));
		
		//valueInt
		addToList(result, "attributeInt", new ValueInt(14L));
			
		//valueList
		List<Value> list = new ArrayList<Value>();
		list.add(new ValueDouble(8.8));
	//	addToList(result, "attributeList", new ValueList(list, TypePrimitive.DOUBLE));
		
		//value null
		addToList(result, "attributeNull", ValueNull.getInstance());
		
		//valueSet
		//Set<Value> set = new HashSet<Value>();
		//set.add(new ValueDouble(8.8));
		//addToList(result, "attributeSet", new ValueSet(set, TypePrimitive.DOUBLE));
		
		//value Time
		addToList(result, "attributeTime", new ValueTime(3L));
		return result;
	}

	@Override
	public Serializator<AttributesMap> getSerializator() {
		SerializatorAtributeMap serializatorAtributeMap = new SerializatorAtributeMap();
		serializatorAtributeMap.Init();
		return serializatorAtributeMap;
	}

}
