package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public abstract class InterpreterTest {
	private static Interpreter instance = null;

	public static Interpreter getInstance() {
		if (instance == null) {
			try {
				ZMI root = createTestHierarchy();
				instance = new Interpreter(root);
			} catch (Exception e) {
				fail("could not create test hierarchy");
			}
		}
		return instance;
	}

	private static ValueContact createContact(String path, byte ip1, byte ip2,
			byte ip3, byte ip4) throws UnknownHostException {
		return new ValueContact(new PathName(path),
				InetAddress.getByAddress(new byte[] { ip1, ip2, ip3, ip4 }));
	}

	private static ZMI createTestHierarchy() throws ParseException,
			UnknownHostException {
		ValueContact violet07Contact = createContact("/uw/violet07", (byte) 10,
				(byte) 1, (byte) 1, (byte) 10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte) 10,
				(byte) 1, (byte) 1, (byte) 38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte) 10,
				(byte) 1, (byte) 1, (byte) 39);
		ValueContact whatever01Contact = createContact("/uw/whatever01",
				(byte) 82, (byte) 111, (byte) 52, (byte) 56);
		ValueContact whatever02Contact = createContact("/uw/whatever02",
				(byte) 82, (byte) 111, (byte) 52, (byte) 57);

		List<Value> list;

		ZMI root = new ZMI();
		root.getAttributes().add("level", new ValueInt(0l));
		root.getAttributes().add("name", new ValueString(null));
		root.getAttributes().add("owner", new ValueString("/uw/violet07"));
		root.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 20:10:17.342"));
		root.getAttributes().add("contacts",
				new ValueSet(TypePrimitive.CONTACT));
		root.getAttributes().add("cardinality", new ValueInt(0l));

		ZMI uw = new ZMI(root);
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1l));
		uw.getAttributes().add("name", new ValueString("uw"));
		uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
		uw.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 20:8:13.123"));
		uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		uw.getAttributes().add("cardinality", new ValueInt(0l));

		ZMI pjwstk = new ZMI(root);
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1l));
		pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
		pjwstk.getAttributes().add("owner",
				new ValueString("/pjwstk/whatever01"));
		pjwstk.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 20:8:13.123"));
		pjwstk.getAttributes().add("contacts",
				new ValueSet(TypePrimitive.CONTACT));
		pjwstk.getAttributes().add("cardinality", new ValueInt(0l));

		ZMI violet07 = new ZMI(uw);
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2l));
		violet07.getAttributes().add("name", new ValueString("violet07"));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[] { khaki31Contact, whatever01Contact });
		violet07.getAttributes().add("contacts",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] { violet07Contact, });
		violet07.getAttributes().add("members",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation",
				new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3l));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[] { new ValueString("tola"),
				new ValueString("tosia"), });
		violet07.getAttributes().add("some_names",
				new ValueList(list, TypePrimitive.STRING));
		Set<Value> set = new HashSet<Value>();
		set.add(new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("some_names_sets",
				new ValueSet(set, TypeCollection.computeElementType(set)));
		violet07.getAttributes().add("expiry",
				new ValueDuration(13l, 12l, 0l, 0l, 0l));

		ZMI khaki31 = new ZMI(uw);
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2l));
		khaki31.getAttributes().add("name", new ValueString("khaki31"));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays
				.asList(new Value[] { violet07Contact, whatever02Contact, });
		khaki31.getAttributes().add("contacts",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] { khaki31Contact });
		khaki31.getAttributes().add("members",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation",
				new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_cores", new ValueInt(3l));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[] { new ValueString("agatka"),
				new ValueString("beatka"), new ValueString("celina"), });
		khaki31.getAttributes().add("some_names",
				new ValueList(list, TypePrimitive.STRING));
		set = new HashSet<Value>();
		set.add(new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("some_names_sets",
				new ValueSet(set, TypeCollection.computeElementType(set)));
		khaki31.getAttributes().add("expiry",
				new ValueDuration(-13l, -11l, 0l, 0l, 0l));
		
		khaki31.getAttributes().add("int_set", new ValueSet(TypePrimitive.INTEGER));

		ZMI khaki13 = new ZMI(uw);
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2l));
		khaki13.getAttributes().add("name", new ValueString("khaki13"));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 21:03:00.000"));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("contacts",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] { khaki13Contact, });
		khaki13.getAttributes().add("members",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long) null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("some_names",
				new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long) null));

		ZMI whatever01 = new ZMI(pjwstk);
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2l));
		whatever01.getAttributes().add("name", new ValueString("whatever01"));
		whatever01.getAttributes().add("owner",
				new ValueString("/uw/whatever01"));
		whatever01.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays
				.asList(new Value[] { violet07Contact, whatever02Contact, });
		whatever01.getAttributes().add("contacts",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] { whatever01Contact, });
		whatever01.getAttributes().add("members",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation",
				new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7l));
		list = Arrays.asList(new Value[] { new ValueString("rewrite") });
		whatever01.getAttributes().add("php_modules",
				new ValueList(list, TypePrimitive.STRING));

		ZMI whatever02 = new ZMI(pjwstk);
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2l));
		whatever02.getAttributes().add("name", new ValueString("whatever02"));
		whatever02.getAttributes().add("owner",
				new ValueString("/uw/whatever02"));
		whatever02.getAttributes().add("timestamp",
				new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays
				.asList(new Value[] { khaki31Contact, whatever01Contact, });
		whatever02.getAttributes().add("contacts",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] { whatever02Contact, });
		whatever02.getAttributes().add("members",
				new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation",
				new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_cores", new ValueInt(13l));
		list = Arrays.asList(new Value[] { new ValueString("odbc") });
		whatever02.getAttributes().add("php_modules",
				new ValueList(list, TypePrimitive.STRING));

		return uw;
	}

	public static class TestCase {
		public final String query;
		public final Value result;
		public final boolean expectException;

		public TestCase(String query, Value result) {
			this.query = query;
			this.result = result;
			expectException = false;
		}

		public TestCase(String query, Value result, boolean exceptionExpected) {
			this.query = query;
			this.result = result;
			expectException = exceptionExpected;
		}
	}

	public abstract void fillTests();

	@Test
	public void test() throws Exception {
		Interpreter interpreter = getInstance();
		fillTests();
		for (TestCase c : tests) {
			System.err.println("In query: " + c.query);
			String query = c.query;
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			Exception exception = null;

			List<QueryResult> result = null;
			Program program = new parser(lex).pProgram();
			try {
				result = interpreter.interpretProgram(program);
			} catch (Exception e) {
				exception = e;
			}
			if (c.expectException) {
				if (exception == null) {
					fail("In query '" + c.query + "': exception expected, got " + result.toString());
				}
				System.err.println("OK, expected exception and received exception: " + exception.getMessage());
				continue; // I feel bad about this particular line.
			} else {
				if (exception != null) {
					exception.printStackTrace();
					fail(exception.getStackTrace() + "\n" + query);
				}
			}
			assertEquals(1, result.size());
			if (!c.result.equals(result.get(0).getValue())) {
				throw new AssertionFailedError(String.format(
						"In query %s: expected %s, got %s", query, c.result,
						result.get(0).getValue()));
			}

		}
	}
	
	
	private List<TestCase> tests = new ArrayList<TestCase>();
	
	
	protected void q(String query, Value v, boolean exceptionExpected) {
		tests.add(new TestCase(query, v, exceptionExpected));
	}
	
	// Named r just because...
	// Note that it takes expression, not query
	protected void r(String expression, Value v, boolean exceptionExpected) {
		q("SELECT " + expression + " AS result", v, exceptionExpected);
	}

	protected void r(String expression, Value v) {
		r(expression, v, false);
	}
	
	protected void e(String expression) {
		r(expression, null, true);
	}
	
	// Expects that expression will evaluate to false
	protected void f(String expression) {
		r(expression, new ValueBoolean(false));
	}
	
	// Expects that expression will evaluate to false
	protected void t(String expression) {
		r(expression, new ValueBoolean(true));
	}
	
	protected void i(String expression, long result) {
		r(expression, new ValueInt(result));
	}
	

	protected void d(String expression, double result) {
		r(expression, new ValueDouble(result));
	}
	

	protected void s(String expression, String result) {
		r(expression, new ValueString(result));
	}


	protected void ls(String expression, String[] results) {
		List<Value> expected = new ArrayList<Value>();
		for ( String str : results ) {
			expected.add(new ValueString(str));
		}
		r(expression, new ValueList(expected, TypeCollection.computeElementType(expected)));
	}

	protected void ss(String expression, String[] results) {
		Set<Value> expected = new HashSet<Value>();
		for (String str : results) {
			expected.add(new ValueString(str));
		}
		List<Value> expectedSingletonList = new ArrayList<Value>();
		expectedSingletonList.add(new ValueSet(expected, TypeCollection
				.computeElementType(expected)));
		r(expression,
				new ValueList(expectedSingletonList, TypeCollection
						.computeElementType(expectedSingletonList)));
	}


}
