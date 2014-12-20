package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.agent.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.agent.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.agent.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public class QueryKeeperModule extends Module {
	private PathName machineName;

	public QueryKeeperModule(Address address, PathName machineName) {
		super(address);
		this.machineName = machineName;
	}

	public final static Integer RECALCULATE_ZMI = 1;
	public final static Integer INSTALL_QUERY = 2;

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
			if (!message.getQueryName().startsWith("&")
					|| message.getQueryName().equals("&")) {
				throw new HandlerException("Unallowed query name '"
						+ message.getQueryName() + "'");
			}

			try {
				// TODO: check query correctness.
				Program parsedProgram = MainInterpreter.parseProgram(message
						.getQuery());
				ValueQuery val = new ValueQuery(message.getQuery());
				queries.put(message.getQueryName(), new QueryWrapper(
						parsedProgram, val));
			} catch (Exception e) {
				throw new HandlerException(e);
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
				if (runQuery(parent, entry.getValue().getParsedQuery(), newMap)) {
					newMap.addOrChange(entry.getKey(), entry.getValue()
							.getValue());
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
				e.printStackTrace();
				return false;
			}			
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { RECALCULATE_ZMI, INSTALL_QUERY },
				new MessageHandler<?>[] { recalculateHandler, installQueryHandler });
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
