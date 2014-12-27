package pl.edu.mimuw.cloudatlas.webclient;

import java.util.Map;

import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public class NodeDataKeeper {
	private ZmisAttributes attributes;
	private CloudatlasAgentConfig config;
	private Map<String, ValueQuery> queries;

	public NodeDataKeeper(ZmisAttributes attributes,
			CloudatlasAgentConfig config,
			Map<String, ValueQuery> queries) {
		super();
		this.attributes = attributes;
		this.config = config;
		this.queries = queries;
	}

	// We expect that node data will not be change during http requests.
	// As such, we need to synchronize accesses to NodeDataKeeper only.
	public synchronized ZmisAttributes getAttributes() {
		return attributes;
	}

	public synchronized CloudatlasAgentConfig getConfig() {
		return config;
	}

	public synchronized void setAttributes(ZmisAttributes attributes) {
		this.attributes = attributes;
	}

	public synchronized void setConfig(CloudatlasAgentConfig config) {
		this.config = config;
	}

	public Map<String, ValueQuery> getQueries() {
		return queries;
	}

	public void setQueries(Map<String, ValueQuery> queries) {
		this.queries = queries;
	}

}
