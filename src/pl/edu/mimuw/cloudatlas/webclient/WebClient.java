package pl.edu.mimuw.cloudatlas.webclient;

import java.io.IOException;
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
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import pl.edu.mimuw.cloudatlas.common.model.Attribute;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.Type.PrimaryType;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueSimple;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.UnknownZoneException;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.webclient.models.ZoneModel;

public class WebClient {
	private final String rmiHost;
	private final int rmiPort;
	private final Server jettyServer;
	private final int refreshPeriodMs;
	
	//Plot:
	private final int plotLenghtMs;
	private final int plotRefreshPeriodMs;
	
	private final Map<PrimaryType, ValueToNumberStringConverter<?>> converters = new HashMap<PrimaryType, ValueToNumberStringConverter<?>>();

	// Node data:
	private NodeDataKeeper keeper;
	private Thread keeperUpdaterThread;

	private void fillHeader(ST template) {
		template.add("path", keeper.getConfig().getPathName());
		template.add("address", keeper.getConfig().getAddress());
	}

	private final PageHandler indexHandler = new PageHandler() {

		@Override
		public void handlePage(HttpServletRequest request,
				HttpServletResponse response, ST template) throws IOException {
			fillHeader(template);
			List<ZoneModel> zones = new ArrayList<ZoneModel>();
			for (ZmiData<AttributesMap> zone : keeper.getAttributes()
					.getContent()) {
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
	
	private final Handler getAttrHandler = new AbstractHandler() {
		
		@Override
		public void handle(String arg0, Request arg1, HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException {
			arg1.setHandled(true);
			response.setContentType("application/json");
			ZmisAttributes attrs = keeper.getAttributes();
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
			result.put("value", converters.get(attributeValue.getType().getPrimaryType()).convertValue(attributeValue));
			result.put("timestamp", timestamp.getValue().toString());
			String output = JSON.toString(result);
			response.getOutputStream().print(output);
	}
	};

	private final Runnable keeperUpdater = new Runnable() {
		
		@Override
		public void run() {
			while ( true ) {
				try {
					Thread.sleep(refreshPeriodMs);
					CloudatlasAgentRmiServer stub = connect();
					keeper.setAttributes(stub.getRootZmi());
					keeper.setConfig(stub.getConfig());
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	};
	

	public WebClient(String rmiHost, int rmiPort, int port, int refreshPeriodMs,
			int plotLenghtMs, int plotRefreshPeriodMs) {
		super();
		this.rmiHost = rmiHost;
		this.rmiPort = rmiPort;
		this.refreshPeriodMs = refreshPeriodMs;
		this.plotLenghtMs = plotLenghtMs;
		this.plotRefreshPeriodMs = plotRefreshPeriodMs;
		this.jettyServer = new Server(port);
		ValueDoubleToNumberStringConverter doubleConverter = new ValueDoubleToNumberStringConverter();
		converters.put(PrimaryType.DOUBLE, doubleConverter);
		ValueLongToNumberStringConverter longConverter = new ValueLongToNumberStringConverter();
		converters.put(PrimaryType.INT, longConverter);
		converters.put(PrimaryType.DURATION, longConverter);
		converters.put(PrimaryType.TIME, longConverter);
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
				.asList(indexHandler, plotHandler);
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
		keeper = new NodeDataKeeper(stub.getRootZmi(), stub.getConfig());
		keeperUpdaterThread = new Thread(keeperUpdater);
		keeperUpdaterThread.start();
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

	private static class RmiConnectionException extends Exception {

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
