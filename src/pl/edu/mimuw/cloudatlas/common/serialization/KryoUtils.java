package pl.edu.mimuw.cloudatlas.common.serialization;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import pl.edu.mimuw.cloudatlas.CA.ZoneAuthenticationData;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicateWrapper;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateAnswer;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateInit;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.TravelTime;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessAnswerCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessInitCommunicate;
import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.common.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;
import pl.edu.mimuw.cloudatlas.common.model.ValueNull;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;

public class KryoUtils {
	
	public static byte[] serialize(Object object, Kryo kryo){
		Output o = new Output(1, Integer.MAX_VALUE);
		kryo.writeObject(o, object);
		return o.toBytes();
	}
	public static Kryo getKryo() {
		final Kryo kryo = new Kryo();
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
		kryo.register(ValueTime.class);
		kryo.register(ValueKey.class, new ValuePublicKeySerializer());
		
		kryo.register(Certificate.class);
		kryo.register(ZoneAuthenticationData.class);
		kryo.addDefaultSerializer(ValueList.class, ValueListSerializer.class);
		kryo.addDefaultSerializer(ValueSet.class, ValueSetSerializer.class);
		return kryo;
	}
	
	public static byte[] readFile(String pathString) throws IOException {
		Path path = Paths.get(pathString);
		byte[] data = Files.readAllBytes(path);
		return Arrays.copyOf(data, data.length);
	}
	
	public static String readFileString(String pathString) throws IOException {
		Path path = Paths.get(pathString);
		byte[] data = Files.readAllBytes(path);
		return new String(Arrays.copyOf(data, data.length - 1));
	}
	
	public static <T> T deserialize(byte[] input, Kryo kryo, Class<T> cl) {
		return kryo
				.readObject(new Input(input), cl);
	}
}
