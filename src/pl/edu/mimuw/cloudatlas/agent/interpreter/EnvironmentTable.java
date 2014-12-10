package pl.edu.mimuw.cloudatlas.agent.interpreter;

import java.util.Collections;

import pl.edu.mimuw.cloudatlas.common.model.ValueNull;

public class EnvironmentTable extends Environment {
	private Table table;

	public EnvironmentTable(Table table) {
		super(Collections.unmodifiableList(table.getColumns()));
		this.table = table;
	}

	@Override
	public Result getIdent(String ident) {
		try {
			return new ResultColumn(table.getColumn(ident));

		} catch (NullPointerException exception) {
			return new ResultSingle(ValueNull.getInstance());
		}
	}

}
