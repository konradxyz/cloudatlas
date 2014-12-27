package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleFramework;
import pl.edu.mimuw.cloudatlas.agent.modules.main.MainModule;
import pl.edu.mimuw.cloudatlas.common.CloudatlasAgentConfig;

public class CloudatlasAgentModulesFramework extends ModuleFramework {
	private final CloudatlasAgentConfig config;

	public CloudatlasAgentModulesFramework(CloudatlasAgentConfig config) {
		this.config = config;
	}

	@Override
	public Message getInitializationMessage() {
		return new Message();
	}

	@Override
	public int getInitializationMessageType() {
		return MainModule.INITIALIZE;
	}

	@Override
	public Module getRootModule(Address rootAddress,
			Address shutdownModuleAddress) {
		return new MainModule(config, rootAddress, shutdownModuleAddress);
	}

}
