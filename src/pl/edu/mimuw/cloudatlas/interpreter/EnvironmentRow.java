package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

public class EnvironmentRow extends Environment {
	private final TableRow row;

	public EnvironmentRow(TableRow row, List<String> columns) {
		super(columns);
		this.row = row;
	}

	@Override
	public Result getIdent(String ident) {
		try {
			Value value = row.getIth(columns.get(ident));
			return new ResultSingle(value);
		} catch (NullPointerException exception) {
			return new ResultSingle(ValueNull.getInstance());
		}
	}
}
