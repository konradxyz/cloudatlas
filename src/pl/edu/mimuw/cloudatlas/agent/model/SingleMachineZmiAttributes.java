package pl.edu.mimuw.cloudatlas.agent.model;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class SingleMachineZmiAttributes extends
		SingleMachineZmiData<AttributesMap> {

	public SingleMachineZmiAttributes(List<ZmiLevel<AttributesMap>> levels) {
		super(levels);
	}

	@Override
	public SingleMachineZmiAttributes clone(Cloner<AttributesMap> cloner) {
		SingleMachineZmiData<AttributesMap> tmp = super.clone(cloner);
		return new SingleMachineZmiAttributes(tmp.getLevels());
	}
}
