package pl.edu.mimuw.cloudatlas.agent.modules.gossip;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData;

public class ZmisFreshness extends SingleMachineZmiData<Long>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8333037660107878668L;

	public ZmisFreshness(
			List<ZmiLevel<Long>> levels) {
		super(levels);
	}
}
