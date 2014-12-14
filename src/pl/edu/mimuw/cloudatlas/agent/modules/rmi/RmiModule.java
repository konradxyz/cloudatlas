package pl.edu.mimuw.cloudatlas.agent.modules.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.GetRootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public final class RmiModule extends Module {
	private final Address zmiKeeper;
	private final int port;
	private Registry registry;
	private CloudatlasAgentRmiServer stub;
	
	private ZMI zmi = null;
	private final Object zmiLock = new Object();

	public RmiModule(Address address, Address zmiKeeper, int port) {
		super(address);
		this.zmiKeeper = zmiKeeper;
		this.port = port;
	}
	
	private final static int ROOT_ZMI = 1;
	
	private CloudatlasAgentRmiServer server = new CloudatlasAgentRmiServer() {
		
		@Override
		public void setCurrentNodeAttribute(String attribute, Value value)
				throws RemoteException {
			sendMessage(zmiKeeper, ZmiKeeperModule.SET_ATTRIBUTE,
					new SetAttributeMessage(new Attribute(attribute), value));

		}

		@Override
		public ZMI getRootZmi() throws RemoteException {
			sendMessage(zmiKeeper, ZmiKeeperModule.GET_ROOT_ZMI,
					new GetRootZmiMessage(getAddress(), ROOT_ZMI));
			ZMI result = null;
			synchronized(zmiLock) {
				zmi = null;
				while ( zmi == null ) {
					try {
						zmiLock.wait();
					} catch (InterruptedException e) {
						return null;
					}
				}
				result = zmi;
				zmi = null;
			}
			return result;
		}
	};
	
	private final MessageHandler<RootZmiMessage> rootZmiMessageReceivedHandler = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			synchronized(zmiLock) {
				zmi = message.getContent();
				zmiLock.notifyAll();
			}
			
		}
		
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { ROOT_ZMI },
				new MessageHandler<?>[] { rootZmiMessageReceivedHandler });
	}	
	
	@Override
	public void initialize() throws ModuleInitializationException {
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
}
