package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import pl.edu.mimuw.cloudatlas.agent.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.agent.model.ZmiData;
import pl.edu.mimuw.cloudatlas.agent.model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate.Type;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessAnswerCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessInitCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.network.ReceivedDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SendDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SocketModule;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.GetRootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.UpdateRemoteZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;

public class GossipModule extends Module {
	private final CloudatlasAgentConfig config;
	private final Address zmiKeeperAddress;
	private final List<Inet4Address> fallbackContacts = new ArrayList<Inet4Address>();
	private final Random random = new Random();
	private final CommunicateSerializer communicateSerializer = new CommunicateSerializer();

	private Address socketModuleAddress;
	
	// Waiting for freshness info:
	// Here we store messages connected with gossiping initialized different machine.
	// We are going to send our freshness info.
	private Map<InetAddress, ZmisFreshness> freshnessInitRequests = new HashMap<InetAddress, ZmisFreshness>();
	// And here we have all messages connected with gossiping initialized by current machine.
	// It indicates that we have already sent our freshness info. We do not have to send it second time.
	private Map<InetAddress, ZmisFreshness> freshnessAnswerRequests = new HashMap<InetAddress, ZmisFreshness>();
	private boolean waitingForZmiForGossip = false;
	
	// Datagram handlers:
	private HashMap<GossipCommunicate.Type, HandleCommunicate<?>> datagramHandlers =
			new HashMap<GossipCommunicate.Type, GossipModule.HandleCommunicate<?>>();

	public GossipModule(Address address, CloudatlasAgentConfig config,
			Address zmiKeeperAddress) {
		super(address);
		this.config = config;
		this.zmiKeeperAddress = zmiKeeperAddress;
		datagramHandlers.put(Type.ZMIS_FRESHNESS_INIT, freshnessInitCommunicateHandler);
		datagramHandlers.put(Type.ZMIS_FRESHNESS_ANSWER, freshnessAnswerCommunicateHandler);
		datagramHandlers.put(Type.ZMI, zmiCommunicateHandler);
	}

	private static final int MESSAGE_RECEIVED = 1;
	public static final int START_GOSSIP = 2;
	private static final int RECEIVED_ZMI = 3;
	public static final int SET_FALLBACK_CONTACTS = 4;
	private static final int INITIALIZE_GOSSIP = 5;
	
	
	private abstract class HandleCommunicate<T extends GossipCommunicate> {
		public abstract void handle(T communicate, InetAddress source);
		@SuppressWarnings("unchecked")
		public void handleUntyped(GossipCommunicate communicate, InetAddress source) {
			System.out.println(communicate.toString());
			T comm = (T) communicate;
			comm.hashCode();
			handle(comm, source);
		}
	}
	
