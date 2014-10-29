			package test;


public class ExpressionInterpreterTest extends InterpreterTest {
	
	
	@Override
	public void fillTests() {
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
		
		t("\"abcccdee\" REGEXP \"[a-z]*ccc[a-z]*\"");
		f("\"abccdee\" REGEXP \"[a-z]*ccc[a-z]*\"");

		i("count(members)", 3);
		
		String[] firsts = {"tola", "tosia", "agatka"};
		ls("first(3, unfold(some_names))", firsts);
		

		String[] lasts = {"agatka", "beatka", "celina"};
		ls("last(3, unfold(some_names))", lasts);
		
		String[] colFirsts = {"3", "3"};
		ls("first(2, (to_string(num_cores)))", colFirsts);
	
		String[] colLasts = {"3", "NULL"};
		ls("last(2, (to_string(num_cores)))", colLasts);
	
		String[] setFirst = {"tola", "tosia"};
		ss("first(1, to_set(some_names))", setFirst);
		
		ss("first(1, to_set(unfold(some_names_sets)))", setFirst);
		
		ss("first(1, to_set(to_list(to_set(some_names))))", setFirst);
		
		ss("first(1, to_set(to_list(to_set(unfold(some_names_sets)))))", setFirst);

		s("to_string(to_duration(60000))", "+0 00:01:00.000" );
		s("to_string(to_duration(934782378235786))", "+10819240 11:43:55.786" );
		s("to_string(to_duration(-934782378235786))", "-10819240 11:43:55.786" );
		s("to_string(to_duration(0))", "+0" );
	}

}
