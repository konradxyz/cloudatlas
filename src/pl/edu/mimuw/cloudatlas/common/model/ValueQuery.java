package pl.edu.mimuw.cloudatlas.common.model;

public class ValueQuery extends ValueSimple<String> {
	private final String name;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5647899248337090676L;

	public ValueQuery(String name, String value) {
		super(value);
		this.name = name;
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
		return new ValueQuery(null, null);
	}

	public String getName() {
		return name;
	}

}
