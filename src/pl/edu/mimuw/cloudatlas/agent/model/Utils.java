package pl.edu.mimuw.cloudatlas.agent.model;

import java.io.PrintStream;

import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class Utils {
	public static void print(SingleMachineZmiData<AttributesMap> content,
			PrintStream output) {
		for (ZmiData<AttributesMap> zmi : content.getContent()) {
			output.println(zmi.getPath());
			zmi.getContent().printAttributes(output);
		}
	}
}
