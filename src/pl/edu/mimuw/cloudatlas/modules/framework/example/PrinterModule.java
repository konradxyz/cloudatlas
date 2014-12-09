package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class PrinterModule extends Module {
	public PrinterModule(Address address) {
		super(address);
	}

	public static final int PRINT_LINE = 0;

	private final MessageHandler<SimpleMessage<String>> printerHandler = new MessageHandler<SimpleMessage<String>>() {

		@Override
		public void handleMessage(SimpleMessage<String> message) {
			System.out.println(message.getContent());

		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		handlers.put(PRINT_LINE, printerHandler);
		return handlers;
	}
}
