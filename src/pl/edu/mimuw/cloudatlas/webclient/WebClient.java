package pl.edu.mimuw.cloudatlas.webclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ajax.JSON;
import org.ini4j.Config;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.Type.PrimaryType;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueSimple;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasQuerySigner;
import pl.edu.mimuw.cloudatlas.common.rmi.InstallQueryResult;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.webclient.models.QueryModel;
import pl.edu.mimuw.cloudatlas.webclient.models.ZoneModel;

public class WebClient {
	private final String rmiHost;
	private final int rmiPort;
	private final Server jettyServer;
	
	//Plot:
	private final int plotLenghtMs;
	private final int plotRefreshPeriodMs;
	
	private final Map<PrimaryType, ValueToNumberStringConverter<?>> converters = new HashMap<PrimaryType, ValueToNumberStringConverter<?>>();
	
	// Signer:
	private final QuerySignerAddress signer;
	
	private CloudatlasAgentConfig config;

	private Thread keeperUpdaterThread;

	private void fillHeader(ST template) {
		template.add("path", config.getPathName());
		template.add("address", config.getAddress());
	}

	private final PageHandler indexHandler = new PageHandler() {

		@Override
		public void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST template) throws IOException {
			fillHeader(template);
			List<ZoneModel> zones = new ArrayList<ZoneModel>();
			try {
				for (ZmiData<AttributesMap> zone : connect().getRootZmi().getContent()) {
					List<String> attrs = new ArrayList<String>();
					for (Entry<Attribute, Value> entry : zone.getContent()) {
						String plotLink = "";
						Value val = entry.getValue();
						if ( converters.containsKey(val.getType().getPrimaryType()) ) {
							// Dirty:
							String encodedPath = URLEncoder.encode(zone.getPath().toString(), "UTF-8");
							String encodedAttr = URLEncoder.encode(entry.getKey().getName(), "UTF-8");
							String link = "../plot?path=" + encodedPath + "&attribute=" + encodedAttr;
							plotLink = " <a href='" + link + "'>plot</a>";
						}
						attrs.add(entry.getKey() + " : "
								+ entry.getValue().getType() + " = "
								+ entry.getValue() + plotLink);
						
					}
					zones.add(new ZoneModel(
							zone.getPath().getName().equals("") ? "/" : zone
									.getPath().getName(), attrs));
				}
			} catch (RmiConnectionException e) {
				throw new IOException(e);
			}

			template.add("zones", zones);
			response.getOutputStream().print(template.render());
		}

