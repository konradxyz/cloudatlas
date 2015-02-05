package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.CA.ZoneAuthenticationData;
import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.GetMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.common.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.common.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueCertificate;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.SingleMachineZmiData.ZmiLevel;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;

public class QueryKeeperModule extends Module {
	private final PathName machineName;
	private final Cipher verifyCipher;
	private final CloudatlasAgentConfig config;

	public QueryKeeperModule(Address address, CloudatlasAgentConfig config)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException {
		super(address);
		this.config = config;
		this.machineName = config.getPathName();
		this.verifyCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		this.verifyCipher.init(Cipher.DECRYPT_MODE, config.getSignerKey());
	}

	public final static Integer RECALCULATE_ZMI = 1;
	public final static Integer INSTALL_QUERY = 2;
	public final static Integer GET_QUERIES = 3;
	public final static Integer DUMP = 4;
	public static final int ACCEPT_QUERY = 5;

	private final List<Program> predefinedQueries = new ArrayList<Program>();
	
	private static class QueryWrapper {
		private final Program parsedQuery;
		private final Certificate queryCertificate;
		private final ValueQuery value;
	
		public QueryWrapper(Program parsedQuery, Certificate value) {
			super();
			this.parsedQuery = parsedQuery;
			this.queryCertificate = value;
			this.value = new ValueQuery(value);
		}
		public Program getParsedQuery() {
			return parsedQuery;
		}
		public ValueQuery getValue() {
			return value;
		}
		
		public Certificate getQueryCertificate() {
			return queryCertificate;
		}
	};

	private final Map<String, QueryWrapper> queries = new HashMap<String, QueryWrapper>();

