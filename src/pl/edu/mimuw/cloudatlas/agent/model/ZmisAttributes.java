package pl.edu.mimuw.cloudatlas.agent.model;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class ZmisAttributes extends
		SingleMachineZmiData<AttributesMap> {

	public ZmisAttributes(List<ZmiLevel<AttributesMap>> levels) {
		super(levels);
	}

	@Override
	public ZmisAttributes clone(Cloner<AttributesMap> cloner) {
		SingleMachineZmiData<AttributesMap> tmp = super.clone(cloner);
		return new ZmisAttributes(tmp.getLevels());
	}
}
