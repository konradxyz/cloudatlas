package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData;

public class ZmisFreshness extends SingleMachineZmiData<Long>{

	public ZmisFreshness(
			List<ZmiLevel<Long>> levels) {
		super(levels);
	}
}
