package pl.edu.mimuw.cloudatlas.webclient;

import java.net.InetAddress;

public class QuerySignerAddress {
	private final InetAddress address;
	private final int port;
	public QuerySignerAddress(InetAddress address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	public InetAddress getAddress() {
		return address;
	}
	public int getPort() {
		return port;
	}
}
