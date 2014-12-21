package pl.edu.mimuw.cloudatlas.agent.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

// Whole system ZMIs form a tree.
// However, ZMIs stored by single machine 
// form less complex structure. 
public class SingleMachineZmiData<T> implements Cloneable {
	public static class ZmiLevel<E> {
		private String ourZoneName;
		private Map<String, E> siblingZones; // including our zone.

		public ZmiLevel(String ourZoneName, Map<String, E> siblingZones) {
			this.ourZoneName = ourZoneName;
			this.siblingZones = siblingZones;
		}

		public ZmiLevel<E> clone(Cloner<E> cloner) {
			Map<String, E> zones = new HashMap<String, E>();
			for (Entry<String, E> entry : siblingZones.entrySet()) {
				zones.put(entry.getKey(), cloner.clone(entry.getValue()));
			}
			return new ZmiLevel<E>(ourZoneName, zones);
		}

		public Map<String, E> getZones() {
			return Collections.unmodifiableMap(siblingZones);
		}

		public String getOurZoneName() {
			return ourZoneName;
		}
	}

	private List<ZmiLevel<T>> levels;

	public SingleMachineZmiData(List<ZmiLevel<T>> levels) {
		this.levels = levels;
	}

	public T get(PathName pathName) throws UnknownZoneException {
		List<String> components = pathName.getComponents();
		if (components.isEmpty()) {
			T res = levels.get(0).siblingZones.get("");
			if (res == null) {
				throw new UnknownZoneException(pathName);
			}
			return res;
		}
		int level = 1;
		for (; level < components.size(); ++level) {
			if (!components.get(level - 1)
					.equals(levels.get(level).ourZoneName)) {
				throw new UnknownZoneException(pathName);
			}
		}
		try {
			T result = levels.get(components.size()).siblingZones
					.get(components.get(components.size() - 1));
			if (result == null)
				throw new UnknownZoneException(pathName);
			return result;
		} catch (Exception e) {
			throw new UnknownZoneException(pathName);
		}
	}

	public SingleMachineZmiData<T> clone(Cloner<T> cloner) {
		List<ZmiLevel<T>> newLevels = new ArrayList<ZmiLevel<T>>();
		for (ZmiLevel<T> level : levels) {
			newLevels.add(level.clone(cloner));
		}
		return new SingleMachineZmiData<T>(newLevels);
	}

	public List<ZmiLevel<T>> getLevels() {
		return Collections.unmodifiableList(levels);
	}

	public List<ZmiData<T>> getContent() {
		List<ZmiData<T>> result = new ArrayList<ZmiData<T>>();
		PathName parent = PathName.ROOT;
		result.add(new ZmiData<T>(parent, levels.get(0).getZones().get("")));
		for (int i = 1; i < levels.size(); ++i) {
			for (Entry<String, T> e : levels.get(i).siblingZones.entrySet()) {
				result.add(new ZmiData<T>(parent.levelDown(e.getKey()), e
						.getValue()));

			}
			parent = parent.levelDown(levels.get(i).ourZoneName);
		}

		return result;
	}

	public static class UnknownZoneException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 488136881639519988L;

		public UnknownZoneException(PathName path) {
			super("Unknown zone " + path.getName());
		}

	}
}
