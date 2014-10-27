package test;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;

public class ExpressionInterpreterTest extends InterpreterTest {
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
	
	private void i(String expression, long result) {
		r(expression, new ValueInt(result));
	}
	

	private void d(String expression, double result) {
		r(expression, new ValueDouble(result));
	}
	

	private void s(String expression, String result) {
		r(expression, new ValueString(result));
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
		
		i("1 + 1", 2);
		i("1 - 1", 0);
		i("3 * 4", 12);
		i("4 % 3", 1);
		
		
		// Division returns double
		d("4 / 3", 4. / 3.);
		
		// We allow to add strings
		s("\"ab\" + \" \" + \"cd\"", "ab cd");
		
		t("true AND true");
		f("true AND false");
		
		t("true OR false");
		f("false OR false");
		
		
		return tests;
	}

}
