package pl.edu.mimuw.cloudatlas.common.model;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class ValueQuery extends ValueSimple<String> {
	private final String name;
	private final Long uniqueId;
	private final byte[] signature;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5647899248337090676L;

	// This is tricky - and dirty but gets the job done.
	// There are basically two types of ValueQuery objects.
	// value != null => standard query
	// value == null => uninstall request for all queries with this uniqueId
	public ValueQuery(String name, String value, Long uniqueId, byte[] signature) {
		super(value);
		if ( !isValidQueryName(name) )
			throw new InvalidParameterException("Invalid query name " + name);
		this.name = name;
		this.uniqueId = uniqueId;
		this.signature = Arrays.copyOf(signature, signature.length);
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
			return new ValueString(getValue());

		default:
			throw new UnsupportedConversionException(getType(), to);
		}
	}

	@Override
	public Value getDefaultValue() {
		return ValueNull.getInstance();
	}

	public String getName() {
		return name;
	}
	
	public String description() {
		return name + " (" + uniqueId + "): " + getValue();
	}
	
	public byte[] toBytes() {
		return toBytes(name, getValue(), uniqueId);
	}
	
	public static boolean isValidQueryName(String name) {
		return name.startsWith("&") && !name.equals("&");
	}
	
	public static byte[] toBytes(String name, String query, Long id) {
		return (name + "&" + query + "&" + Long.toHexString(id)).getBytes();
	}

	public byte[] getSignature() {
		return signature;
	}

	public Long getUniqueId() {
		return uniqueId;
	}

}
