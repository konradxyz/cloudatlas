package pl.edu.mimuw.cloudatlas.agent.modules.main;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.GetMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.example.PrinterModule;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.example.ReaderModule;
import pl.edu.mimuw.cloudatlas.agent.modules.gossip.GossipModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.QueryKeeperModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.RecalculateZmisMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.query.ZmiRecalculatedMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.rmi.RmiModule;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.AlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.TimerModule;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.UpdateLocalZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;

public class MainModule extends Module {
	private Address shutdownAddress;
	private Address printerAddress;
	private Address zmiKeeperAddress;
	private Address timerAddress;
	private Address queryKeeperAddress;
	private Address gossipAddress;

	private final CloudatlasAgentConfig config;

	protected static final Integer ALARM_RECEIVED = 3;

	protected static final Integer RECALCULATED_ZMI = 4;
	protected static final Integer ZMI_RECEIVED_FOR_RECALCULATION = 5;
	public static final Integer INITIALIZE = 6;

	private final MessageHandler<SimpleMessage<String>> readHandler = new MessageHandler<SimpleMessage<String>>() {
	
		@Override
		public void handleMessage(SimpleMessage<String> message) throws HandlerException {
			sendMessage(printerAddress, PrinterModule.PRINT_LINE, message);
			if ( message.getContent().equals("quit") ) {
				sendMessage(shutdownAddress, ShutdownModule.INITIALIZE_SHUTDOWN, new Message());
			}
			if ( message.getContent().equals("dump")) {
				sendMessage(queryKeeperAddress, QueryKeeperModule.DUMP, new Message());
			}
		}
	};
	
	private final MessageHandler<AlarmMessage> alarmHandler = new MessageHandler<AlarmMessage>() {

		@Override
		public void handleMessage(AlarmMessage message)
				throws HandlerException {
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI,
					new GetMessage(getAddress(),
							ZMI_RECEIVED_FOR_RECALCULATION));
		}
	};

	private final MessageHandler<RootZmiMessage> rootZmiHandlerForRecalc = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			sendMessage(queryKeeperAddress, QueryKeeperModule.RECALCULATE_ZMI,
					new RecalculateZmisMessage(message.getContent(),
							getAddress(), RECALCULATED_ZMI));
		}

	};

	private final MessageHandler<ZmiRecalculatedMessage> recalcZmiHandler = new MessageHandler<ZmiRecalculatedMessage>() {

		@Override
		public void handleMessage(ZmiRecalculatedMessage message)
				throws HandlerException {
			sendMessage(
					zmiKeeperAddress,
					ZmiKeeperModule.UPDATE_LOCAL_ZMI,
					new UpdateLocalZmiMessage(message.getPathName(), message
							.getMap()));
		}

	};

	private final MessageHandler<Message> initializeHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			sendMessage(gossipAddress, GossipModule.INITIALIZE_MODULE,
					new Message());
			sendMessage(timerAddress, TimerModule.SCHEDULE_MESSAGE, new ScheduleAlarmMessage(0, 0, 1000, getAddress(), ALARM_RECEIVED));
			sendMessage(zmiKeeperAddress, ZmiKeeperModule.INIT, new Message());

		}
	};

	public MainModule(CloudatlasAgentConfig config, Address uniqueAddress,
			Address shutdownModuleAddress) {
		super(uniqueAddress);
		this.shutdownAddress = shutdownModuleAddress;
		this.config = config;
	}

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		handlers.put(ReaderModule.LINE_READ, readHandler);
		handlers.put(ALARM_RECEIVED, alarmHandler);
		handlers.put(ZMI_RECEIVED_FOR_RECALCULATION, rootZmiHandlerForRecalc);
		handlers.put(RECALCULATED_ZMI, recalcZmiHandler);
		handlers.put(INITIALIZE, initializeHandler);
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

		TimerModule timeModule = new TimerModule(generator.getUniqueAddress());
		timerAddress = timeModule.getAddress();
		modules.add(timeModule);
		
		ZmiKeeperModule zmiKeeper = new ZmiKeeperModule(generator.getUniqueAddress(), 
				config, timerAddress);
		zmiKeeperAddress = zmiKeeper.getAddress();
		modules.add(zmiKeeper);

		

		QueryKeeperModule queryKeeper;
		try {
			queryKeeper = new QueryKeeperModule(
					generator.getUniqueAddress(), config);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
		queryKeeperAddress = queryKeeper.getAddress();
		modules.add(queryKeeper);


		GossipModule gossip = new GossipModule(generator.getUniqueAddress(),
				config, zmiKeeperAddress, queryKeeperAddress, timerAddress);
		gossipAddress = gossip.getAddress();
		modules.add(gossip);

		
		RmiModule rmiModule = new RmiModule(generator.getUniqueAddress(),
				zmiKeeperAddress, queryKeeperAddress, gossipAddress, config);
		modules.add(rmiModule);

		
		return modules;
	}
}
