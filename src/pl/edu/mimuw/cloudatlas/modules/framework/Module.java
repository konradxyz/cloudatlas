package pl.edu.mimuw.cloudatlas.modules.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Module {
	private Context context;
	private Map<Integer, MessageHandler<?>> handlers;
	private Address address;

	// This function will be called during module initialization.
	// All handlers returned by this function will be associated with their 
	// respective message types. Note that it is possible to define
	// handler that would handle multiple message types.
	protected abstract Map<Integer, MessageHandler<?>> generateHandlers();

	// This function might be redefined in order to allow for custom
	// initialization.
	public void initialize() throws ModuleInitializationException {
	}

	// All resources that were allocated during this object's lifetime should be
	// freed by this function.
	public void shutdown() {
	}
	
	// Every module might depend on submodules.
	// In fact, root module should almost always redefine this function.
	// Otherwise, root would be the only module in particular ModuleFramework.
	// It might be good idea to store addresses of generated modules.
	// It will not be possible to access those addresses from this module
	// at any other moment.
	public List<Module> getSubModules(AddressGenerator generator) {
		return new ArrayList<Module>();
	}

	protected final void sendMessage(Address target, int messageType,
			Message message) {
		context.sendMessage(new MessageWrapper(target, messageType, message,
				getAddress()));
	}	
	
	
	public Module(Address address) {
		this.address = address;
	}
	
	public final Address getAddress() {
		return address;
	}
	
	public final void init(Context ctx) throws ModuleInitializationException {
		context = ctx;
		handlers = generateHandlers();
		initialize();
	}
	
	public final void handleMessage(MessageWrapper wrapper) 
			throws HandlerException {
		MessageHandler<?> handler = handlers.get(wrapper.getMessageType());
		if (handler == null) {
			System.err.println("Undefined handler.");
		}
		assert(handler != null);
		wrapper.getMessage();
		handler.handleUntypedMessage(wrapper.getMessage());
	}
	
	
	// Small helper function.
	public static Map<Integer, MessageHandler<?>> getHandlers(
			Integer[] msgTypes, MessageHandler<?>[] handlers) {
		assert(msgTypes.length == handlers.length);
		Map<Integer, MessageHandler<?>> result = 
				new HashMap<Integer, MessageHandler<?>>();
		for ( int i = 0; i < msgTypes.length; ++i ){
			result.put(msgTypes[i], handlers[i]);
		}
		return result;

	}
}
