package pl.edu.mimuw.cloudatlas.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public interface CloudatlasAgentRmiServer extends Remote {
	public void setCurrentNodeAttribute(String attribute, Value value)
			throws RemoteException;
	public ZmisAttributes getRootZmi() throws RemoteException;
	public CloudatlasAgentConfig getConfig() throws RemoteException;
	public static final int DEFAULT_PORT = 33333;
}
