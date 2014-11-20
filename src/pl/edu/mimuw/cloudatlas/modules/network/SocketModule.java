package pl.edu.mimuw.cloudatlas.modules.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ModuleInitializationException;

public final class SocketModule extends Module {
	private final int port;
	private DatagramSocket socket;
	
	// Messages to be sent by senderThread through socket.
	// In order to terminate senderThread you need to put finishMessage
	// in this queue.
	// It is ugly solution but it will work for now.
	private final SendMessage finishMessage = new SendMessage(null, null,  0);
	private BlockingQueue<SendMessage> toSendQueue = new LinkedBlockingQueue<SendMessage>();
	private Thread senderThread;
	
	public SocketModule(Address address, int port) {
		super(address);
		this.port = port;
	}
	
	public static final int SEND_MESSAGE = 1;
	
	private final MessageHandler<SendMessage> sendHandler = new MessageHandler<SendMessage>() {
		
		@Override
		public void handleMessage(SendMessage message) {
			toSendQueue.add(message);
		}
	};
	
	private final Runnable sender  = new Runnable() {

		@Override
		public void run() {
			boolean finished = false;
			while (!finished) {
				try {
					SendMessage msg = toSendQueue.take();
					System.err.println("got message");
					finished = msg == finishMessage;
					if ( !finished ) {
						System.err.println("sending");
						socket.send(new DatagramPacket(msg.getContent(), 
								msg.getContent().length, msg.getTarget(), msg.getPort()));
						System.err.println("sent");
					}
				} catch (InterruptedException | IOException e) {
					// Note that even if we do not receive IOException here
					// we can't assume that our msg was received - it is UDP 
					// after all. We expect that higher level protocol
					// will take care of ACKs and eventual retransmissions.
					// As such, we do not notify anyone about failure.
					e.printStackTrace();
				}
			}
		}
		
	};


	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(
				new Integer[]{SEND_MESSAGE}, 
				new MessageHandler<?>[]{sendHandler});
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
			
		} catch(Exception e) {
			socket.close();
			throw new ModuleInitializationException(e);
		}
	}
	
	@Override
	public void shutdown() {
		toSendQueue.add(finishMessage);
		try {
			senderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}
