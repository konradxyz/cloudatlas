package pl.edu.mimuw.cloudatlas.common.rmi;

import java.rmi.RemoteException;
import java.rmi.Remote;

import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public interface CloudatlasQuerySigner extends Remote {
	public InstallQueryResult installQuery(String name, String query) throws RemoteException;
	public ValueQuery uninstallQuery(ValueQuery query) throws RemoteException;
}
