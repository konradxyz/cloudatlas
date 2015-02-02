package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.TimerModule;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils.SignatureMessage;
import pl.edu.mimuw.cloudatlas.common.utils.Utils;

public final class SocketModule extends Module {
	private final int port;
	private DatagramSocket socket;
	private final int maxMessageSize;
	private int nextMessageId = 0;

	// Messages to be sent by senderThread through socket should be put
	// in toSendQueue. In order to terminate senderThread you need to put
	// finishMessage in this queue.
	private final SendDatagramMessage finishMessage = new SendDatagramMessage(null, null,0, null);
	private BlockingQueue<SendDatagramMessage> toSendQueue = new LinkedBlockingQueue<SendDatagramMessage>();
	private Thread senderThread;

	private Thread receiverThread;
	private final Address gatewayModuleAddress;
	private final int gatewayModuleMessageType;
	private final int HEADER_SIZE = 12;
	
	private final Address timerAddress ;
	
	// Cleanup config:
	private final int packetExpirationMs = 10000;
	private final int packetCleanupPeriodMs = 1000;
	
	private final Map<MessageId, PartialMessage> incomingMessages = new HashMap<MessageId, PartialMessage>();

	public SocketModule(Address address, int port, int maxMessageSize,
			Address gatewayModule, int gatewayMessageType, Address timerAddress) {
		super(address);
		this.port = port;
		this.maxMessageSize = maxMessageSize;
		gatewayModuleAddress = gatewayModule;
		gatewayModuleMessageType = gatewayMessageType;
		this.timerAddress = timerAddress;
	}

	public static final int SEND_MESSAGE = 1;
	private static final int MESSAGE_RECEIVED = 2;
	private static final int CLEANUP = 3;
	public static final int INIT = 4;

