package pl.edu.mimuw.cloudatlas.agent.modules.gossip.contactselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueContact;
import pl.edu.mimuw.cloudatlas.common.model.ValueSet;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;

public abstract class ContactSelectionStrategy {
	// Returns null if no suitable contact were found.
	public abstract ContactResult selectContact(ZmisAttributes attrs);
	
	protected final Random random = new Random();
	
	public static class ContactResult {
		private ValueContact contact;
		private int level;
		public ValueContact getContact() {
			return contact;
		}
		public int getLevel() {
			return level;
		}
		public ContactResult(ValueContact contact, int level) {
			super();
			this.contact = contact;
			this.level = level;
		}
	}
	
	public static List<List<ValueContact>> getContacts(ZmisAttributes attrs) {
		List<List<ValueContact>> result = new ArrayList<List<ValueContact>>();
		List<ZmiLevel<AttributesMap>> levels = attrs.getLevels();
		for ( int i = 0; i < levels.size(); ++i ) {
			List<ValueContact> conts = new ArrayList<ValueContact>();
			for ( Entry<String, AttributesMap> e : levels.get(i).getZones().entrySet()) {
				try {
					if ( !e.getKey().equals(levels.get(i).getOurZoneName()) ){
						ValueSet s = ((ValueSet)e.getValue().get("contacts"));
						for ( Value v : s) {
							conts.add((ValueContact)v);
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			result.add(conts);
		}
		
		
		return result;
	}
}