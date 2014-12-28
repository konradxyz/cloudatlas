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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.common.utils.Utils;

public final class SocketModule extends Module {
	private final int port;
	private DatagramSocket socket;
	private final int maxMessageSize;
	private int nextMessageId = 0;
	// Messages to be sent by senderThread through socket should be put
	// in toSendQueue. In order to terminate senderThread you need to put
	// finishMessage in this queue.
	// It is ugly solution but it will work for now.
	private final SendDatagramMessage finishMessage = new SendDatagramMessage(null, null,0);
	private BlockingQueue<SendDatagramMessage> toSendQueue = new LinkedBlockingQueue<SendDatagramMessage>();
	private Thread senderThread;

	// receiverThread reads message from socket and sends it to
	// gatewayModuleAddress. Datagram is wrapped in message of type
	// gatewayModuleMessageType.
	private Thread receiverThread;
	private final Address gatewayModuleAddress;
	private final int gatewayModuleMessageType;
	private final int HEADER_SIZE = 12;

	public SocketModule(Address address, int port, int maxMessageSize,
			Address gatewayModule, int gatewayMessageType) {
		super(address);
		this.port = port;
		this.maxMessageSize = maxMessageSize;
		gatewayModuleAddress = gatewayModule;
		gatewayModuleMessageType = gatewayMessageType;
	}

	public static final int SEND_MESSAGE = 1;

	private final MessageHandler<SendDatagramMessage> sendHandler = new MessageHandler<SendDatagramMessage>() {

		@Override
		public void handleMessage(SendDatagramMessage message) throws HandlerException {
			toSendQueue.add(message);
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
					
				} catch (InterruptedException | IOException e) {
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
		public PartialMessage(int partsCount) {
			super();
			this.parts = new ArrayList<byte[]>(Collections.nCopies(
					partsCount, (byte[]) null));
			this.receivedTimestampMs = Utils.getNowMs();
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
			byte[] content = new byte[maxMessageSize + HEADER_SIZE + 1];
			DatagramPacket packet = new DatagramPacket(content, maxMessageSize
					+ HEADER_SIZE + 1);
			Map<MessageId, PartialMessage> incomingMessages = new HashMap<MessageId, PartialMessage>();
			boolean finished = false;
			while (!finished) {
				try {
					socket.receive(packet);
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
							incomingMessages.put(
									key,
									new PartialMessage(packetCount));
						}
						if (incomingMessages.get(key).getParts().size() > packetId) {
							incomingMessages.get(key).getParts().set(packetId,
									unwrappedContent);
						}

						// TODO: this part is slow.
						// Moreover, there is obvious resources leak.
						// We are ok with the first problem. Second one should
						// be fixed.
						int i = 0;
						while (i < incomingMessages.get(key).getParts().size()
								&& incomingMessages.get(key).getParts().get(i) != null)
							i++;
						if (i >= incomingMessages.get(key).getParts().size()) {
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							for (int j = 0; j < incomingMessages.get(key).getParts()
									.size(); ++j) {
								outputStream.write(incomingMessages.get(key).getParts()
										.get(j));
							}
							byte[] msg = outputStream.toByteArray();
							byte[] sentTimestampBytes = Arrays.copyOfRange(msg, 0,  8);
							byte[] realMsg = Arrays.copyOfRange(msg, 8, msg.length);
							
							ByteArrayInputStream inputStream = new ByteArrayInputStream(sentTimestampBytes);
							DataInputStream dataInputStream = new DataInputStream(inputStream);
							Long sent = dataInputStream.readLong();
							sendMessage(gatewayModuleAddress,
									gatewayModuleMessageType,
									new ReceivedDatagramMessage(realMsg,
											key.from, sent, incomingMessages
													.get(key)
													.getReceivedTimestampMs()));
							incomingMessages.remove(key);
						}
					} else {
						System.err
								.println("Received too big datagram or too small datagram- it might have been truncated - skipping");
					}
				} catch (IOException e) {
					finished = Thread.interrupted();
				}
			}
		}

	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { SEND_MESSAGE },
				new MessageHandler<?>[] { sendHandler });
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
