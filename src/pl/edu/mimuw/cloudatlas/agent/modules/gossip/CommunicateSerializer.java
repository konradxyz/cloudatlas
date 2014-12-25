package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.net.Inet4Address;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicateWrapper;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.agent.serialization.Inet4AddressSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.PathNameSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.ValueListSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.ValueSetSerializer;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;

public class CommunicateSerializer {
	private final Kryo kryo = new Kryo();

	public CommunicateSerializer() {
		kryo.register(ValueContact.class);
		kryo.register(PathName.class, new PathNameSerializer());
		kryo.register(GossipCommunicateWrapper.class);
		kryo.register(GossipCommunicate.class);
		kryo.register(ValueString.class);
		kryo.register(String.class);
		kryo.register(ZmiCommunicate.class);


		kryo.addDefaultSerializer(Inet4Address.class,
				Inet4AddressSerializer.class);
		kryo.addDefaultSerializer(ValueList.class, ValueListSerializer.class);
		kryo.addDefaultSerializer(ValueSet.class, ValueSetSerializer.class);

	}

	public GossipCommunicate deserialize(byte[] input) {
		return kryo
				.readObject(new Input(input), GossipCommunicateWrapper.class)
				.getCommunicate();
	}

	public byte[] serialize(GossipCommunicate communicate) {
		Output o = new Output(1, Integer.MAX_VALUE);
		kryo.writeObject(o, new GossipCommunicateWrapper(communicate));
		return o.toBytes();
	}

}
