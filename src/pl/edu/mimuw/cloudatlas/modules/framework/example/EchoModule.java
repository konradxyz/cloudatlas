package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class EchoModule extends Module {
	private ShutdownModule shutdownModule;
	private Address shutdownAddress;
	private Address printerAddress;


	private final MessageHandler<SimpleMessage<String>> readHandler = new MessageHandler<SimpleMessage<String>>() {

	
		@Override
		public void handleMessage(SimpleMessage<String> message) {
			sendMessage(printerAddress, PrinterModule.PRINT_LINE, message);
			if ( message.getContent().equals("quit") ) {
				sendMessage(shutdownAddress, ShutdownModule.INITIALIZE_SHUTDOWN, new Message());
			}
			
		}
	};

	public EchoModule(Address uniqueAddress, ShutdownModule shutdownModule) {
		super(uniqueAddress);
		this.shutdownModule = shutdownModule;
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
		
		modules.add(shutdownModule);
		shutdownAddress = shutdownModule.getAddress();
		
		PrinterModule printer = new PrinterModule(generator.getUniqueAddress());
		printerAddress = printer.getAddress();
		modules.add(printer);
		
		ReaderModule reader = new ReaderModule(generator.getUniqueAddress(), getAddress());
		modules.add(reader);
		
		
		return modules;
	}
}
