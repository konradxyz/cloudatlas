package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicateWrapper;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class CommunicateSerializer {
	private Kryo kryo = new Kryo();

	public CommunicateSerializer() {
		kryo = KryoUtils.getKryo();	 
	}

	public GossipCommunicate deserialize(byte[] input) {
		return KryoUtils.deserialize(input, kryo, GossipCommunicate.class);
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
