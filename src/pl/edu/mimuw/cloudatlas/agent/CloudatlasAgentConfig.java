package pl.edu.mimuw.cloudatlas.agent;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

public final class CloudatlasAgentConfig {
	private final PathName pathName;

	public CloudatlasAgentConfig(PathName pathName) {
		super();
		this.pathName = pathName;
	}

	public PathName getPathName() {
		return pathName;
	}

	public static CloudatlasAgentConfig fromIni(Ini file) {
		try {
			String pathName = file.get("agent", "zone_path_name");
			return new CloudatlasAgentConfig(new PathName(pathName));
		} catch (Exception e) {
			System.err.println("Could not parse config file, cause: '"
					+ e.getMessage() + "'");
			return null;
		}
	}
}
