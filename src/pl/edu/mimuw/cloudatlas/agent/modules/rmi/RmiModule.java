package pl.edu.mimuw.cloudatlas.agent.modules.rmi;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.GetMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.GossipModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.InstallQueryMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.query.QueryKeeperModule;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public final class RmiModule extends Module {
	private final Address zmiKeeper;
	private final Address queryKeeper;
	private final Address gossipModule;
	private final int port;
	private Registry registry;
	private CloudatlasAgentRmiServer stub;
	private final CloudatlasAgentConfig config;
	
	private final RmiGetter<ZmisAttributes> zmisGetter;
	private final RmiGetter<Map<String, ValueQuery>> queriesGetter;
	private final RmiGetter<List<InetAddress>> fallbackContactsGetter;
	
	
	public RmiModule(Address address, Address zmiKeeper, Address queryKeeper, Address gossipModule, int port, CloudatlasAgentConfig config) {
		super(address);
		this.zmiKeeper = zmiKeeper;
		this.queryKeeper = queryKeeper;
		this.port = port;
		this.config = config;
		this.gossipModule = gossipModule;
		this.zmisGetter = new RmiGetter<ZmisAttributes>(RMI_GET_ROOT_ZMI, zmiKeeper, ZmiKeeperModule.GET_ROOT_ZMI, ROOT_ZMI);
		this.queriesGetter = new RmiGetter<Map<String, ValueQuery>>(RMI_GET_QUERIES, queryKeeper, QueryKeeperModule.GET_QUERIES, QUERIES);
		this.fallbackContactsGetter = new RmiGetter<List<InetAddress>>(
				RMI_GET_FALLBACK_CONTACTS, gossipModule,
				GossipModule.GET_FALLBACK_CONTACTS, FALLBACK_CONTACTS);
	}
	
	private final static int ROOT_ZMI = 1;
	private final static int RMI_GET_ROOT_ZMI = 2;
	private final static int RMI_GET_QUERIES = 3;
	private final static int QUERIES = 4;
	private final static int RMI_GET_FALLBACK_CONTACTS = 5;
	private final static int FALLBACK_CONTACTS = 6;
	
	
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
			return zmisGetter.rmiGet();
		}

		@Override
		public CloudatlasAgentConfig getConfig() throws RemoteException {
			return config;
		}

		@Override
		public Map<String, ValueQuery> getQueries() throws RemoteException {
			return queriesGetter.rmiGet();
		}

		@Override
		public void installQuery(ValueQuery query)
				throws RemoteException {
			sendMessage(queryKeeper, QueryKeeperModule.INSTALL_QUERY, new InstallQueryMessage(query));	
		}

		@Override
		public List<InetAddress> getFallbackAddresses() throws RemoteException {
			return fallbackContactsGetter.rmiGet();
		}

		@Override
		public void setFallbackAddresses(List<InetAddress> addrs)
				throws RemoteException {
			sendMessage(gossipModule, GossipModule.SET_FALLBACK_CONTACTS, new SimpleMessage<>(addrs));
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> result = new HashMap<Integer, MessageHandler<?>>();
		addHandlersFromGetter(zmisGetter, result);
		addHandlersFromGetter(queriesGetter, result);
		addHandlersFromGetter(fallbackContactsGetter, result);
		return result;
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
	
	private static void addHandlersFromGetter(RmiGetter<?> getter, Map<Integer, MessageHandler<?>> handlers) {
		handlers.put(getter.getReplyMessageType(), getter.getReplyHandler());
		handlers.put(getter.getRmiMessageType(), getter.getRmiMessageHandler());
	}
	
	private class RmiGetter<T> {
		private List<Wrapper<T>> wrappers = new ArrayList<Wrapper<T>>();
		private int rmiMessageType;
		
		private Address realModuleAddress;
		private int realMessageType;
		
		
		private int replyMessageType;
		
		private MessageHandler<SimpleMessage<Wrapper<T>>> rmiMessageHandler = new MessageHandler<SimpleMessage<Wrapper<T>>>() {

			@Override
			public void handleMessage(SimpleMessage<Wrapper<T>> message)
					throws HandlerException {
				wrappers.add(message.getContent());
				sendMessage(realModuleAddress, realMessageType, new GetMessage(getAddress(), replyMessageType));
			}
		};
		
		private MessageHandler<SimpleMessage<T>> replyHandler = new MessageHandler<SimpleMessage<T>>() {

			@Override
			public void handleMessage(SimpleMessage<T> message)
					throws HandlerException {
				for ( Wrapper<T> wr : wrappers ) {
					synchronized(wr) {
						wr.object = message.getContent();
						wr.notify();
					}
				}
				wrappers.clear();
				
			}
		};
		

		public T rmiGet() throws RemoteException {
			Wrapper<T> wrapper = new Wrapper<T>();
			synchronized(wrapper) {
				sendMessage(getAddress(), rmiMessageType, new SimpleMessage<Wrapper<T>>(wrapper));
				try {
					wrapper.wait(RMI_TIMEOUT_MS);
				} catch (InterruptedException e) {
					throw new RemoteException();
				}
				return wrapper.object;
			}
		}


		public int getRmiMessageType() {
			return rmiMessageType;
		}


		public int getReplyMessageType() {
			return replyMessageType;
		}


		public MessageHandler<SimpleMessage<Wrapper<T>>> getRmiMessageHandler() {
			return rmiMessageHandler;
		}


		public MessageHandler<SimpleMessage<T>> getReplyHandler() {
			return replyHandler;
		}


		public RmiGetter(int rmiMessageType, Address realModuleAddress,
				int realMessageType, int replyMessageType) {
			super();
			this.rmiMessageType = rmiMessageType;
			this.realModuleAddress = realModuleAddress;
			this.realMessageType = realMessageType;
			this.replyMessageType = replyMessageType;
		}
	}
	
	private static class Wrapper<T> {
		T object;
	}
}
