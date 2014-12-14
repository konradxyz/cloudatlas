package pl.edu.mimuw.cloudatlas.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public interface CloudatlasAgentRmiServer extends Remote {
	public void setCurrentNodeAttribute(String attribute, Value value)
			throws RemoteException;
	public ZMI getRootZmi() throws RemoteException;
	public static final int DEFAULT_PORT = 33333;
}
