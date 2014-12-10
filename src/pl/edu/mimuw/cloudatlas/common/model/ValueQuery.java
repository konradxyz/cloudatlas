package pl.edu.mimuw.cloudatlas.common.model;

public class ValueQuery extends ValueSimple<String> {

	public ValueQuery(String value) {
		super(value);
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
		return new ValueQuery(null);
	}

}