		@Override
		public String getPage() {
			return "index";
		}
	};
	
	private final PageHandler plotHandler = new PageHandler() {

		@Override
		public void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST template) throws IOException {
			fillHeader(template);
			template.add("zone_path", new PathName(request.getParameter("path")).toString());
			template.add("attribute", request.getParameter("attribute"));
			template.add("refresh_period_ms", plotRefreshPeriodMs);
			template.add("length_ms", plotLenghtMs);
			response.getOutputStream().print(template.render());
		}

		@Override
		public String getPage() {
			return "plot";
		}
	};
	
	private final PageHandler queriesHandler = new PageHandler() {
		
		@Override
		public void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST st) throws IOException {
			fillHeader(st);
			CloudatlasAgentRmiServer stub;
			try {
				stub = connect();
			} catch (RmiConnectionException e1) {
				throw new IOException(e1);
			}
			Map<String, ValueQuery> queries = stub.getQueries();
			String message = "&nbsp;";
			if ( "1".equals(request.getParameter("install"))) {
				try {
					String name = request.getParameter("name");
					String query = request.getParameter("query");
					CloudatlasQuerySigner signer = connectSigner();
					InstallQueryResult res = signer.installQuery(name, query);
					switch (res.getStatus() ) {
						case OK:
							ValueQuery q = res.getQuery();
							stub.installQuery(q);
							queries = stub.getQueries();
							break;
					case CONFLICT:
						message = "Could not install query. It conflicts with query " + res.getQuery().description();
						break;
					case INVALID_QUERY:
						message = "Invalid query: " + name + ": " + query;
						break;
					default:
						message = "Internal error.";
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
					message = "Could not install query";
				}
			}
			
			if ("1".equals(request.getParameter("remove"))) {
				try {
					ValueQuery q = queries.get(request.getParameter("name"));
					CloudatlasQuerySigner signer = connectSigner();
					ValueQuery uninstallQuery = signer.uninstallQuery(q);
					if ( uninstallQuery != null ) {
						stub.installQuery(uninstallQuery);
						queries = stub.getQueries();
						message = "Query uninstalled.";
					} else {
						message = "Could not uninstall query. It is possible that this query was uninstalled but this modification has not propagated to all nodes yet.";
					}
				} catch (Exception e) {
					e.printStackTrace();
					message = "Could not uninstall query, cause: " + e.getMessage();
				}
			}
			
			
			st.add("message", message);
			List<QueryModel> queriesModel = new ArrayList<QueryModel>();
			for ( ValueQuery v : queries.values() ){
				if ( v.getValue() != null ) {
					queriesModel.add(new QueryModel(v.description(), v.getName()));
				}
			}
			st.add("queries", queriesModel);
			response.getOutputStream().print(st.render());
		}
		
		@Override
		public String getPage() {
			return "queries";
		}
	};
	
	private final PageHandler fallbackContactsHandler = new  PageHandler() {
		
		@Override
		public void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST st) throws IOException {
			fillHeader(st);
			String message = "&nbsp;";
			try {
				CloudatlasAgentRmiServer stub = connect();
				if ( "1".equals(request.getParameter("update")) ) {
					try {
						String[] contacts = request.getParameter("contacts").split(";");
						List<InetAddress> addrs = new ArrayList<InetAddress>();
						for ( String contact : contacts ) {
							addrs.add(InetAddress.getByName(contact));
						}
						stub.setFallbackAddresses(addrs);
						message = "Fallback contacts updated.";
					} catch (Exception e) {
						message = "Could not update fallback contacts.";
					}		
				}
				
				List<InetAddress> addrs = connect().getFallbackAddresses();
				String contacts = "";
				for ( InetAddress addr : addrs ) {
					if ( !contacts.equals(""))
						contacts = contacts + ";";
					contacts = contacts + addr.getHostAddress();
				}
				st.add("message", message);
				st.add("contacts", contacts);
			} catch (RmiConnectionException e) {
				throw new IOException(e);
			}
			response.getOutputStream().print(st.render());
			
		}
		
		@Override
		public String getPage() {
			return "fallback_contacts";
		}
	};
	
	private final Handler getAttrHandler = new AbstractHandler() {

		@Override
		public void handle(String arg0, Request arg1,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			arg1.setHandled(true);
			response.setContentType("application/json");
			ZmisAttributes attrs = connect().getRootZmi();
			PathName path = new PathName(request.getParameter("path"));
			String attribute = request.getParameter("attribute");
			AttributesMap zmiAttrs;
			try {
				zmiAttrs = attrs.get(path);
			} catch (UnknownZoneException e) {
				throw new ServletException(e);
			}
			Value attributeValue = zmiAttrs.get(attribute);
			ValueTime timestamp = (ValueTime) zmiAttrs.get("timestamp");

			Map<String, String> result = new HashMap<String, String>();
			result.put("value",
					converters.get(attributeValue.getType().getPrimaryType())
							.convertValue(attributeValue));
			result.put("timestamp", timestamp.getValue().toString());
			String output = JSON.toString(result);
			response.getOutputStream().print(output);
		}
	};
	

	public WebClient(String rmiHost, int rmiPort, int port,
			int plotLenghtMs, int plotRefreshPeriodMs, QuerySignerAddress signer) {
		super();
		this.rmiHost = rmiHost;
		this.rmiPort = rmiPort;
		this.plotLenghtMs = plotLenghtMs;
		this.plotRefreshPeriodMs = plotRefreshPeriodMs;
		this.jettyServer = new Server(port);
		ValueDoubleToNumberStringConverter doubleConverter = new ValueDoubleToNumberStringConverter();
		converters.put(PrimaryType.DOUBLE, doubleConverter);
		ValueLongToNumberStringConverter longConverter = new ValueLongToNumberStringConverter();
		converters.put(PrimaryType.INT, longConverter);
		converters.put(PrimaryType.DURATION, longConverter);
		converters.put(PrimaryType.TIME, longConverter);
		this.signer = signer;
	}

	public void initialize() throws RemoteException, RmiConnectionException {

		RedirectRegexRule rule = new RedirectRegexRule();
		rule.setRegex("/");
		rule.setReplacement("/index");
		rule.setTerminating(true);

		RewriteHandler rewriteHandler = new RewriteHandler();
		rewriteHandler.addRule(rule);
		rewriteHandler.setRewritePathInfo(true);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setResourceBase("web");

		List<PageHandler> pageHandlers = Arrays
				.asList(indexHandler, plotHandler, queriesHandler, fallbackContactsHandler);
		for (PageHandler p : pageHandlers)
			p.init();
		
		ContextHandler getAttrHandlerContext = new ContextHandler();
		getAttrHandlerContext.setAllowNullPathInfo(true);
		getAttrHandlerContext.setContextPath("/getattr");
		getAttrHandlerContext.setHandler(this.getAttrHandler);

		HandlerList pageHandlersList = new HandlerList();
		// This is an interesting trick:
		pageHandlersList.setHandlers(pageHandlers.toArray(new PageHandler[0]));

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] {rewriteHandler, resourceHandler,
				pageHandlersList, getAttrHandlerContext, new DefaultHandler() });

		jettyServer.setHandler(handlers);

		CloudatlasAgentRmiServer stub = connect();
		config = stub.getConfig();
	}

	public void run() throws Exception {
		jettyServer.start();
		jettyServer.join();
		keeperUpdaterThread.join();
	}

	private CloudatlasAgentRmiServer connect() throws RmiConnectionException {
		try {
			Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
			CloudatlasAgentRmiServer stub;
			try {
				stub = (CloudatlasAgentRmiServer) registry.lookup("cloudatlas");
			} catch (NotBoundException e) {
				e.printStackTrace();
				throw new ServletException(e);
			}
			return stub;
		} catch (Exception e) {
			throw new RmiConnectionException(e);
		}
	}

	private CloudatlasQuerySigner connectSigner() throws RmiConnectionException {
		try {
			Registry registry = LocateRegistry.getRegistry(signer.getAddress().getHostAddress(), signer.getPort());
			CloudatlasQuerySigner stub;
			try {
				stub = (CloudatlasQuerySigner) registry
						.lookup("cloudatlas_signer");
			} catch (NotBoundException e) {
				e.printStackTrace();
				throw new ServletException(e);
			}
			return stub;
		} catch (Exception e) {
			throw new RmiConnectionException(e);
		}
	}

	private static class RmiConnectionException extends IOException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5303508464967062601L;

		public RmiConnectionException(Exception e) {
			super(e);
		}

	}

	private abstract static class PageHandler extends ContextHandler {
		public abstract String getPage();

		public abstract void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST st) throws IOException;

		public void init() {
			this.setContextPath("/" + getPage());
			this.setAllowNullPathInfo(true);
			this.setHandler(new AbstractHandler() {

				@Override
				public void handle(String arg0, Request arg1,
						HttpServletRequest request, HttpServletResponse response)
						throws IOException, ServletException {
					STGroupDir templates = new STGroupDir("templates", '$', '$');
					response.setContentType("text/html");
					handlePage(request, response,
							templates.getInstanceOf(getPage()));
					arg1.setHandled(true);
				}
			});
		}
	}
	
	private static abstract class ValueToNumberStringConverter<T extends Value> {
		public abstract String convert(T val);
		@SuppressWarnings("unchecked")
		public String convertValue(Value val) {
			return convert(((T) val));
		}
	}
	
	private static class ValueLongToNumberStringConverter extends ValueToNumberStringConverter<ValueSimple<Long>> {

		@Override
		public String convert(ValueSimple<Long> val) {
			return val.getValue().toString();
		}
		
	}
	
	private static class ValueDoubleToNumberStringConverter extends ValueToNumberStringConverter<ValueSimple<Double>> {

		@Override
		public String convert(ValueSimple<Double> val) {
			return val.getValue().toString();
		}
		
	}
}
