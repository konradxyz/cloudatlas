package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.GetMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.common.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.common.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;

public class QueryKeeperModule extends Module {
	private final PathName machineName;
	private final Cipher verifyCipher;

	public QueryKeeperModule(Address address, CloudatlasAgentConfig config)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException {
		super(address);
		this.machineName = config.getPathName();
		this.verifyCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		this.verifyCipher.init(Cipher.DECRYPT_MODE, config.getSignerKey());
	}

	public final static Integer RECALCULATE_ZMI = 1;
	public final static Integer INSTALL_QUERY = 2;
	public final static Integer GET_QUERIES = 3;
	public final static Integer DUMP = 4;

	private final List<Program> predefinedQueries = new ArrayList<Program>();
	
	private static class QueryWrapper {
		private final Program parsedQuery;
		private final ValueQuery value;
		public QueryWrapper(Program parsedQuery, ValueQuery value) {
			super();
			this.parsedQuery = parsedQuery;
			this.value = value;
		}
		public Program getParsedQuery() {
			return parsedQuery;
		}
		public ValueQuery getValue() {
			return value;
		}
	};

	private final Map<String, QueryWrapper> queries = new HashMap<String, QueryWrapper>();

	private final MessageHandler<InstallQueryMessage> installQueryHandler = new MessageHandler<InstallQueryMessage>() {

		@Override
		public void handleMessage(InstallQueryMessage message)
				throws HandlerException {
			for (ValueQuery q : message.getContent()) {
				try {
					byte[] descr = q.toBytes();
					byte[] hash = SecurityUtils.computeHash(descr);
					byte[] decryptedSignature = verifyCipher.doFinal(q.getSignature());
					if ( Arrays.equals(hash, decryptedSignature)){
						QueryWrapper newWrapper;
						if (q.getValue() != null) {
							Program parsedProgram = MainInterpreter
									.parseProgram(q.getValue());
							newWrapper = new QueryWrapper(parsedProgram, q);
						} else {
							newWrapper = new QueryWrapper(null, q);
						}
						
						if (!queries.containsKey(q.getName())) {
							queries.put(q.getName(), newWrapper);
						} else {
							QueryWrapper old = queries.get(q.getName());
							ValueQuery oldQuery = old.getValue();
							if ( oldQuery.getUniqueId() < q.getUniqueId() ) {
								queries.put(q.getName(), newWrapper);
							} else {
								if ( oldQuery.getUniqueId().equals(q.getUniqueId()) && q.getValue() == null ) {
									queries.put(q.getName(), newWrapper);
								}
							}
						}
					} else {
						System.err.println("Wrong query signature " + q.description());
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

	};

	private final MessageHandler<RecalculateZmisMessage> recalculateHandler = new MessageHandler<RecalculateZmisMessage>() {

		@Override
		public void handleMessage(RecalculateZmisMessage message)
				throws HandlerException {
			SingleMachineZmiData<AttributesMap> data = message.getRootZmi();
			PathName current = PathName.ROOT;
			List<PathName> paths = new ArrayList<PathName>();
			paths.add(current);
			for (int i = 1; i < data.getLevels().size(); ++i) {
				current = current.levelDown(data.getLevels().get(i)
						.getOurZoneName());
				paths.add(current);
			}
			for (int i = data.getLevels().size() - 2; i >= 0; --i) {
				ZMI parent = new ZMI();
				for (AttributesMap m : data.getLevels().get(i + 1).getZones()
						.values()) {
					ZMI son = new ZMI();
					son.getAttributes().swap(m);
					parent.addSon(son);
				}
				ZmiLevel<AttributesMap> parentLevel = data.getLevels().get(i);
				AttributesMap newParentMap = generateNewAttributes(parent, i,
						parentLevel.getOurZoneName());
				sendMessage(message.getTargetAddress(), message.getTargetMessageType(), 
						new ZmiRecalculatedMessage(newParentMap.clone(), paths.get(i)));

				parentLevel.getZones().get(parentLevel.getOurZoneName())
						.swap(newParentMap);

			}
		}

		private AttributesMap generateNewAttributes(ZMI parent, long level,
				String name) {
			AttributesMap newMap = new AttributesMap();
			// Lets copy current name.
			newMap.add("name", new ValueString(name));
			newMap.add("level", new ValueInt(level));
			newMap.add("owner", new ValueString(machineName.getName()));
			for (Program program : predefinedQueries) {
				runQuery(parent, program, newMap);
			}
			for (Entry<String, QueryWrapper> entry : queries.entrySet()) {
				if ( entry.getValue().getParsedQuery() != null ) {
					if (runQuery(parent, entry.getValue().getParsedQuery(), newMap)) {
						newMap.addOrChange(entry.getKey(), entry.getValue()
							.getValue());
					}
				}
			}
			newMap.addOrChange("timestamp", new ValueTime(Calendar
					.getInstance().getTimeInMillis()));
			return newMap;

		}
		
		private boolean runQuery(ZMI parent, Program query, AttributesMap result) {
			try {
				Interpreter interpreter = new Interpreter(parent);
				List<QueryResult> results = interpreter.interpretProgram(query);
				for (QueryResult r : results) {
					result.addOrChange(r.getName(), r.getValue());
				}
				return true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return false;
			}			
		}
	};
	
	private final MessageHandler<GetMessage> getQueriesHandler = new MessageHandler<GetMessage>() {

		@Override
		public void handleMessage(GetMessage message) throws HandlerException {
			Map<String, ValueQuery> result = new HashMap<String, ValueQuery>();
			for ( Entry<String, QueryWrapper> entry : queries.entrySet() ) {
				result.put(entry.getKey(), entry.getValue().getValue());
			}
			sendMessage(message.getResponseTarget(), message.getResponseMessageType(), new SimpleMessage<>(result));
		}
	};
	
	private final MessageHandler<Message> dumpHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			System.err.println(queries);
			
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { RECALCULATE_ZMI, INSTALL_QUERY, GET_QUERIES, DUMP },
				new MessageHandler<?>[] { recalculateHandler, installQueryHandler, getQueriesHandler, dumpHandler});
	}

	@Override
	public void initialize() throws ModuleInitializationException {
		try {
			String[] queries = new String[] {
					"SELECT sum(cardinality) AS cardinality",
					"SELECT to_set(random(5, unfold(contacts))) AS contacts" };
			for (String query : queries) {
				predefinedQueries.add(MainInterpreter.parseProgram(query));
			}
		} catch (Exception e) {
			throw new ModuleInitializationException(e);
		}
	}
}