	private final MessageHandler<Message> initHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			sendMessage(timerAddress, TimerModule.SCHEDULE_MESSAGE, new ScheduleAlarmMessage(0, 0, packetCleanupPeriodMs, getAddress(), CLEANUP));
		}
	};

	private final MessageHandler<SendDatagramMessage> sendHandler = new MessageHandler<SendDatagramMessage>() {

		@Override
		public void handleMessage(SendDatagramMessage message) throws HandlerException {
			toSendQueue.add(message);
		}
	};
	
	private final MessageHandler<ReceivedDatagramInternalMessage> receivedDatagramHandler = new MessageHandler<ReceivedDatagramInternalMessage>() {

		@Override
		public void handleMessage(ReceivedDatagramInternalMessage message)
				throws HandlerException {
			DatagramPacket packet = message.getContent();
			try {
				if (packet.getLength() <= maxMessageSize + HEADER_SIZE
						&& packet.getLength() > HEADER_SIZE) {
					ByteArrayInputStream stream = new ByteArrayInputStream(
							packet.getData());
					DataInputStream dstream = new DataInputStream(stream);
					int machineMessageId = dstream.readInt();
					int packetId = dstream.readInt();
					int packetCount = dstream.readInt();
					byte[] unwrappedContent = new byte[packet.getLength()
							- HEADER_SIZE];
					dstream.readFully(unwrappedContent);

					MessageId key = new MessageId(packet.getAddress(),
							machineMessageId);
					if (!incomingMessages.containsKey(key)) {
						incomingMessages.put(key, new PartialMessage(
								packetCount, message.getReceivedTimestampMs()));
					}
					if (incomingMessages.get(key).getParts().size() > packetId) {
						incomingMessages.get(key).getParts()
								.set(packetId, unwrappedContent);
					}

					int i = 0;
					while (i < incomingMessages.get(key).getParts().size()
							&& incomingMessages.get(key).getParts().get(i) != null)
						i++;
					if (i >= incomingMessages.get(key).getParts().size()) {
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						for (int j = 0; j < incomingMessages.get(key)
								.getParts().size(); ++j) {
							outputStream.write(incomingMessages.get(key)
									.getParts().get(j));
						}
						byte[] msg = outputStream.toByteArray();
						SignatureMessage signatureMessage = SecurityUtils.divideMessage(msg);
						byte[] signature = signatureMessage.getSignature();
						byte[] messageNoSignature = signatureMessage.getMessage();
						byte[] sentTimestampBytes = Arrays.copyOfRange(messageNoSignature, 0,
								8);
						byte[] realMsg = Arrays.copyOfRange(messageNoSignature, 8, messageNoSignature.length);
						ByteArrayInputStream inputStream = new ByteArrayInputStream(
								sentTimestampBytes);
						DataInputStream dataInputStream = new DataInputStream(
								inputStream);
						Long sent = dataInputStream.readLong();
						System.err.println("rec" + signature.length + " " + realMsg.length);
						sendMessage(gatewayModuleAddress,
								gatewayModuleMessageType,
								new ReceivedDatagramMessage(realMsg, key.from,
										sent, incomingMessages.get(key)
												.getReceivedTimestampMs(), signature));
						incomingMessages.remove(key);
					}
				} else {
					System.err
							.println("Received too big datagram or too small datagram- it might have been truncated - skipping");
				}
			} catch (IOException e) {
				throw new HandlerException(e);
			}
		}
	};
	
	private final MessageHandler<Message> cleanupHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			for (Iterator<MessageId> it = incomingMessages.keySet().iterator(); it
					.hasNext();) {
				MessageId id = it.next();
				if (incomingMessages.get(id).getReceivedTimestampMs()
						+ packetExpirationMs < Utils.getNowMs()) {
					it.remove();
				}
			}
		}
	};


	private final Runnable sender = new Runnable() {

		@Override
		public void run() {
			boolean finished = false;
			while (!finished) {
				try {
					int messageId = nextMessageId;
					nextMessageId++;
					SendDatagramMessage msg = toSendQueue.take();
					if ( msg == finishMessage )
						return;
					ByteArrayOutputStream prepareStreamByte = new ByteArrayOutputStream();
					DataOutputStream prepareStream = new DataOutputStream(prepareStreamByte);
					prepareStream.writeLong(Utils.getNowMs());
					prepareStream.write(msg.getContent());
					byte[] toSend = prepareStreamByte.toByteArray();
					// Podpisywanie
					toSend = SecurityUtils.prependSignature(toSend,
							msg.getPrivateKey());
							
					System.err.println("sending " + toSend.length);
					int packetsCount = (int) Math
							.ceil((double) toSend.length
									/ (double) maxMessageSize);
					
					
					for (int i = 0; i < packetsCount; ++i) {
						ByteArrayOutputStream bstream = new ByteArrayOutputStream(
								HEADER_SIZE + maxMessageSize);
						DataOutputStream stream = new DataOutputStream(bstream);
						stream.writeInt(messageId);
						stream.writeInt(i);
						stream.writeInt(packetsCount);
						int offset = i * maxMessageSize;
						int length = maxMessageSize;
						if (offset + length > toSend.length)
							length = toSend.length - offset;
						stream.write(toSend, offset, length);
						stream.close();
						byte[] data = bstream.toByteArray();
						socket.send(new DatagramPacket(data, data.length,
								msg.getTarget(), msg.getPort()));
					}
					
				} catch (InterruptedException | IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
					// Note that even if we do not receive IOException here
					// we can't assume that our msg was received - it is UDP
					// after all. We expect that higher level protocol
					// will take care of ACKs and eventual retransmissions.
					// As such, we do not notify anyone about failure.
					System.err.println(e.getMessage());
				}
			}
		}

	};

	private static class MessageId {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime
					* result
					+ ((machineMessageId == null) ? 0 : machineMessageId
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageId other = (MessageId) obj;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (machineMessageId == null) {
				if (other.machineMessageId != null)
					return false;
			} else if (!machineMessageId.equals(other.machineMessageId))
				return false;
			return true;
		}

		public MessageId(InetAddress from, Integer machineMessageId) {
			super();
			this.from = from;
			this.machineMessageId = machineMessageId;
		}

		private InetAddress from;
		private Integer machineMessageId;
	}
	
	private static class PartialMessage {
		public PartialMessage(int partsCount, Long received) {
			super();
			this.parts = new ArrayList<byte[]>(Collections.nCopies(
					partsCount, (byte[]) null));
			this.receivedTimestampMs = received;
		}
		public List<byte[]> getParts() {
			return parts;
		}
		public Long getReceivedTimestampMs() {
			return receivedTimestampMs;
		}
		private List<byte[]> parts;
		private Long receivedTimestampMs;
	}

	private final Runnable receiver = new Runnable() {

		@Override
		public void run() {
			boolean finished = false;
			while (!finished) {
				try {
					byte[] content = new byte[maxMessageSize + HEADER_SIZE + 1];
					DatagramPacket packet = new DatagramPacket(content, maxMessageSize
							+ HEADER_SIZE + 1);
					socket.receive(packet);
					sendMessage(getAddress(), MESSAGE_RECEIVED, new ReceivedDatagramInternalMessage(packet, Utils.getNowMs()));
				} catch (IOException e) {
					finished = Thread.interrupted();
				}
			}
		}

	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { SEND_MESSAGE, MESSAGE_RECEIVED, CLEANUP, INIT },
				new MessageHandler<?>[] { sendHandler, receivedDatagramHandler, cleanupHandler, initHandler });
	}

	@Override
	public void initialize() throws ModuleInitializationException {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			throw new ModuleInitializationException(e);
		}
		try {
			senderThread = new Thread(sender);
			senderThread.start();
			receiverThread = new Thread(receiver);
			receiverThread.start();
		} catch (Exception e) {
			socket.close();
			throw new ModuleInitializationException(e);
		}
	}

	@Override
	public void shutdown() {
		toSendQueue.clear();
		toSendQueue.add(finishMessage);
		try {
			senderThread.join();
			receiverThread.interrupt();
			socket.close();
			receiverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			socket.close();
		}
	}
}
