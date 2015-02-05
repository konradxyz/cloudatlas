package pl.edu.mimuw.cloudatlas.common.rmi;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public interface CloudatlasAgentRmiServer extends Remote {
	public void setCurrentNodeAttribute(String attribute, Value value)
			throws RemoteException;
	public ZmisAttributes getRootZmi() throws RemoteException;
	public CloudatlasAgentConfig getConfig() throws RemoteException;
	public Map<String, ValueQuery> getQueries() throws RemoteException;
	public List<InetAddress> getFallbackAddresses() throws RemoteException;
	public void setFallbackAddresses(List<InetAddress> addrs) throws RemoteException;
	public void installQuery(Certificate queryCertificate) throws RemoteException;
}
