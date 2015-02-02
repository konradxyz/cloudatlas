package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.CA.ZoneAuthenticationData;
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
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.GossipCommunicate.Type;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicate;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateAnswer;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.QueriesCommunicateInit;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.TravelTime;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages.WithCertificateCommunicate;
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
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.UpdateRemoteZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;
import pl.edu.mimuw.cloudatlas.common.utils.Utils;
import sun.security.action.GetLongAction;

public class GossipModule extends Module {
	private final CloudatlasAgentConfig config;
	private final Address zmiKeeperAddress;
	private final Address queryKeeperAddress;
	private final List<InetAddress> fallbackContacts = new ArrayList<InetAddress>();
	private final Random random = new Random();
	private final CommunicateSerializer communicateSerializer = new CommunicateSerializer();

	private Address socketModuleAddress;
	private final Address timerModuleAddress;

	// Node data we keep for immediate sending to other nodes.
	private ZmisAttributes cachedAttributes;
	private List<ValueQuery> cachedQueries;

	// Datagram handlers:
	private HashMap<GossipCommunicate.Type, HandleCommunicate<?>> datagramHandlers = new HashMap<GossipCommunicate.Type, GossipModule.HandleCommunicate<?>>();

	private final ContactSelectionStrategy selectionStrategy;

	public GossipModule(Address address, CloudatlasAgentConfig config,
			Address zmiKeeperAddress, Address queryKeeperAddress,
			Address timerModuleAddress) {
		super(address);
		this.config = config;
		this.zmiKeeperAddress = zmiKeeperAddress;
		datagramHandlers.put(Type.ZMIS_FRESHNESS_INIT,
				freshnessInitCommunicateHandler);
		datagramHandlers.put(Type.ZMIS_FRESHNESS_ANSWER,
				freshnessAnswerCommunicateHandler);
		datagramHandlers.put(Type.ZMI, zmiCommunicateHandler);
		datagramHandlers.put(Type.QUERIES_INIT, queriesCommunicateInitHandler);
		datagramHandlers.put(Type.QUERIES_ANSWER,
				queriesCommunicateAnswerHandler);
		this.timerModuleAddress = timerModuleAddress;
		this.queryKeeperAddress = queryKeeperAddress;
		if (config.getFallbackAddress() != null)
			fallbackContacts.add(config.getFallbackAddress());
		this.selectionStrategy = ContactSelectionStrategy.fromType(config.getStrategy());
	}

	public static final int INITIALIZE_MODULE = 1;
	public static final int START_GOSSIP = 2;
	public static final int REFRESH_DATA = 3;

	public static final int ZMI_RECEIVED = 4;
	public static final int QUERIES_RECEIVED = 5;

	private static final int NETWORK_MESSAGE_RECEIVED = 6;

	public static final int SET_FALLBACK_CONTACTS = 7;
	public static final int GET_FALLBACK_CONTACTS = 8;

	private ZmisAttributes getCachedAttributes() throws HandlerException {
		if (cachedAttributes == null) {
			throw new HandlerException("Gossip module not yet initialized");
		}
		return cachedAttributes;
	}

	private List<ValueQuery> getCachedQueries() throws HandlerException {
		if (cachedQueries == null) {
			throw new HandlerException("Gossip module not yet initialized");
		}
		return cachedQueries;
	}

	private final MessageHandler<Message> initializeHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			sendMessage(timerModuleAddress, TimerModule.SCHEDULE_MESSAGE,
					new ScheduleAlarmMessage(100, 0,
							config.getGossipPeriodMs(), getAddress(),
							START_GOSSIP));
			sendMessage(
					timerModuleAddress,
					TimerModule.SCHEDULE_MESSAGE,
					new ScheduleAlarmMessage(0, 1, config
							.getGossipDataRefreshTimeMs(), getAddress(),
							REFRESH_DATA));
			
