package test;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.CA.CAUtils;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.CommunicateSerializer;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.FallbackCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateInit;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class CommunicateSerializerTest {
	private void singleTest(GossipCommunicate input) {
		CommunicateSerializer serializer = new CommunicateSerializer();
		byte[] o = serializer.serialize(input);
		System.out.println(o.length);
		GossipCommunicate output = serializer.deserialize(serializer.serialize(input));
		assertEquals(output, input);
	}
	
	private void serialize(Object obj) {
		CommunicateSerializer serializer = new CommunicateSerializer();
		Output o = new Output(1, Integer.MAX_VALUE);
		serializer.getKryo().writeObject(o, obj);
		System.out.println(o.toBytes().length);
	}
	
	
	private <T> void test(T obj, Class<T> cl) {
		Kryo kryo = KryoUtils.getKryo();
		byte[] o = KryoUtils.serialize(obj, kryo);
		KryoUtils.deserialize(o, kryo, cl);
	}


	@Test
	public void test() throws UnknownHostException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		test(new ValueKey(CAUtils.getKeyPair().getPublic()), ValueKey.class);
	//	test(CAUtils.getKeyPair().getPublic(), PublicKey.class);
		KeyPair p = CAUtils.getKeyPair();
		AttributesMap map = new AttributesMap();
		map.add("key", new ValueKey(p.getPublic()));
		Certificate c = new Certificate(new AttributesMap(), CAUtils.getKeyPair()
				.getPrivate(), KryoUtils.getKryo());
		singleTest(new FallbackCommunicate(c, 2, PathName.ROOT));
		test(new Certificate(new AttributesMap(), CAUtils.getKeyPair()
				.getPrivate(), KryoUtils.getKryo()), Certificate.class);
		AttributesMap attrs = new AttributesMap();
		attrs.add("name", new ValueString("pc1"));
		attrs.add("contact", new ValueContact(new PathName("/j/dasdas"), InetAddress.getLocalHost()));
		attrs.add("query", new ValueQuery("&a", "b", 1l, new byte[1]));
		attrs.add("int", new ValueInt(1l));
		singleTest(new ZmiCommunicate(PathName.ROOT, attrs, c, 1));
		/*
		Kryo kryo = new Kryo();
		Output o = new Output(1111);
		kryo.writeObject(o, "qwerty".getBytes());
		System.out.println(o.toBytes().length);
		
		
		serialize(new PathName("/j/dasdas"));
		serialize(InetAddress.getLocalHost());
		serialize(new ValueContact(new PathName("/j/dasdas"), InetAddress.getLocalHost()));
		serialize(new Attribute("contact"));
		
		serialize(attrs);
		singleTest(new ZmiCommunicate(PathName.ROOT, attrs, null, 1));
		
		serialize(new QueriesCommunicateInit(new ArrayList<ValueQuery>(), 0, null));
		serialize(new ZmisFreshness(new ArrayList<ZmiLevel<Long>>()));
*/		
	}

}