	private final MessageHandler<InstallQueryMessage> installQueryHandler = new MessageHandler<InstallQueryMessage>() {

		@Override
		public void handleMessage(InstallQueryMessage message)
				throws HandlerException {
			System.out.println("INstalling query");
			for (Certificate query : message.getContent()) {
				try {
					AttributesMap attributesMap = query.getAttributesMap();
					Certificate certificate = ((ValueCertificate) attributesMap
							.get("certificate")).getValue();
					String zoneName = ((ValueString) certificate.getAttributesMap().get("zoneName")).getValue();
					PathName path = new PathName(zoneName);
					PublicKey clientKey = 
							config.getZoneCertificationData().get(path.getComponents().size()).getChildrenAuthenticationKey();
					ZoneAuthenticationData auth = config.getZoneCertificationData().
							get(path.getComponents().size());
					System.out.println(auth.getCertificate().getAttributesMap());
					System.err.println(DatatypeConverter.printHexBinary(clientKey.getEncoded()));
					if (!certificate.isValid(clientKey)) {
						System.err.println("CC is not valid, skipping");
						continue;
					}

					PublicKey queryAuthKey = ((ValueKey) certificate
							.getAttributesMap().get("publicKey")).getValue();
					if (!query.isValid(queryAuthKey))
						continue;

					System.err.println("Query validated");
					QueryWrapper wrapper = null;
					Value valueString = query.getAttributesMap().getOrNull("query");
					if (valueString==null){
						wrapper = new QueryWrapper(null, query);
					}
					else {
						String queryString = ((ValueString) valueString).getValue();
						Program parsedProgram = MainInterpreter
							.parseProgram(queryString);
						wrapper = new QueryWrapper(parsedProgram, query);
					}
					String queryName = ((ValueString) query.getAttributesMap().get("name")).getValue();
					Long time = ((ValueTime)query.getAttributesMap().get("timestamp")).getValue();
					queries.put(queryName, wrapper);
					if (queries.containsKey(queryName)) {
						QueryWrapper old = queries.get(queryName);
						Long oldTime = ((ValueTime) old.getQueryCertificate()
								.getAttributesMap().get("timestamp"))
								.getValue();
						if ( time > oldTime ) {
							queries.put(queryName, wrapper);
						}
					} else
						queries.put(queryName, wrapper);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

	};
	
	private final MessageHandler<InstallQueryMessage> acceptQueryHandler = new MessageHandler<InstallQueryMessage>() {

		@Override
		public void handleMessage(InstallQueryMessage message)
				throws HandlerException {
			System.out.println("INstalling query");
			for (Certificate query : message.getContent()) {
				try {				
					QueryWrapper wrapper = null;
					Value valueString = query.getAttributesMap().getOrNull("query");
					if (valueString==null){
						wrapper = new QueryWrapper(null, query);
					}
					else {
						String queryString = ((ValueString) valueString).getValue();
						Program parsedProgram = MainInterpreter
							.parseProgram(queryString);
						wrapper = new QueryWrapper(parsedProgram, query);
					}
					String queryName = ((ValueString) query.getAttributesMap().get("name")).getValue();
					Long time = ((ValueTime)query.getAttributesMap().get("timestamp")).getValue();
					queries.put(queryName, wrapper);
					if (queries.containsKey(queryName)) {
						QueryWrapper old = queries.get(queryName);
						Long oldTime = ((ValueTime) old.getQueryCertificate()
								.getAttributesMap().get("timestamp"))
								.getValue();
						if ( time > oldTime ) {
							queries.put(queryName, wrapper);
						}
					} else
						queries.put(queryName, wrapper);

				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

	};

	private final MessageHandler<RecalculateZmisMessage> recalculateHandler = new MessageHandler<RecalculateZmisMessage>() {

		@Override
		public void handleMessage(RecalculateZmisMessage message)
				throws HandlerException {
			SingleMachineZmiData<AttributesMap> data = message.getRootZmi();
			PathName current = PathName.ROOT;
			List<PathName> paths = new ArrayList<PathName>();
			paths.add(current);
			for (int i = 1; i < data.getLevels().size(); ++i) {
				current = current.levelDown(data.getLevels().get(i)
						.getOurZoneName());
				paths.add(current);
			}
			for (int i = data.getLevels().size() - 2; i >= 0; --i) {
				ZMI parent = new ZMI();
				for (AttributesMap m : data.getLevels().get(i + 1).getZones()
						.values()) {
					ZMI son = new ZMI();
					son.getAttributes().swap(m);
					parent.addSon(son);
				}
				ZmiLevel<AttributesMap> parentLevel = data.getLevels().get(i);
				AttributesMap newParentMap = generateNewAttributes(parent, i,
						parentLevel.getOurZoneName());
				sendMessage(message.getTargetAddress(), message.getTargetMessageType(), 
						new ZmiRecalculatedMessage(newParentMap.clone(), paths.get(i)));

				parentLevel.getZones().get(parentLevel.getOurZoneName())
						.swap(newParentMap);

			}
		}

		private AttributesMap generateNewAttributes(ZMI parent, long level,
				String name) {
			AttributesMap newMap = new AttributesMap();
			// Lets copy current name.
			newMap.add("name", new ValueString(name));
			newMap.add("level", new ValueInt(level));
			newMap.add("owner", new ValueString(machineName.getName()));
			for (Program program : predefinedQueries) {
				runQuery(parent, program, newMap);
			}
			for (Entry<String, QueryWrapper> entry : queries.entrySet()) {
				if ( entry.getValue().getParsedQuery() != null ) {
					if (runQuery(parent, entry.getValue().getParsedQuery(), newMap)) {
						newMap.addOrChange(entry.getKey(), entry.getValue()
							.getValue());
					}
				}
			}
			newMap.addOrChange("timestamp", new ValueTime(Calendar
					.getInstance().getTimeInMillis()));
			return newMap;

		}
		
		private boolean runQuery(ZMI parent, Program query, AttributesMap result) {
			try {
				Interpreter interpreter = new Interpreter(parent);
				List<QueryResult> results = interpreter.interpretProgram(query);
				for (QueryResult r : results) {
					result.addOrChange(r.getName(), r.getValue());
				}
				return true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return false;
			}			
		}
	};
	
	private final MessageHandler<GetMessage> getQueriesHandler = new MessageHandler<GetMessage>() {

		@Override
		public void handleMessage(GetMessage message) throws HandlerException {
			Map<String, Certificate> result = new HashMap<String, Certificate>();
			for ( Entry<String, QueryWrapper> entry : queries.entrySet() ) {
				result.put(entry.getKey(), entry.getValue().getQueryCertificate());
			}
			sendMessage(message.getResponseTarget(), message.getResponseMessageType(), new SimpleMessage<>(result));
		}
	};
	
	private final MessageHandler<Message> dumpHandler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) throws HandlerException {
			System.err.println(queries);
			
		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		return getHandlers(new Integer[] { RECALCULATE_ZMI, INSTALL_QUERY, GET_QUERIES, DUMP , ACCEPT_QUERY},
				new MessageHandler<?>[] { recalculateHandler, installQueryHandler, getQueriesHandler, dumpHandler, acceptQueryHandler});
	}

	@Override
	public void initialize() throws ModuleInitializationException {
		try {
			String[] queries = new String[] {
					"SELECT sum(cardinality) AS cardinality",
					"SELECT to_set(random(5, unfold(contacts))) AS contacts" };
			for (String query : queries) {
				predefinedQueries.add(MainInterpreter.parseProgram(query));
			}
		} catch (Exception e) {
			throw new ModuleInitializationException(e);
		}
	}
}
