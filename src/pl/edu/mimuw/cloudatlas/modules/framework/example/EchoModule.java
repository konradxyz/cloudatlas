package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.modules.network.ReceivedDatagramMessage;
import pl.edu.mimuw.cloudatlas.modules.network.SendDatagramMessage;
import pl.edu.mimuw.cloudatlas.modules.network.SocketModule;
import pl.edu.mimuw.cloudatlas.modules.timer.AlarmMessage;
import pl.edu.mimuw.cloudatlas.modules.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.modules.timer.TimerModule;

public class EchoModule extends Module {
	private Address shutdownAddress;
	private Address printerAddress;
	private Address socketAddress;
	private Address timerAddress;
	
	private static final int RECEIVED_DATAGRAM = 1;
	private static final int ALARM_RECEIVED = 2;

	private final MessageHandler<SimpleMessage<String>> readHandler = new MessageHandler<SimpleMessage<String>>() {

	
		@Override
		public void handleMessage(SimpleMessage<String> message) throws HandlerException {
			sendMessage(printerAddress, PrinterModule.PRINT_LINE, message);
			if ( message.getContent().equals("quit") ) {
				sendMessage(shutdownAddress, ShutdownModule.INITIALIZE_SHUTDOWN, new Message());
			}
			String content = message.getContent();			
			if ( content.startsWith("send") ) {
				try {
					InetAddress target = InetAddress.getByName("localhost");
					if ( content.startsWith("sendto ") ) {
						content = content.substring("sendto ".length());
						String ip = content.split(" ")[0];
						target = InetAddress.getByName(ip);
						content  = content.substring(ip.length() + 1);
					} 
					else {
						content = content.substring("send ".length());
					}
					content = content + "\n";
					sendMessage(socketAddress, SocketModule.SEND_MESSAGE,
							new SendDatagramMessage(content.getBytes(), target, 12345));
				} catch (UnknownHostException e) {
					throw new HandlerException(e);
				}
				
			}
			else if ( content.startsWith("alarm ") ) {
				content = content.substring("alarm ".length());
				int i = 0;
				int delay = Integer.parseInt(content.split(" ")[i++]);
				int requestId= Integer.parseInt(content.split(" ")[i++]);
				sendMessage(timerAddress, TimerModule.SCHEDULE_MESSAGE,
						new ScheduleAlarmMessage(delay, requestId));
			} 
			
		}
	};

	private final MessageHandler<ReceivedDatagramMessage> receiveHandler = new MessageHandler<ReceivedDatagramMessage>() {

		@Override
		public void handleMessage(ReceivedDatagramMessage message)
				throws HandlerException {
			String toPrint = new String(message.getContent());
			sendMessage(printerAddress, PrinterModule.PRINT_LINE,
					new SimpleMessage<String>(toPrint + "\n"));
		}
	};
	
	private final MessageHandler<AlarmMessage> alarmHandler = new MessageHandler<AlarmMessage>() {

		@Override
		public void handleMessage(AlarmMessage message)
				throws HandlerException {
			String toPrint = "alarm received "+message.getRequestId();
			sendMessage(printerAddress, PrinterModule.PRINT_LINE,
					new SimpleMessage<String>(toPrint + "\n"));
		}
	};

	public EchoModule(Address uniqueAddress, Address shutdownModuleAddress) {
		super(uniqueAddress);
		this.shutdownAddress = shutdownModuleAddress;
	}

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		handlers.put(ReaderModule.LINE_READ, readHandler);
		handlers.put(RECEIVED_DATAGRAM, receiveHandler);
		handlers.put(ALARM_RECEIVED, alarmHandler);
		return handlers;
	}
	
	@Override
	public List<Module> getSubModules(AddressGenerator generator) {
		List<Module> modules = new ArrayList<Module>();
		
		PrinterModule printer = new PrinterModule(generator.getUniqueAddress());
		printerAddress = printer.getAddress();
		modules.add(printer);
		
		ReaderModule reader = new ReaderModule(generator.getUniqueAddress(), 
				getAddress());
		modules.add(reader);
		
		SocketModule socket = new SocketModule(generator.getUniqueAddress(),
				5432, 60 * 1024, getAddress(), RECEIVED_DATAGRAM);
		modules.add(socket);
		socketAddress = socket.getAddress();
		
		TimerModule timeModule = new TimerModule(generator.getUniqueAddress(), getAddress(), ALARM_RECEIVED);
		timerAddress = timeModule.getAddress();
		modules.add(timeModule);
		
		return modules;
	}
}
