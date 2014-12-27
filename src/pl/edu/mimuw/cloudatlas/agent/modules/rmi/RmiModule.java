package pl.edu.mimuw.cloudatlas.agent.modules.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.GetRootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public final class RmiModule extends Module {
	private final Address zmiKeeper;
	private final int port;
	private Registry registry;
	private CloudatlasAgentRmiServer stub;
	private final CloudatlasAgentConfig config;
	
	private final List<Wrapper<ZmisAttributes>> waitingForZmis = new ArrayList<Wrapper<ZmisAttributes>>();

	public RmiModule(Address address, Address zmiKeeper, int port, CloudatlasAgentConfig config) {
		super(address);
		this.zmiKeeper = zmiKeeper;
		this.port = port;
		this.config = config;
	}
	
	private final static int ROOT_ZMI = 1;
	private final static int RMI_GET_ROOT_ZMI = 2;
	
	private final static int RMI_TIMEOUT_MS = 2000;
	
	private CloudatlasAgentRmiServer server = new CloudatlasAgentRmiServer() {
		
		@Override
		public void setCurrentNodeAttribute(String attribute, Value value)
				throws RemoteException {
			sendMessage(zmiKeeper, ZmiKeeperModule.SET_ATTRIBUTE,
					new SetAttributeMessage(new Attribute(attribute), value));

		}

		@Override
		public ZmisAttributes getRootZmi() throws RemoteException {
			Wrapper<ZmisAttributes> wrapper = new Wrapper<ZmisAttributes>();
			synchronized(wrapper) {
				sendMessage(getAddress(), RMI_GET_ROOT_ZMI, new SimpleMessage<Wrapper<ZmisAttributes>>(wrapper));
				try {
					wrapper.wait(RMI_TIMEOUT_MS);
				} catch (InterruptedException e) {
					throw new RemoteException();
				}
				return wrapper.object;
			}
		}

		@Override
		public CloudatlasAgentConfig getConfig() throws RemoteException {
			return config;
		}
	};
	
	private final MessageHandler<RootZmiMessage> rootZmiMessageReceivedHandler = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			for ( Wrapper<ZmisAttributes> wr : waitingForZmis ) {
				synchronized(wr) {
					wr.object = message.getContent().clone();
					wr.notify();
				}
			}
			waitingForZmis.clear();
		}
	};
	
	private final MessageHandler<SimpleMessage<Wrapper<ZmisAttributes>>> rmiGetZmiHandler = new MessageHandler<SimpleMessage<Wrapper<ZmisAttributes>>>() {

		@Override
		public void handleMessage(SimpleMessage<Wrapper<ZmisAttributes>> message)
				throws HandlerException {
			waitingForZmis.add(message.getContent());
			sendMessage(zmiKeeper, ZmiKeeperModule.GET_ROOT_ZMI,
					new GetRootZmiMessage(getAddress(), ROOT_ZMI));
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { ROOT_ZMI, RMI_GET_ROOT_ZMI },
				new MessageHandler<?>[] { rootZmiMessageReceivedHandler, rmiGetZmiHandler });
	}	
	
	@Override
	public void initialize() throws ModuleInitializationException {
		System.setProperty("java.security.policy", "file:./agent.policy");
		System.setProperty("java.rmi.server.hostname", config.getAddress().getHostAddress());

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			stub = 
					(CloudatlasAgentRmiServer) UnicastRemoteObject.exportObject(server, 0);
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("cloudatlas", stub);
		} catch (RemoteException e) {
			throw new ModuleInitializationException(e);
		}
	}

	@Override
	public void shutdown() {
		// WORST IDEA EVER:
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
				System.exit(0);
			}
		}).start();
	}
	
	private static class Wrapper<T> {
		T object;
	}
}
