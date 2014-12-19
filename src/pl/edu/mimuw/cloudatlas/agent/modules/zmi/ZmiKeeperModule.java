package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public final class ZmiKeeperModule extends Module {
	private ZMI rootZmi;
	private ZMI currentMachineZmi;
	private final CloudatlasAgentConfig config;

	public ZmiKeeperModule(Address address, CloudatlasAgentConfig config) {
		super(address);
		assert (!config.getPathName().getComponents().isEmpty());
		this.config = config;
		// First we will create ZMIs for path from current singleton zone to
		// root.
		rootZmi = new ZMI();
		rootZmi.getAttributes().add("name", new ValueString(""));
		ZMI parent = rootZmi;
		for (String zoneName : config.getPathName().getComponents()) {
			ZMI current = new ZMI(parent);
			current.getAttributes().add("name", new ValueString(zoneName));
			parent.addSon(current);
			parent = current;
		}
		currentMachineZmi = parent;
		currentMachineZmi.getAttributes().add("cardinality", new ValueInt(1l));
		currentMachineZmi.getAttributes().add(
				"level",
				new ValueInt((long) config.getPathName().getComponents()
						.size()));
		currentMachineZmi.getAttributes().add("owner",
				new ValueString(config.getPathName().toString()));
		ValueSet contacts = new ValueSet(TypePrimitive.CONTACT);
		if  (config.getAddress() != null )
			contacts.add(new ValueContact(config.getPathName(), config.getAddress()));
		currentMachineZmi.getAttributes().add("contacts", contacts);
		refreshCurrentZmiTimestamp();
	}

	private void refreshCurrentZmiTimestamp() {
		currentMachineZmi.getAttributes().addOrChange("timestamp",
				new ValueTime(Calendar.getInstance().getTimeInMillis()));
	}

	public static final int SET_ATTRIBUTE = 1;
	public static final int GET_ROOT_ZMI = 2;
	public static final int UPDATE_LOCAL_ZMI = 3;

	private final MessageHandler<SetAttributeMessage> sendMessageHandler = new MessageHandler<SetAttributeMessage>() {

		@Override
		public void handleMessage(SetAttributeMessage message)
				throws HandlerException {
			assert (currentMachineZmi != null);
			currentMachineZmi.getAttributes().addOrChange(
					message.getAttribute(), message.getValue());
			refreshCurrentZmiTimestamp();
		}
	};

	private final MessageHandler<GetRootZmiMessage> getRootHandler = new MessageHandler<GetRootZmiMessage>() {

		@Override
		public void handleMessage(GetRootZmiMessage message)
				throws HandlerException {
			sendMessage(message.getResponseTarget(),
					message.getResponseMessageType(), new RootZmiMessage(
							rootZmi.clone()));
		}
	};

	private final MessageHandler<UpdateLocalZmiMessage> updateLocalZmiHandler = new MessageHandler<UpdateLocalZmiMessage>() {

		@Override
		public void handleMessage(UpdateLocalZmiMessage message)
				throws HandlerException {
			PathName path = message.getPath();
			assertLocalNonSingletonPathName(path);
			ZMI elem = findZmi(path.getComponents());
			if (elem == null) {
				throw new HandlerException("Zone " + path + " not found");
			}
			elem.setAttributes(message.getAttributes());
		}
	};

	private void assertLocalNonSingletonPathName(PathName path)
			throws HandlerException {
		List<String> checked = path.getComponents();
		List<String> local = config.getPathName().getComponents();
		if (checked.size() >= local.size())
			throw new HandlerException("Path " + path
					+ " is not local nonsingleton zmi path");
		for (int i = 0; i < checked.size(); ++i) {
			if (!checked.get(i).equals(local.get(i))) {
				throw new HandlerException("Path " + path
						+ " is not local nonsingleton zmi path");
			}
		}

	}

	private ZMI findZmi(List<String> path) throws HandlerException {
		return findZmi(path, -1, rootZmi);
	}

	private static ZMI findZmi(List<String> path, int parentId, ZMI parent)
			throws HandlerException {
		if (parentId >= path.size() - 1) {
			return parent;
		}
		int nextParentId = parentId + 1;

		String name = path.get(nextParentId);
		for (ZMI son : parent.getSons()) {
			if (son.getAttributes().get("name").equals(new ValueString(name))) {
				return findZmi(path, nextParentId, son);
			}
		}
		return null;
	}

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { SET_ATTRIBUTE, GET_ROOT_ZMI,
				UPDATE_LOCAL_ZMI }, new MessageHandler<?>[] {
				sendMessageHandler, getRootHandler, updateLocalZmiHandler });
	}
}