	private HandleCommunicate<ZmisFreshnessInitCommunicate> freshnessInitCommunicateHandler = new HandleCommunicate<ZmisFreshnessInitCommunicate>() {

		@Override
		public void handle(ZmisFreshnessInitCommunicate communicate, InetAddress source) {
			freshnessInitRequests.put(source, communicate.getContent());
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI, new GetRootZmiMessage(getAddress(), RECEIVED_ZMI));
		}
	};

	private HandleCommunicate<ZmisFreshnessAnswerCommunicate> freshnessAnswerCommunicateHandler = new HandleCommunicate<ZmisFreshnessAnswerCommunicate>() {

		@Override
		public void handle(ZmisFreshnessAnswerCommunicate communicate, InetAddress source) {
			freshnessAnswerRequests.put(source, communicate.getContent());
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI, new GetRootZmiMessage(getAddress(), RECEIVED_ZMI));
		}
	};
	
	private HandleCommunicate<ZmiCommunicate> zmiCommunicateHandler = new HandleCommunicate<ZmiCommunicate>() {

		@Override
		public void handle(ZmiCommunicate communicate, InetAddress source) {
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.UPDATE_REMOTE_ZMI,
					new UpdateRemoteZmiMessage(communicate.getPathName(),
							communicate.getAttributes()));
		}
	};
	
	private MessageHandler<ReceivedDatagramMessage> receivedMessageHandler = new MessageHandler<ReceivedDatagramMessage>() {

		@Override
		public void handleMessage(ReceivedDatagramMessage message)
				throws HandlerException {
			GossipCommunicate communicate = communicateSerializer
					.deserialize(message.getContent()); 
			Type tp = communicate.getType();
			datagramHandlers.get(tp).handleUntyped(communicate, message.getSource());
		}
	};

	private MessageHandler<Message> startGossipHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			System.out.println("start gossip, requesting current zmi");
			sendMessage(
					zmiKeeperAddress,
					ZmiKeeperModule.GET_ROOT_ZMI,
					new GetRootZmiMessage(getAddress(),  RECEIVED_ZMI));
			waitingForZmiForGossip = true;
		}
	};
	
	private MessageHandler<RootZmiMessage> receivedZmi = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			sendMessage(getAddress(), INITIALIZE_GOSSIP, 
					new SimpleMessage<ZmisAttributes>(message.getContent().clone()));
			sendZmisAndOptionallyFreshness(freshnessInitRequests, message.getContent(), true);
			freshnessInitRequests.clear();
			sendZmisAndOptionallyFreshness(freshnessAnswerRequests, message.getContent(), false);
			freshnessAnswerRequests.clear();
		}
	};
	
	
	
	private MessageHandler<SimpleMessage<List<Inet4Address>>> setFallbackContactsHandler = 
			new MessageHandler<SimpleMessage<List<Inet4Address>>>() {

				@Override
				public void handleMessage(
						SimpleMessage<List<Inet4Address>> message)
						throws HandlerException {
					fallbackContacts.clear();
					fallbackContacts.addAll(message.getContent());
				}
		
	};

	private void sendZmisAndOptionallyFreshness(
			Map<InetAddress, ZmisFreshness> freshnessRequests,
			ZmisAttributes attrs, boolean sendFreshness) {

		for (Entry<InetAddress, ZmisFreshness> e : freshnessRequests.entrySet()) {
			try {
				if (sendFreshness) {
					ZmisFreshness freshness = generateFreshness(attrs,
							Math.min(e.getValue().getLevels().size() - 1, attrs
									.getLevels().size() - 1));
					sendNetworkMessage(new ZmisFreshnessAnswerCommunicate(
							freshness), e.getKey());
				}
				//TODO: make sure we do not send target's local zones.
				List<ZmiData<AttributesMap>> toSend = filterNewer(
						attrs.getContent(), e.getValue());
				for (ZmiData<AttributesMap> single : toSend) {
					sendNetworkMessage(new ZmiCommunicate(single.getPath(),
							single.getContent()), e.getKey());
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}

		}

	}

	private ZmisFreshness generateFreshness(ZmisAttributes zmis, int level) {
		List<ZmiLevel<Long>> resList = new ArrayList<ZmiLevel<Long>>();
		for ( ZmiLevel<AttributesMap> l : zmis.getLevels().subList(0,  level + 1) ){
			Map<String, Long> timestamps = new HashMap<String, Long>();
			for ( Entry<String, AttributesMap> e : l.getZones().entrySet()) {
				try { 
					timestamps.put(e.getKey(), ((ValueTime)e.getValue().get("timestamp")).getValue());
				} catch (Exception exc) {
					// Silently skipping.
				}
			}
			
			ZmiLevel<Long> freshnessLevel = new ZmiLevel<Long>(l.getOurZoneName(), timestamps);
			resList.add(freshnessLevel);
		}
		return new ZmisFreshness(resList);
	}
	
	private MessageHandler<SimpleMessage<ZmisAttributes>> initializeGossipHandler = 
			new MessageHandler<SimpleMessage<ZmisAttributes>>() {

		@Override
		public void handleMessage(SimpleMessage<ZmisAttributes> message)
				throws HandlerException {
			if (waitingForZmiForGossip) {
				waitingForZmiForGossip = false;
				initializeGossip(message.getContent());
			}
		}
	};

	private static List<ZmiData<AttributesMap>> filterNewer(
			List<ZmiData<AttributesMap>> myZmis, ZmisFreshness otherFreshness) {
		List<ZmiData<AttributesMap>> result = new ArrayList<ZmiData<AttributesMap>>();
		for (ZmiData<AttributesMap> myAttrs : myZmis) {
			try {
				Long myTimestamp = ((ValueTime) myAttrs.getContent().get(
						"timestamp")).getValue();
				Long otherTimestamp = -1l;
				try {
					otherTimestamp = otherFreshness.get(myAttrs.getPath());
					// TODO: remove next line.
					otherTimestamp = -1l;
				} catch ( UnknownZoneException e) {
					otherTimestamp = -1l;
				}
				if (myTimestamp > otherTimestamp) {
					result.add(new ZmiData<AttributesMap>(myAttrs.getPath(),
							myAttrs.getContent().clone()));
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return result;
	}

	private void initializeGossip(ZmisAttributes zmis) throws HandlerException {
		// For now we assume we are only using fallback contacts.
		if (fallbackContacts.isEmpty()) {
			throw new HandlerException("GossipModule: empty fallback contacts");
		}
		Inet4Address target = fallbackContacts.get(random
				.nextInt(fallbackContacts.size()));
		ZmisFreshness freshness = generateFreshness(zmis, zmis.getLevels()
				.size() - 1);
		sendNetworkMessage(new ZmisFreshnessInitCommunicate(freshness), target);
	}

	private void sendNetworkMessage(GossipCommunicate message, InetAddress target) {
		byte[] o = communicateSerializer.serialize(message);
		sendMessage(
				socketModuleAddress,
				SocketModule.SEND_MESSAGE,
				new SendDatagramMessage(o, target, config
						.getPort()));
	}

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { 
					MESSAGE_RECEIVED, 
					START_GOSSIP,
					RECEIVED_ZMI, 
					SET_FALLBACK_CONTACTS,
					INITIALIZE_GOSSIP},
				new MessageHandler<?>[] { 
					receivedMessageHandler,
					startGossipHandler, 
					receivedZmi,
					setFallbackContactsHandler,
					initializeGossipHandler});
	}

	@Override
	public List<Module> getSubModules(AddressGenerator generator) {
		socketModuleAddress = generator.getUniqueAddress();
		return Arrays.asList((Module) new SocketModule(socketModuleAddress, 
				config.getPort(), 
				config.getMaxMessageSizeBytes(), 
				getAddress(),
				MESSAGE_RECEIVED));
	}

}
