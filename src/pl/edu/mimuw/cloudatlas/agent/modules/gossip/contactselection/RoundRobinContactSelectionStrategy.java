package pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;

public class RoundRobinContactSelectionStrategy extends
		ContactSelectionStrategy {
	private int level = 1;

	private static int next(int n, int max) {
		if (n >= max) {
			return 1;
		} else {
			return n + 1;
		}
	}

	@Override
	public ContactResult selectContact(ZmisAttributes attrs) {
		List<ZmiLevel<AttributesMap>> levels = attrs.getLevels();
		if (levels.size() <= 1)
			return null;
		if (level >= levels.size())
			level = levels.size() - 1;
		int oldLevel = level;
		level = next(level, levels.size() - 1);
		List<List<ValueContact>> contacts = getContacts(attrs);
		while (contacts.get(level).isEmpty() && level != oldLevel) {
			level = next(level, levels.size() - 1);
		}
		if (contacts.get(level).isEmpty()) {
			return null;
		} else {
			ValueContact cont = contacts.get(level).get(
					random.nextInt(contacts.get(level).size()));
			return new ContactResult(cont, level);
		}
	}
}
