package pl.edu.mimuw.cloudatlas.common.rmi;

import java.io.Serializable;

import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class InstallQueryResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6789344938096502493L;
	
	
	private final Status status;
	// Semantics of this field depends on value of status.
	// If status == OK then it is new query. It should be sent to agent.
	// If status == CONFLICT then it is query that is already installed and that is conflicting with our query.
	// If status == INVALID_QUERY then it is null.
	private final ValueQuery query;
	
	
	
	public enum Status {
		OK, CONFLICT, INVALID_QUERY
	}



	public InstallQueryResult(Status status, ValueQuery query) {
		super();
		this.status = status;
		this.query = query;
	}



	public Status getStatus() {
		return status;
	}



	public ValueQuery getQuery() {
		return query;
	}
}
