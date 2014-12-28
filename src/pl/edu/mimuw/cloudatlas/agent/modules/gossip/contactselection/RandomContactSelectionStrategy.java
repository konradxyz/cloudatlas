package pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public class RandomContactSelectionStrategy extends ContactSelectionStrategy {
	private final RequestsPerLevel requestsCompute;
	private final Random random = new Random();

	public RandomContactSelectionStrategy(RequestsPerLevel requestsCompute) {
		super();
		this.requestsCompute = requestsCompute;
	}

	@Override
	public ContactResult selectContact(ZmisAttributes attrs) {
		List<List<ValueContact>> contacts = getContacts(attrs);
		int last = 0;
		Map<Integer, Integer> levelInRandomUpperBound = new HashMap<Integer, Integer>();
		for (int i = 1; i < contacts.size(); ++i) {
			if (!contacts.get(i).isEmpty()) {
				int count = requestsCompute.computeRequestsCount(i,
						contacts.size() - 1);
				last += count;
				levelInRandomUpperBound.put(i, last);
			}
		}
		if (last == 0)
			return null;
		int selected = random.nextInt(last);
		int resLevel = 1;
		for (Entry<Integer, Integer> e : levelInRandomUpperBound.entrySet()) {
			resLevel = e.getKey();
			if (e.getValue() > selected)
				break;
		}

		ValueContact cont = contacts.get(resLevel).get(
				random.nextInt(contacts.get(resLevel).size()));
		return new ContactResult(cont, resLevel);
	}

}
