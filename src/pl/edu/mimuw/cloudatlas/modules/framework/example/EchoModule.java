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
import pl.edu.mimuw.cloudatlas.modules.network.SendMessage;
import pl.edu.mimuw.cloudatlas.modules.network.SocketModule;

public class EchoModule extends Module {
	private Address shutdownAddress;
	private Address printerAddress;
	private Address socketAddress;



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
					InetAddress target = InetAddress.getLocalHost();
					if ( content.startsWith("sendto ") ) {
						content = content.substring("sendto ".length());
						String ip = content.split(" ")[0];
						target = InetAddress.getByName(ip);
						content  = content.substring(ip.length() + 1);
					} else {
						content = content.substring("send ".length());
					}
					sendMessage(socketAddress, SocketModule.SEND_MESSAGE,
							new SendMessage(content.getBytes(), target));
				} catch (UnknownHostException e) {
					throw new HandlerException(e);
				}
				
			}
			
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
		
		SocketModule socket = new SocketModule(generator.getUniqueAddress(), 12345);
		modules.add(socket);
		socketAddress = socket.getAddress();
		
		return modules;
	}
}
