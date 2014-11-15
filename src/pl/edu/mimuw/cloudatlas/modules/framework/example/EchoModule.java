package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.modules.ModuleAddresses;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class EchoModule extends Module {

	private final MessageHandler<SimpleMessage<String>> readHandler = new MessageHandler<SimpleMessage<String>>() {

	
		@Override
		public void handleMessage(SimpleMessage<String> message) {
			sendMessage(ModuleAddresses.PRINTER, PrinterModule.PRINT_LINE, message);
			if ( message.getContent().equals("quit") ) {
				sendMessage(ModuleAddresses.SHUTDOWN, ShutdownModule.INITIALIZE_SHUTDOWN, new Message());
			}
			
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		handlers.put(ReaderModule.LINE_READ, readHandler);
		return handlers;
	}

	@Override
	protected Integer getAddress() {
		return ModuleAddresses.ECHO;
	}

}
