package pl.edu.mimuw.cloudatlas.common.interpreter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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

	// TODO: move this function.
	public static Program parseProgram(String program) throws Exception {
		Yylex lex = new Yylex(new ByteArrayInputStream(program.getBytes()));
		return new parser(lex).pProgram();
	}

	public static void printZMIs(ZMI root) {
		printZMIsToStream(root, System.out);
	}

	public static void printZMIsToStream(ZMI root, PrintStream o) {
		o.println(Main.getPathName(root));
		root.printAttributes(o);
		for (ZMI son : root.getSons()) {
			printZMIsToStream(son, o);
		}
	}

}
