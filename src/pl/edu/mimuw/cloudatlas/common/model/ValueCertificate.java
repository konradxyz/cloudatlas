package pl.edu.mimuw.cloudatlas.common.model;

import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ValueCertificate extends ValueSimple<Certificate> {

	public ValueCertificate(Certificate value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNull() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Value convertTo(Type to) {
		return new ValueString(getValue().getAttributesMap().toString());
	}

	@Override
	public Value getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
