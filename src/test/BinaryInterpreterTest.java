package test;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;

public class BinaryInterpreterTest extends InterpreterTest {
	private List<TestCase> tests = new ArrayList<TestCase>();
	
	
	// Named r just because...
	// Note that it takes expression, not query
	private void r(String expression, Value v) {
		tests.add(new TestCase("SELECT " + expression + " AS result", v));
	}
	
	
	// Expects that expression will evaluate to false
	private void f(String expression) {
		r(expression, new ValueBoolean(false));
	}
	
	// Expects that expression will evaluate to false
	private void t(String expression) {
		r(expression, new ValueBoolean(true));
	}
	
	@Override
	public List<TestCase> getTests() {
		// Relational operators:
		f("1 < 1");
		t("1.0 < 2.0");
		f("\"m\" < \"a\"");
		
		t("1 <= 1");
		t("1.0 <= 2.0");
		f("\"m\" <= \"a\"");
		
		t("1 = 1");
		f("1.0 = 2.0");
		f("\"m\" = \"a\"");
		
		f("1 <> 1");
		t("1.0 <> 2.0");
		t("\"m\" <> \"a\"");
		
		f("1 > 1");
		f("1.0 > 2.0");
		t("\"m\" > \"a\"");
		
		t("1 >= 1");
		f("1.0 >= 2.0");
		t("\"m\" >= \"a\"");
		
		
		
		
		
		return tests;
	}

}
