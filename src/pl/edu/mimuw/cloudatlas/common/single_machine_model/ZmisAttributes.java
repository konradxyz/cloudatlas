package pl.edu.mimuw.cloudatlas.common.single_machine_model;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class ZmisAttributes extends
		SingleMachineZmiData<AttributesMap> implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4411924869436760831L;

	public ZmisAttributes(List<ZmiLevel<AttributesMap>> levels) {
		super(levels);
	}

	@Override
	public ZmisAttributes clone(Cloner<AttributesMap> cloner) {
		SingleMachineZmiData<AttributesMap> tmp = super.clone(cloner);
		return new ZmisAttributes(tmp.getLevels());
	}
	
	@Override
	public ZmisAttributes clone() {
		return clone(new ZmisAttributesCloner());
	}
	
	private class ZmisAttributesCloner implements Cloner<AttributesMap> {

		@Override
		public AttributesMap clone(AttributesMap c) {
			return c.clone();
		}
		
	}
	
	public void print(PrintStream output) {
		for (ZmiData<AttributesMap> zmi : this.getContent()) {
			output.println(zmi.getPath());
			zmi.getContent().printAttributes(output);
		}
	}
}
