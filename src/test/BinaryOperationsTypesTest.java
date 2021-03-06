package test;


public class BinaryOperationsTypesTest extends InterpreterTest {

	@Override
	public void fillTests() {
		// Add string to empty ResultList
		e("first(1, \"miauu\" + unfold(int_set))");
		// Add empty ResultList to string
		e("first(1, unfold(int_set) + \"miauu\")");
		// Add columns of different types.
		// Unfortunately right now there is no way to create empty ResultColumn.
		e("first(1, int_set + num_cores)");
		
		
		ls("(first(2, to_string(num_cores + num_cores)))", new String[]{"6", "6"});
		
		ls("(first(2, to_string(num_cores + num_cores)))", new String[]{"6", "6"});
		
		e("ceil(unfold(int_set))");
		
		
		// With this condition int_set is empty column.
		q("SELECT (-int_set) + \"sasa\" AS sth WHERE size(int_set) > 0", null, true);
		
		
		e("NOT 1");
		e("- true");
		f("NOT true");
		i("- ( 1 + 1)", -2);
		
		ls("first(2, to_string(isNull(int_set)))", new String[]{"true", "false"});
		
		t("isNull(avg(unfold(int_set)))");
	
	}

}
