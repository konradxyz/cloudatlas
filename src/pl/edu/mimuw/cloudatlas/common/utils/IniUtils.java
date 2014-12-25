package pl.edu.mimuw.cloudatlas.common.utils;

import java.io.File;
import java.io.IOException;

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
}
