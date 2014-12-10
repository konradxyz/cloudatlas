package pl.edu.mimuw.cloudatlas.agent.modules.framework;

public final class Address {
	private Integer value;
	
	public static final Address ANY = new Address();
	
	Address(int val) {
		value = new Integer(val);
	}
	
	Address() {
		value = null;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o == null )
			return false;
		if ( o instanceof Address) {
			Address other = (Address) o;
			if ( value == null ) {
				return other.value == null;
			}
			return value.equals(other.value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}
	
	@Override
	public String toString() {
		return value != null ?  value.toString() : "null";
	}

}
