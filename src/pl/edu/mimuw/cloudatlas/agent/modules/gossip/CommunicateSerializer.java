package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicateWrapper;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateAnswer;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateInit;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.TravelTime;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessAnswerCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessInitCommunicate;
import pl.edu.mimuw.cloudatlas.agent.serialization.Inet4AddressSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.PathNameSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.ValueListSerializer;
import pl.edu.mimuw.cloudatlas.agent.serialization.ValueSetSerializer;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.common.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;

public class CommunicateSerializer {
	private final Kryo kryo = new Kryo();

	public CommunicateSerializer() {

		kryo.register(GossipCommunicateWrapper.class);
		kryo.register(GossipCommunicate.class);

		kryo.register(ZmiCommunicate.class);
		kryo.register(QueriesCommunicateAnswer.class);
		kryo.register(QueriesCommunicateInit.class);
		kryo.register(TravelTime.class);
		kryo.register(ZmisFreshnessAnswerCommunicate.class);
		kryo.register(ZmisFreshnessInitCommunicate.class);

		kryo.register(ZmisFreshness.class);
		kryo.register(ZmiLevel.class);

		kryo.register(Attribute.class);
		kryo.register(AttributesMap.class);
		kryo.register(PathName.class, new PathNameSerializer());
		kryo.register(HashMap.class);
		kryo.register(ArrayList.class);
		kryo.register(Inet4Address.class, new Inet4AddressSerializer());

		kryo.register(ValueBoolean.class);
		kryo.register(ValueContact.class);
		kryo.register(ValueDouble.class);
		kryo.register(ValueDuration.class);
		kryo.register(ValueInt.class);
		kryo.register(ValueNull.class);
		kryo.register(ValueQuery.class);
		kryo.register(ValueString.class);
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

	public Kryo getKryo() {
		return kryo;
	}
}
