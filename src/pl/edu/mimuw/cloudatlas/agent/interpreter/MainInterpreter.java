package pl.edu.mimuw.cloudatlas.agent.interpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.agent.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public class MainInterpreter {
	@SuppressWarnings("serial")
	public static class QueryInstallException extends Exception {

		public QueryInstallException(String string) {
			super(string);
		}
		
	}

	public static void main(String[] args) throws UnknownHostException, ParseException {
		ZMI root = Main.createTestHierarchy();
		readAndInstallProgram(root, System.in);
		runAllQueries(root);
		printZMIs(root);
	}
	
	
	private static void runAllQueries(ZMI zmi) {
		if ( zmi.getSons().isEmpty() ) {
			return;
		}
		for ( ZMI son : zmi.getSons() ) {
			runAllQueries(son);
		}
		List<Entry<Attribute, Value>> queries = new ArrayList<Entry<Attribute, Value>>();
		for ( Entry<Attribute, Value> entry : zmi.getAttributes() ){
			if ( Attribute.isQuery(entry.getKey())) {
				queries.add(entry);
			}
		}
		for ( Entry<Attribute, Value> entry : queries ) {
				try {
					Interpreter interpreter = new Interpreter(zmi);
					Program program = parseProgram(((ValueQuery) entry.getValue()).getValue());		
					List<QueryResult> result = interpreter.interpretProgram(program);
					for(QueryResult r : result) {
						zmi.getAttributes().addOrChange(r.getName(), r.getValue());
					}
				} catch ( Exception e) {
					System.err.println(String.format("Query %s failed: %s", entry.getKey().getName(), e.getMessage()));
				}
			
		}
	}


	public static void readAndInstallProgram(ZMI root, InputStream input) {
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\\n");
		int line = 0;
		while(scanner.hasNext()) {
			++line;
			try {
				tryInstallQuery(root, scanner.next());
			} catch (QueryInstallException e) {
				System.err.println(String.format("In line %d: %s", line, e.getMessage()));
				System.err.println(String.format("In line %d: skipping query", line));
			}
		}
		scanner.close();

	}

	
	private static Program parseProgram(String program) throws Exception {
		Yylex lex = new Yylex(new ByteArrayInputStream(program.getBytes()));
		return new parser(lex).pProgram();
	}

	private static void tryInstallQuery(ZMI root, String query) throws QueryInstallException {
		if ( !query.startsWith("&")) {
			throw new QueryInstallException("Query name should start with &, skipping.");
		}
		int queryNameEnd = query.indexOf(':');
		if ( queryNameEnd < 0 ) {
			throw new QueryInstallException("Query name not terminated.");
		}
		String name = query.substring(1, queryNameEnd);
		String queryContent = query.substring(queryNameEnd + 1);
		if ( name.isEmpty() ) {
			throw new QueryInstallException("Query name should not be empty");
		}
		try {
			parseProgram(queryContent);
		} catch (Exception e) {
			throw new QueryInstallException("Could not parse query: " + e.getMessage());
		}
		installQuery(root, name, queryContent);
		
	}
	
	private static void installQuery(ZMI zmi, String queryName, String query) {
		if ( zmi.getSons().isEmpty() )
			return;
		zmi.getAttributes().addOrChange("&" + queryName, new ValueQuery(query));
		for ( ZMI son : zmi.getSons() ) {
			installQuery(son, queryName, query);
		}
	}
	
	public static void printZMIs(ZMI root) {
		System.out.println(Main.getPathName(root));
		root.printAttributes(System.out);
		for ( ZMI son : root.getSons()) {
			printZMIs(son);
		}
	}
	

}
