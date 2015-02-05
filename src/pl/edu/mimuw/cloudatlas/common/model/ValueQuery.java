package pl.edu.mimuw.cloudatlas.common.model;

import java.security.InvalidParameterException;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ValueQuery extends ValueSimple<Certificate> {
	private final String name;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5647899248337090676L;

	public ValueQuery(Certificate value) {
		super(value);
		name = ((ValueString) value.getAttributesMap().get("name")).getValue();
		if ( !isValidQueryName(name) )
			throw new InvalidParameterException("Invalid query name " + name);
	}

	@Override
	public Type getType() {
		return TypePrimitive.QUERY;
	}

	@Override
	public Value convertTo(Type to) {
		switch (to.getPrimaryType()) {
		case QUERY:
			return this;
		case STRING:
			return getValue().getAttributesMap().get("query");

		default:
			throw new UnsupportedConversionException(getType(), to);
		}
	}

	@Override
	public Value getDefaultValue() {
		return ValueNull.getInstance();
	}
	
	public String description() {
		return name + ": " + toString();
	}

	public String getName() {
		return name;
	}
	public static boolean isValidQueryName(String name) {
		return name.startsWith("&") && !name.equals("&");
	}


}