			sendMessage(socketModuleAddress, SocketModule.INIT, new Message());
		}
	};

	private final MessageHandler<Message> refreshDataHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI,
					new GetMessage(getAddress(), ZMI_RECEIVED));
			sendMessage(queryKeeperAddress, QueryKeeperModule.GET_QUERIES,
					new GetMessage(getAddress(), QUERIES_RECEIVED));
		}
	};

	private final MessageHandler<SimpleMessage<Map<String, ValueQuery>>> queriesReceivedHandler = new MessageHandler<SimpleMessage<Map<String, ValueQuery>>>() {

		@Override
		public void handleMessage(SimpleMessage<Map<String, ValueQuery>> message)
				throws HandlerException {
			cachedQueries = new ArrayList<ValueQuery>(message.getContent()
					.values());

		}
	};

	private final MessageHandler<SimpleMessage<ZmisAttributes>> zmiReceivedHandler = new MessageHandler<SimpleMessage<ZmisAttributes>>() {

		@Override
		public void handleMessage(SimpleMessage<ZmisAttributes> message)
				throws HandlerException {
			cachedAttributes = message.getContent();
		}
	};

	private MessageHandler<Message> startGossipHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			ZmisAttributes zmis = getCachedAttributes();
			ContactResult contact = selectionStrategy.selectContact(zmis);
			InetAddress target = null;
			int level = 1;
			PrivateKey key = null;
			Certificate certificate = null;
			if (contact == null) {
				// TODO: fallback contacts should follow some different logic right now.
				// THERE is no clear information about keys that should be used to sign msg.
				// As such we should add new message type that will not be signed.
				if (fallbackContacts.isEmpty()) {
					throw new HandlerException(
							"GossipModule: empty fallback contacts");
				}
				target = fallbackContacts.get(random.nextInt(fallbackContacts
						.size()));
				level = zmis.getLevels().size() - 1;
			} else {
				target = contact.getContact().getAddress();
				level = contact.getLevel(); // level to target level
				key = config.getZoneCertificationData().get(level - 1).getZmiAuthenticationKey();
				certificate = config.getZoneCertificationData().get(level - 1).getCertificate();
			}
			System.err.println("Gossip on level " + level + " with " + target);
			sendNetworkMessage(new QueriesCommunicateInit(getCachedQueries(),
					level, certificate), target, key);
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

	private MessageHandler<SimpleMessage<List<InetAddress>>> setFallbackContactsHandler = new MessageHandler<SimpleMessage<List<InetAddress>>>() {

		@Override
		public void handleMessage(SimpleMessage<List<InetAddress>> message)
				throws HandlerException {
			fallbackContacts.clear();
			fallbackContacts.addAll(message.getContent());
		}

	};

	private MessageHandler<ReceivedDatagramMessage> receivedNetworkMessageHandler = new MessageHandler<ReceivedDatagramMessage>() {

		@Override
		public void handleMessage(ReceivedDatagramMessage message)
				throws HandlerException {
			GossipCommunicate communicate = communicateSerializer
					.deserialize(message.getContent());
			Type tp = communicate.getType();
			datagramHandlers.get(tp).handleUntyped(communicate,
					message.getSource(), message.getSentTimestampMs(),
					message.getReceivedTimestampMs(), message.getSignature());
		}
	};

	private abstract class HandleCommunicate<T extends GossipCommunicate> {
		public abstract void handle(T communicate, InetAddress source,
				Long sent, Long received, byte[] signature) throws HandlerException;

		@SuppressWarnings("unchecked")
		public void handleUntyped(GossipCommunicate communicate,
				InetAddress source, Long sent, Long received, byte[] signature)
				throws HandlerException {
			T comm = (T) communicate;
			comm.hashCode();
			handle(comm, source, sent, received, signature);
		}
	}

	public byte[] communicateTimestamp(Long timestamp, GossipCommunicate communicate) throws IOException{
		byte[] o = communicateSerializer.serialize(communicate);
		ByteArrayOutputStream prepareStreamByte = new ByteArrayOutputStream();
		DataOutputStream prepareStream = new DataOutputStream(prepareStreamByte);
		prepareStream.writeLong(timestamp);
		prepareStream.write(o);
		byte[] communicateTimestamp = prepareStreamByte.toByteArray();
		return communicateTimestamp;
	}
	
	public Boolean isValidCommunicateTimestamp(Long timestamp, GossipCommunicate communicate, byte[] signature, PublicKey publicKey) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException{
		byte[] communicateTimestamp = communicateTimestamp(timestamp, communicate);
		return SecurityUtils.ifEqualMessages(communicateTimestamp, publicKey, signature);
	}
	
	public Boolean isValidMessage(WithCertificateCommunicate communicate, Long sent,byte[] signature) throws HandlerException {
		ValueKey valueKey = (ValueKey) communicate.getCertificate().getAttributesMap().get("publicKey");
		PublicKey publicKey = valueKey.getValue();
		try {
			if (!isValidCommunicateTimestamp(sent, communicate, signature, publicKey)) {
				System.err.println("Hashed message not equal to signature");
				return false;
			}
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException | IOException e) {
			e.printStackTrace();
			throw new HandlerException(e);
		}
		
		ZoneAuthenticationData zoneAuthenticationData = config.getZoneCertificationData().get(communicate.getGossipLevel()- 1);
		//TODO zmienić kolejność w zoneauthenticationdata
		publicKey = zoneAuthenticationData.getSiblingAuthenticationKey();
		byte[] serializedMessage = KryoUtils.serialize(communicate.getCertificate().getAttributesMap(), KryoUtils.getKryo());
		try {
			if (!SecurityUtils.ifEqualMessages(serializedMessage,publicKey, communicate.getCertificate().getSignature()))
			{
				System.err.println("Wrong public key");
				return false;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException(e);
		}
		return true;
	}
	
	private final HandleCommunicate<QueriesCommunicateInit> queriesCommunicateInitHandler = new HandleCommunicate<QueriesCommunicateInit>() {

		@Override
		public void handle(QueriesCommunicateInit communicate,
				InetAddress source, Long sent, Long received, byte[] signature)
				throws HandlerException { 
			if (!isValidMessage(communicate, sent, signature)) {
				return;
			}
			sendMessage(queryKeeperAddress, QueryKeeperModule.INSTALL_QUERY,
					new InstallQueryMessage(communicate.getQueries()));
			Certificate certificate = config.getZoneCertificationData().get(communicate.getGossipLevel()- 1).getCertificate();
			sendNetworkMessage(new QueriesCommunicateAnswer(getCachedQueries(),
					communicate.getGossipLevel(), certificate), source);
		}
	};

	private final HandleCommunicate<QueriesCommunicateAnswer> queriesCommunicateAnswerHandler = new HandleCommunicate<QueriesCommunicateAnswer>() {

		@Override
		public void handle(QueriesCommunicateAnswer communicate,
				InetAddress source, Long sent, Long received, byte[] signature)
				throws HandlerException {
			if (!isValidMessage(communicate, sent, signature)) {
				return;
			}
			sendMessage(queryKeeperAddress, QueryKeeperModule.INSTALL_QUERY,
					new InstallQueryMessage(communicate.getQueries()));

			ZmisAttributes attrs = getCachedAttributes();
			ZmisFreshness freshness = generateFreshness(attrs, Math.min(
					communicate.getGossipLevel(), attrs.getLevels().size() - 1));
			Certificate certificate = config.getZoneCertificationData().get(communicate.getGossipLevel()-1).getCertificate();
			sendNetworkMessage(new ZmisFreshnessInitCommunicate(freshness,
					new TravelTime(sent, received), certificate, communicate.getGossipLevel()), source);
		}
	};

	private HandleCommunicate<ZmisFreshnessInitCommunicate> freshnessInitCommunicateHandler = new HandleCommunicate<ZmisFreshnessInitCommunicate>() {

		@Override
		public void handle(ZmisFreshnessInitCommunicate communicate,
				InetAddress source, Long sent, Long received, byte[] signature)
				throws HandlerException {
			if (!isValidMessage(communicate, sent, signature)) {
				return;
			}
			ZmisAttributes attrs = getCachedAttributes();
			ZmisFreshness freshness = generateFreshness(attrs, Math.min(
					communicate.getContent().getLevels().size() - 1, attrs
							.getLevels().size() - 1));
			Certificate certificate = config.getZoneCertificationData().get(communicate.getGossipLevel()-1).getCertificate();
			sendNetworkMessage(new ZmisFreshnessAnswerCommunicate(freshness,
					new TravelTime(sent, received), certificate, communicate.getGossipLevel()), source);

			sendZmis(attrs, communicate.getContent(), source,
					communicate.getTime(), new TravelTime(sent, received));
		}
	};

	private HandleCommunicate<ZmisFreshnessAnswerCommunicate> freshnessAnswerCommunicateHandler = new HandleCommunicate<ZmisFreshnessAnswerCommunicate>() {

		@Override
		public void handle(ZmisFreshnessAnswerCommunicate communicate,
				InetAddress source, Long sent, Long received, byte[] signature)
				throws HandlerException {
			if (!isValidMessage(communicate, sent, signature)) {
				return;
			}
			sendZmis(getCachedAttributes(), communicate.getContent(), source,
					communicate.getTime(), new TravelTime(sent, received));
		}
	};

	private HandleCommunicate<ZmiCommunicate> zmiCommunicateHandler = new HandleCommunicate<ZmiCommunicate>() {

		@Override
		public void handle(ZmiCommunicate communicate, InetAddress source,
				Long sent, Long received, byte[] signature) {
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.UPDATE_REMOTE_ZMI,
					new UpdateRemoteZmiMessage(communicate.getPathName(),
							communicate.getAttributes()));
		}
	};

	private void sendZmis(ZmisAttributes zmis, ZmisFreshness otherFreshness,
			InetAddress target, TravelTime fromThisToTarget,
			TravelTime fromTargetToThis) {
		Long rtd = fromTargetToThis.getReceived() - fromThisToTarget.getSent() - (fromThisToTarget.getReceived() - fromTargetToThis.getSent());
		// We want to ensure that thisTimestamp + offset = targetTimestamp
		Long offset = fromThisToTarget.getReceived() - rtd/2 - fromThisToTarget.getSent();
		List<ZmiData<AttributesMap>> toSend = filterNewer(zmis.getContent(),
				otherFreshness, offset);
		PathName targetPath = otherFreshness.getPath();
		for (ZmiData<AttributesMap> single : toSend) {
			if (!single.getPath().isPrefixOf(targetPath)) {
				sendNetworkMessage(
						new ZmiCommunicate(single.getPath(),
								single.getContent()), target);
			}
		}
	}

	private ZmisFreshness generateFreshness(ZmisAttributes zmis, int level) {
		List<ZmiLevel<Long>> resList = new ArrayList<ZmiLevel<Long>>();
		for (ZmiLevel<AttributesMap> l : zmis.getLevels().subList(0, level + 1)) {
			Map<String, Long> timestamps = new HashMap<String, Long>();
			for (Entry<String, AttributesMap> e : l.getZones().entrySet()) {
				try {
					timestamps.put(e.getKey(),
							((ValueTime) e.getValue().get("timestamp"))
									.getValue());
				} catch (Exception exc) {
					// Silently skipping.
				}
			}

			ZmiLevel<Long> freshnessLevel = new ZmiLevel<Long>(
					l.getOurZoneName(), timestamps);
			resList.add(freshnessLevel);
		}
		return new ZmisFreshness(resList);
	}

	private static List<ZmiData<AttributesMap>> filterNewer(
			List<ZmiData<AttributesMap>> myZmis, ZmisFreshness otherFreshness, Long offset) {
		List<ZmiData<AttributesMap>> result = new ArrayList<ZmiData<AttributesMap>>();
		PathName otherPathName = otherFreshness.getPath();
		for (ZmiData<AttributesMap> myAttrs : myZmis) {
			try {
				if (!myAttrs.getPath().levelUp().isPrefixOf(otherPathName)) {
					continue;
				}
				Long myTimestamp = ((ValueTime) myAttrs.getContent().get(
						"timestamp")).getValue();
				Long myTimestampAccordingToOther = myTimestamp + offset;
				Long otherTimestamp = -1l;
				try {
					otherTimestamp = otherFreshness.get(myAttrs.getPath());
				} catch (UnknownZoneException e) {
					otherTimestamp = myTimestampAccordingToOther - 1;
				}
				if (myTimestampAccordingToOther > otherTimestamp) {
					AttributesMap attrs = myAttrs.getContent().clone();
					attrs.addOrChange("timestamp", new ValueTime(myTimestampAccordingToOther));
					result.add(new ZmiData<AttributesMap>(myAttrs.getPath(),
							attrs));
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return result;
	}

	// Nie pierwszy
	// To nie ma znaczenia
	// Nie
	// tO ZLE MIEJSCE
	// NIE
	// TAK
	// JEST ZLE
	private void sendNetworkMessage(GossipCommunicate message,
			InetAddress target, PrivateKey key) {
		byte[] o = communicateSerializer.serialize(message);
		List<ZoneAuthenticationData> zoneCertificationData = config.getZoneCertificationData();
		// Pracujesz nad kodem
		// Masz wiedziec co sie w nim dzieje
		// Jak nie wiesz to pytasz
		// Ale sugeruje wczesniej zobaczyc jakie sa pola
		sendMessage(socketModuleAddress, SocketModule.SEND_MESSAGE,
				new SendDatagramMessage(o, target, config.getPort(), key));
	}
	
	private void sendNetworkMessage(GossipCommunicate message,
			InetAddress target) {
		sendNetworkMessage(message, target, null); //zeby sie kompilowalo.
	}

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { INITIALIZE_MODULE, START_GOSSIP,
				REFRESH_DATA, ZMI_RECEIVED, QUERIES_RECEIVED,
				NETWORK_MESSAGE_RECEIVED, SET_FALLBACK_CONTACTS,
				GET_FALLBACK_CONTACTS }, new MessageHandler<?>[] {
				initializeHandler, startGossipHandler, refreshDataHandler,
				zmiReceivedHandler, queriesReceivedHandler,
				receivedNetworkMessageHandler, setFallbackContactsHandler,
				getFallbackContactsHandler });
	}

	@Override
	public List<Module> getSubModules(AddressGenerator generator) {
		socketModuleAddress = generator.getUniqueAddress();
		return Arrays.asList((Module) new SocketModule(socketModuleAddress,
				config.getPort(), config.getMaxMessageSizeBytes(),
				getAddress(), NETWORK_MESSAGE_RECEIVED, timerModuleAddress));
	}

}
