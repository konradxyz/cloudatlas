package pl.edu.mimuw.cloudatlas.modules.framework;

public final class AddressGenerator {
	private int next = 0;
	
	public Address getUniqueAddress() {
		return new Address(next++);
	}

}
