package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.GetMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection.ContactSelectionStrategy;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection.ContactSelectionStrategy.ContactResult;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection.RoundRobinContactSelectionStrategy;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate.Type;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmiCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessAnswerCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.ZmisFreshnessInitCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.network.ReceivedDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SendDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SocketModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.InstallQueryMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.query.QueryKeeperModule;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.TimerModule;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.UpdateRemoteZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public class GossipModule extends Module {
	private final CloudatlasAgentConfig config;
	private final Address zmiKeeperAddress;
	private final Address queryKeeperAddress;
	private final List<InetAddress> fallbackContacts = new ArrayList<InetAddress>();
	private final Random random = new Random();
	private final CommunicateSerializer communicateSerializer = new CommunicateSerializer();

	private Address socketModuleAddress;
	private final Address timerModuleAddress;
	
	// Waiting for freshness info:
	// Here we store messages connected with gossiping initialized different machine.
	// We are going to send our freshness info.
	private Map<InetAddress, ZmisFreshness> freshnessInitRequests = new HashMap<InetAddress, ZmisFreshness>();
	// And here we have all messages connected with gossiping initialized by current machine.
	// It indicates that we have already sent our freshness info. We do not have to send it second time.
	private Map<InetAddress, ZmisFreshness> freshnessAnswerRequests = new HashMap<InetAddress, ZmisFreshness>();
	private boolean waitingForZmiForGossip = false;

	private List<InetAddress> waitingForQueries = new ArrayList<InetAddress>();
	
	// Datagram handlers:
	private HashMap<GossipCommunicate.Type, HandleCommunicate<?>> datagramHandlers =
			new HashMap<GossipCommunicate.Type, GossipModule.HandleCommunicate<?>>();
	
	private ContactSelectionStrategy selectionStrategy = new RoundRobinContactSelectionStrategy();

	public GossipModule(Address address, CloudatlasAgentConfig config,
			Address zmiKeeperAddress, Address queryKeeperAddress, Address timerModuleAddress) {
		super(address);
		this.config = config;
		this.zmiKeeperAddress = zmiKeeperAddress;
		datagramHandlers.put(Type.ZMIS_FRESHNESS_INIT, freshnessInitCommunicateHandler);
		datagramHandlers.put(Type.ZMIS_FRESHNESS_ANSWER, freshnessAnswerCommunicateHandler);
		datagramHandlers.put(Type.ZMI, zmiCommunicateHandler);
		datagramHandlers.put(Type.QUERIES, queriesCommunicateHandler);
		this.timerModuleAddress = timerModuleAddress;
		this.queryKeeperAddress = queryKeeperAddress;
		if ( config.getFallbackAddress() != null )
			fallbackContacts.add(config.getFallbackAddress());
	}

	private static final int MESSAGE_RECEIVED = 1;
	public static final int START_GOSSIP = 2;
	private static final int RECEIVED_ZMI = 3;
	public static final int SET_FALLBACK_CONTACTS = 4;
	private static final int INITIALIZE_GOSSIP = 5;
	public static final int INITIALIZE_MODULE = 6;
	public static final int GET_FALLBACK_CONTACTS = 7;
	private static final int QUERIES_RECEIVED = 8;
	
	
	private abstract class HandleCommunicate<T extends GossipCommunicate> {
		public abstract void handle(T communicate, InetAddress source);
		@SuppressWarnings("unchecked")
		public void handleUntyped(GossipCommunicate communicate, InetAddress source) {
			T comm = (T) communicate;
			comm.hashCode();
			handle(comm, source);
		}
	}
	
	private HandleCommunicate<ZmisFreshnessInitCommunicate> freshnessInitCommunicateHandler = new HandleCommunicate<ZmisFreshnessInitCommunicate>() {

		@Override
		public void handle(ZmisFreshnessInitCommunicate communicate, InetAddress source) {
			freshnessInitRequests.put(source, communicate.getContent());
			waitingForQueries.add(source);
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI, new GetMessage(getAddress(), RECEIVED_ZMI));
			sendMessage(queryKeeperAddress, QueryKeeperModule.GET_QUERIES, new GetMessage(getAddress(), QUERIES_RECEIVED));
		}
	};

	private HandleCommunicate<ZmisFreshnessAnswerCommunicate> freshnessAnswerCommunicateHandler = new HandleCommunicate<ZmisFreshnessAnswerCommunicate>() {

		@Override
		public void handle(ZmisFreshnessAnswerCommunicate communicate, InetAddress source) {
			freshnessAnswerRequests.put(source, communicate.getContent());
			waitingForQueries.add(source);
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI, new GetMessage(getAddress(), RECEIVED_ZMI));
			sendMessage(queryKeeperAddress, QueryKeeperModule.GET_QUERIES, new GetMessage(getAddress(), QUERIES_RECEIVED));
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
	
	private HandleCommunicate<QueriesCommunicate> queriesCommunicateHandler = new HandleCommunicate<QueriesCommunicate>() {

		@Override
		public void handle(QueriesCommunicate communicate, InetAddress source) {
			sendMessage(queryKeeperAddress, QueryKeeperModule.INSTALL_QUERY,
					new InstallQueryMessage(communicate.getQueries()));
			
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
			sendMessage(
					zmiKeeperAddress,
					ZmiKeeperModule.GET_ROOT_ZMI,
					new GetMessage(getAddress(),  RECEIVED_ZMI));
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
	
	private MessageHandler<GetMessage> getFallbackContactsHandler = new MessageHandler<GetMessage>() {

		@Override
		public void handleMessage(GetMessage message) throws HandlerException {
			sendMessage(message.getResponseTarget(),
					message.getResponseMessageType(),
					new SimpleMessage<List<InetAddress>>(
							new ArrayList<InetAddress>(fallbackContacts)));
			
		}
	};
	
	private MessageHandler<SimpleMessage<List<InetAddress>>> setFallbackContactsHandler = 
			new MessageHandler<SimpleMessage<List<InetAddress>>>() {

				@Override
				public void handleMessage(
						SimpleMessage<List<InetAddress>> message)
						throws HandlerException {
					fallbackContacts.clear();
					fallbackContacts.addAll(message.getContent());
				}
		
	};

	private MessageHandler<SimpleMessage<Map<String, ValueQuery>>> queriesReceivedHandler = new MessageHandler<SimpleMessage<Map<String, ValueQuery>>>() {

		@Override
		public void handleMessage(SimpleMessage<Map<String, ValueQuery>> message)
				throws HandlerException {
			List<ValueQuery> queries = new ArrayList<ValueQuery>(message
					.getContent().values());
			for (InetAddress addr : waitingForQueries) {
				sendNetworkMessage(new QueriesCommunicate(queries), addr);
			}
			waitingForQueries.clear();
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
				List<ZmiData<AttributesMap>> toSend = filterNewer(
						attrs.getContent(), e.getValue());
				PathName targetPath = e.getValue().getPath();
				for (ZmiData<AttributesMap> single : toSend) {
					if ( !single.getPath().isPrefixOf(targetPath)) {
						sendNetworkMessage(new ZmiCommunicate(single.getPath(),
							single.getContent()), e.getKey());
					}
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
	
	private MessageHandler<Message> initializeHandler = new MessageHandler<Message>() {
		
		@Override
		public void handleMessage(Message message) throws HandlerException {
			sendMessage(timerModuleAddress, TimerModule.SCHEDULE_MESSAGE, 
					new ScheduleAlarmMessage(0, 0, config.getGossipPeriodMs(), 
							getAddress(), START_GOSSIP));
		}
	};

	private static List<ZmiData<AttributesMap>> filterNewer(
			List<ZmiData<AttributesMap>> myZmis, ZmisFreshness otherFreshness) {
		List<ZmiData<AttributesMap>> result = new ArrayList<ZmiData<AttributesMap>>();
		PathName otherPathName = otherFreshness.getPath();
		for (ZmiData<AttributesMap> myAttrs : myZmis) {
			try {
				if ( !myAttrs.getPath().levelUp().isPrefixOf(otherPathName) ) {
					continue;
				}
				Long myTimestamp = ((ValueTime) myAttrs.getContent().get(
						"timestamp")).getValue();
				Long otherTimestamp = -1l;
				try {
					otherTimestamp = otherFreshness.get(myAttrs.getPath());
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
		ContactResult contact = selectionStrategy.selectContact(zmis);
		InetAddress target = null;
		int level = 1;
		if ( contact == null ) {
			if (fallbackContacts.isEmpty()) {
				throw new HandlerException("GossipModule: empty fallback contacts");
			}
			target = fallbackContacts.get(random
					.nextInt(fallbackContacts.size()));
			level = zmis.getLevels().size() - 1;
		} else {
			target = contact.getContact().getAddress();
			level = contact.getLevel();
		}
		ZmisFreshness freshness = generateFreshness(zmis, level);
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
					INITIALIZE_GOSSIP,
					INITIALIZE_MODULE,
					GET_FALLBACK_CONTACTS,
					QUERIES_RECEIVED},
				new MessageHandler<?>[] { 
					receivedMessageHandler,
					startGossipHandler, 
					receivedZmi,
					setFallbackContactsHandler,
					initializeGossipHandler,
					initializeHandler,
					getFallbackContactsHandler,
					queriesReceivedHandler});
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
