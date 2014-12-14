package pl.edu.mimuw.cloudatlas.nodeclient;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.management.MBeanServerConnection;

import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public final class NodeClient implements Runnable {
	private final String host;
	private final int rmiPort;
	private final int refreshPeriodMs;

	public NodeClient(String host, int rmiPort, int refreshPeriodMs) {
		super();
		this.host = host;
		this.rmiPort = rmiPort;
		this.refreshPeriodMs = refreshPeriodMs;
	}

	private static Scanner getCommandScanner(String command)
			throws IOException, InterruptedException {
		String[] cmd = { "/bin/sh", "-c", command };
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		Scanner scanner = new Scanner(p.getInputStream());
		return scanner;
	}

	// Returns first line of output.
	private static String runCommand(String command) throws IOException,
			InterruptedException {
		Scanner scanner = getCommandScanner(command);
		String res = scanner.nextLine();
		scanner.close();
		return res;
	}

	// TODO: dnsnames, averaging - check lab06.
	// Right now we send average load from last 1 minute, as reported by uptime.
	public Map<String, Value> getAttributes() throws IOException,
			NumberFormatException, InterruptedException {
		Map<String, Value> result = new HashMap<String, Value>();

		MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

		OperatingSystemMXBean osMBean = ManagementFactory
				.newPlatformMXBeanProxy(mbsc,
						ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
						OperatingSystemMXBean.class);

		result.put(
				"cpu_load",
				new ValueDouble(
						Double.parseDouble(runCommand("uptime | sed s/.*average:\\ // | sed s/,.*//"))));

		// Disk
		File f = new File("/");
		result.put("total_disk", new ValueInt(f.getTotalSpace()));
		result.put("free_disk", new ValueInt(f.getFreeSpace()));

		// Ram
		Scanner sc = getCommandScanner("free | grep 'Mem'");
		try {
			sc.next(); // discard.
			result.put("total_ram", new ValueInt(sc.nextLong()));
			sc.next();
			result.put("free_ram", new ValueInt(sc.nextLong()));
		} finally {
			sc.close();
		}

		// Swap
		sc = getCommandScanner("free | grep 'Swap'");
		try {
			sc.next(); // discard.
			result.put("total_swap", new ValueInt(sc.nextLong()));
			sc.next();
			result.put("free_swap", new ValueInt(sc.nextLong()));
		} finally {
			sc.close();
		}

		result.put("num_processes",
				new ValueInt(Long.parseLong(runCommand("ps -e | wc -l"))));

		result.put("num_cores",
				new ValueInt((long) osMBean.getAvailableProcessors()));
		result.put("kernel_ver", new ValueString(osMBean.getVersion()));

		result.put("logged_users",
				new ValueInt(Long.parseLong(runCommand("who | wc -l"))));

		return result;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(refreshPeriodMs);
				Registry registry = LocateRegistry.getRegistry(host, rmiPort);
				CloudatlasAgentRmiServer stub = (CloudatlasAgentRmiServer) registry
						.lookup("cloudatlas");
				Map<String, Value> attrs = getAttributes();
				System.out.println(attrs);
				for (Entry<String, Value> attr : attrs.entrySet()) {
					stub.setCurrentNodeAttribute(attr.getKey(), attr.getValue());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
