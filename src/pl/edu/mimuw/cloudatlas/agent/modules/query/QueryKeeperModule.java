package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.agent.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.agent.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public class QueryKeeperModule extends Module {

	public QueryKeeperModule(Address address) {
		super(address);
	}

	public final static Integer RECALCULATE_ZMI = 1;

	private final List<Program> predefinedQueries = new ArrayList<Program>();

	private final MessageHandler<RecalculateZmisMessage> recalculateHandler = new MessageHandler<RecalculateZmisMessage>() {

		@Override
		public void handleMessage(RecalculateZmisMessage message)
				throws HandlerException {
			PathName machineName = message.getMachineName();
			recalculateZmis(machineName.getComponents(), -1,
					message.getRootZmi(), message.getTargetAddress(),
					message.getTargetMessageType());

		}

		private void recalculateZmis(List<String> path, int parentId,
				ZMI parent, Address address, int messageType) {
			int nextParentId = parentId + 1;
			// If nextParentId == path.size() - 1 then next parent would be our
			// singleton zone.
			// We do not need to recalculate singleton zone's zmi.
			if (parentId >= path.size() - 1)
				return;
			for (ZMI son : parent.getSons()) {
				if (son.getAttributes().get("name")
						.equals(new ValueString(path.get(nextParentId)))) {
					recalculateZmis(path, nextParentId, son, address,
							messageType);
					break;
				}
			}
			AttributesMap map = generateNewAttributes(parent, parentId + 1,
					path);
			parent.setAttributes(map.clone());
			sendMessage(address, messageType, new ZmiRecalculatedMessage(map,
					new PathName(path.subList(0, parentId + 1))));
		}

		private AttributesMap generateNewAttributes(ZMI parent, long level,
				List<String> path) {
			AttributesMap newMap = new AttributesMap();
			// Lets copy current name.
			newMap.add("name", parent.getAttributes().get("name"));
			newMap.add("level", new ValueInt(level));
			newMap.add("owner", new ValueString(new PathName(path).toString()));
			for (Program program : predefinedQueries) {
				Interpreter interpreter = new Interpreter(parent);
				List<QueryResult> result = interpreter
						.interpretProgram(program);
				for (QueryResult r : result) {
					newMap.addOrChange(r.getName(), r.getValue());
				}
			}
			newMap.addOrChange("timestamp", new ValueTime(Calendar
					.getInstance().getTimeInMillis()));
			return newMap;

		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { RECALCULATE_ZMI },
				new MessageHandler<?>[] { recalculateHandler });
	}

	@Override
	public void initialize() throws ModuleInitializationException {
		try {
			predefinedQueries.add(MainInterpreter
					.parseProgram("SELECT sum(cardinality) AS cardinality"));
			// TODO: add contacts query.
		} catch (Exception e) {
			throw new ModuleInitializationException(e);
		}
	}

}