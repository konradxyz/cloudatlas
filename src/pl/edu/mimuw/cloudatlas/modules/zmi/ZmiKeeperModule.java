package pl.edu.mimuw.cloudatlas.modules.zmi;

import java.util.Calendar;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;

public final class ZmiKeeperModule extends Module {
	private ZMI rootZmi;
	private PathName currentMachinePathName;
	private ZMI currentMachineZmi;

	public ZmiKeeperModule(Address address, PathName currentMachinePathName) {
		super(address);
		assert (!currentMachinePathName.getComponents().isEmpty());
		this.currentMachinePathName = currentMachinePathName;
		// First we will create ZMIs for path from current singleton zone to
		// root.
		rootZmi = new ZMI();
		rootZmi.getAttributes().add("name", new ValueString(""));
		ZMI parent = rootZmi;
		for (String zoneName : currentMachinePathName.getComponents()) {
			ZMI current = new ZMI(parent);
			current.getAttributes().add("name", new ValueString(zoneName));
			parent.addSon(current);
			parent = current;
		}
		currentMachineZmi = parent;
		refreshCurrentZmiTimestamp();
	}

	private void refreshCurrentZmiTimestamp() {
		currentMachineZmi.getAttributes().addOrChange("timestamp",
				new ValueTime(Calendar.getInstance().getTimeInMillis()));
	}

	public static final int SET_ATTRIBUTE = 1;
	public static final int GET_ROOT_ZMI = 2;

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

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { SET_ATTRIBUTE, GET_ROOT_ZMI },
				new MessageHandler<?>[] { sendMessageHandler, getRootHandler});
	}
}
