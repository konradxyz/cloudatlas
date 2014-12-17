package pl.edu.mimuw.cloudatlas.agent.modules.framework.example;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.ReceivedDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SendDatagramMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.network.SocketModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.QueryKeeperModule;
import pl.edu.mimuw.cloudatlas.agent.modules.query.RecalculateZmisMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.query.ZmiRecalculatedMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.rmi.RmiModule;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.AlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.timer.TimerModule;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.GetRootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.RootZmiMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.SetAttributeMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.zmi.ZmiKeeperModule;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public class EchoModule extends Module {
	private Address shutdownAddress;
	private Address printerAddress;
	private Address socketAddress;
	private Address zmiKeeperAddress;
	private Address timerAddress;
	private Address queryKeeperAddress;

	private final PathName pathName = new PathName("/I/dont/really/care");

	private static final int RECEIVED_DATAGRAM = 1;
	protected static final Integer ZMI_RECEIVED = 2;
	protected static final Integer ALARM_RECEIVED = 3;

	protected static final Integer RECALCULATED_ZMI = 4;
	protected static final Integer ZMI_RECEIVED_FOR_RECALCULATION = 5;

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
							new SendDatagramMessage(content.getBytes(), target, 5432));
				} catch (UnknownHostException e) {
					throw new HandlerException(e);
				}
				
			}

			if ( content.startsWith("set") ) {
				String attrs[] = content.split(" ");
				sendMessage(zmiKeeperAddress, ZmiKeeperModule.SET_ATTRIBUTE,
						new SetAttributeMessage(new Attribute(attrs[1]),
								new ValueString(attrs[2])));
				return;
			}
			if (content.startsWith("show")) {
				sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI,
						new GetRootZmiMessage(getAddress(), ZMI_RECEIVED));
			}

			if ( content.startsWith("alarm ") ) {
				content = content.substring("alarm ".length());
				int i = 0;
				int period= Integer.parseInt(content.split(" ")[i++]);
				int delay = Integer.parseInt(content.split(" ")[i++]);
				int requestId= Integer.parseInt(content.split(" ")[i++]);
				sendMessage(timerAddress, TimerModule.SCHEDULE_MESSAGE,
						new ScheduleAlarmMessage(delay, requestId, period, getAddress(), ALARM_RECEIVED));
			}
			if (content.startsWith("calc"))
				sendMessage(zmiKeeperAddress, ZmiKeeperModule.GET_ROOT_ZMI,
						new GetRootZmiMessage(getAddress(),
								ZMI_RECEIVED_FOR_RECALCULATION));
		}
	};

	private final MessageHandler<ReceivedDatagramMessage> receiveHandler = new MessageHandler<ReceivedDatagramMessage>() {

		@Override
		public void handleMessage(ReceivedDatagramMessage message)
				throws HandlerException {
			String toPrint = new String(message.getContent());
			sendMessage(printerAddress, PrinterModule.PRINT_LINE,
					new SimpleMessage<String>("received via network:" + toPrint + "\n"));
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

	private final MessageHandler<RootZmiMessage> rootZmiHandler = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			// Well - we should not print in here....
			// But who cares? We are going to delete this code.
			// TODO: remove.
			MainInterpreter.printZMIs(message.getContent());
		}

	};

	private final MessageHandler<RootZmiMessage> rootZmiHandlerForRecalc = new MessageHandler<RootZmiMessage>() {

		@Override
		public void handleMessage(RootZmiMessage message)
				throws HandlerException {
			sendMessage(queryKeeperAddress, QueryKeeperModule.RECALCULATE_ZMI,
					new RecalculateZmisMessage(message.getContent(), pathName,
							getAddress(), RECALCULATED_ZMI));
		}

	};

	private final MessageHandler<ZmiRecalculatedMessage> recalcZmiHandler = new MessageHandler<ZmiRecalculatedMessage>() {

		@Override
		public void handleMessage(ZmiRecalculatedMessage message)
				throws HandlerException {
			System.out.println(message.getPathName());
			message.getMap().printAttributes(System.out);

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
		handlers.put(ZMI_RECEIVED, rootZmiHandler);
		handlers.put(ALARM_RECEIVED, alarmHandler);
		handlers.put(ZMI_RECEIVED_FOR_RECALCULATION, rootZmiHandlerForRecalc);
		handlers.put(RECALCULATED_ZMI, recalcZmiHandler);
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
				5432, 1000, getAddress(), RECEIVED_DATAGRAM);
		modules.add(socket);
		socketAddress = socket.getAddress();
		
		ZmiKeeperModule zmiKeeper = new ZmiKeeperModule(generator.getUniqueAddress(), 
				pathName);
		zmiKeeperAddress = zmiKeeper.getAddress();
		modules.add(zmiKeeper);

		TimerModule timeModule = new TimerModule(generator.getUniqueAddress());
		timerAddress = timeModule.getAddress();
		modules.add(timeModule);

		RmiModule rmiModule = new RmiModule(generator.getUniqueAddress(),
				zmiKeeperAddress, CloudatlasAgentRmiServer.DEFAULT_PORT);
		modules.add(rmiModule);

		QueryKeeperModule queryKeeper = new QueryKeeperModule(
				generator.getUniqueAddress());
		queryKeeperAddress = queryKeeper.getAddress();
		modules.add(queryKeeper);

		return modules;
	}
}
