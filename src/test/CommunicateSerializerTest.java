package test;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.CommunicateSerializer;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;

public class CommunicateSerializerTest {
	private void singleTest(GossipCommunicate input) {
		CommunicateSerializer serializer = new CommunicateSerializer();
		GossipCommunicate output = serializer.deserialize(serializer.serialize(input));
		assertEquals(output, input);
	}

	@Test
	public void test() throws UnknownHostException {
		AttributesMap attrs = new AttributesMap();
		attrs.add("name", new ValueString("pc1"));
		attrs.add("contact", new ValueContact(new PathName("/j/dasdas"), InetAddress.getLocalHost()));
		singleTest(new ZmiCommunicate(PathName.ROOT, attrs));
		
		Kryo kryo = new Kryo();
		Output o = new Output(1111);
		kryo.writeObject(o, "qwerty".getBytes());
		System.out.println(o.toBytes().length);
	}

}
