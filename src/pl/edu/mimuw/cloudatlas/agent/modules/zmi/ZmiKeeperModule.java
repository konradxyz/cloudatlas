package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.agent.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.agent.model.Cloner;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.agent.model.Utils;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;

public final class ZmiKeeperModule extends Module {
	private SingleMachineZmiData<AttributesMap> zmi;
	private AttributesMap currentMachineAttributes;
	private final CloudatlasAgentConfig config;

	public ZmiKeeperModule(Address address, CloudatlasAgentConfig config) {
		super(address);
		assert (!config.getPathName().getComponents().isEmpty());
		this.config = config;
		List<ZmiLevel<AttributesMap>> levels = new ArrayList<ZmiLevel<AttributesMap>>();
		levels.add(fromName(""));
		for (String zoneName : config.getPathName().getComponents()) {
			levels.add(fromName(zoneName));
		}
		zmi = new SingleMachineZmiData<AttributesMap>(new AttributesMap(),
				new Cloner<AttributesMap>() {
					@Override
					public AttributesMap clone(AttributesMap c) {
						return c.clone();
					}
				}, levels);
		Utils.print(zmi, System.err);
		try {
			currentMachineAttributes = zmi.get(config.getPathName());
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw new InternalError();
		}
		currentMachineAttributes.add("cardinality", new ValueInt(1l));
		currentMachineAttributes.add("level", new ValueInt((long) config
				.getPathName().getComponents().size()));
		currentMachineAttributes.add("owner", new ValueString(config
				.getPathName().toString()));
		ValueSet contacts = new ValueSet(TypePrimitive.CONTACT);
		if (config.getAddress() != null)
			contacts.add(new ValueContact(config.getPathName(), config
					.getAddress()));
		currentMachineAttributes.add("contacts", contacts);
		refreshCurrentZmiTimestamp();
	}

	private static ZmiLevel<AttributesMap> fromName(String name) {
		AttributesMap zone = new AttributesMap();
		zone.add("name", new ValueString(name));
		Map<String, AttributesMap> zones = new HashMap<String, AttributesMap>();
		zones.put(name, zone);
		return new ZmiLevel<AttributesMap>(name, zones);
	}

	private void refreshCurrentZmiTimestamp() {
		currentMachineAttributes.addOrChange("timestamp",
				new ValueTime(Calendar.getInstance().getTimeInMillis()));
	}

	public static final int SET_ATTRIBUTE = 1;
	public static final int GET_ROOT_ZMI = 2;
	public static final int UPDATE_LOCAL_ZMI = 3;

	private final MessageHandler<SetAttributeMessage> sendMessageHandler = new MessageHandler<SetAttributeMessage>() {

		@Override
		public void handleMessage(SetAttributeMessage message)
				throws HandlerException {
			assert (currentMachineAttributes != null);
			currentMachineAttributes.addOrChange(
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
							zmi.clone()));
		}
	};

	private final MessageHandler<UpdateLocalZmiMessage> updateLocalZmiHandler = new MessageHandler<UpdateLocalZmiMessage>() {

		@Override
		public void handleMessage(UpdateLocalZmiMessage message)
				throws HandlerException {
			// TODO: assert that only local zones can be updated here.
			PathName path = message.getPath();
			if ( path.equals(config.getPathName())) {
				throw new HandlerException("You can't update whole singleton ZMI");
			}
			try {
				zmi.get(path).swap(message.getAttributes());
			} catch (UnknownZoneException e) {
				throw new HandlerException(e.getMessage());
			}
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { SET_ATTRIBUTE, GET_ROOT_ZMI,
				UPDATE_LOCAL_ZMI }, new MessageHandler<?>[] {
				sendMessageHandler, getRootHandler, updateLocalZmiHandler });
	}
}
