package pl.edu.mimuw.cloudatlas.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.ini4j.Ini;

// Functions 
public class IniUtils {
	public static Ini readConfigFromArgs(String[] args) {
		if (args.length != 1) {
			System.err
					.println("Wrong number of arguments. Expected single argument - path to .ini config file.");
			return null;
		}
		return readConfigFromPath(args[0]);
	}

	private static Ini readConfigFromPath(String path) {
		try {
			return new Ini(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
			System.err
					.println("Could not find, open or parse config file. Cause: "
							+ e.getMessage());
			return null;
		}
	}
	
	public static int readInt(Ini file, String group, String param) {
		return Integer.parseInt(file.get(group, param));
	}
	
	public static byte[] readByteArrayFromHex(Ini file, String group, String param) {
		return DatatypeConverter.parseHexBinary(file.get(group, param));
	}
	
	public static Inet4Address readAddressFromIni(Ini file, String group, String interfaceParam) {
		String interfaceName = file.get(group, interfaceParam);
		Inet4Address result = null;
		try {
			List<InetAddress> addresses = Collections.list(NetworkInterface
					.getByName(interfaceName).getInetAddresses());
			for (InetAddress address : addresses) {
				try {
					result = (Inet4Address) address;
				} catch (ClassCastException e) {
				}
			}
		} catch (NullPointerException e) {
			System.err.println("Unknown interface '" + interfaceName + "'");
		} catch (SocketException e1) {
			System.err.println(e1.getMessage());
		}
		if ( result == null )
			System.err.println("Could not retrieve IPv4 address associated with interface " + interfaceName);
		return result;
	}
}
