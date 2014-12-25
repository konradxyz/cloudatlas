package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleFramework;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.example.ReaderModule;
import pl.edu.mimuw.cloudatlas.agent.modules.main.MainModule;

public class CloudatlasAgentModulesFramework extends ModuleFramework {
	private final CloudatlasAgentConfig config;

	public CloudatlasAgentModulesFramework(CloudatlasAgentConfig config) {
		this.config = config;
	}

	@Override
	public Message getInitializationMessage() {
		return new SimpleMessage<String>("Module test");
	}

	@Override
	public int getInitializationMessageType() {
		return ReaderModule.LINE_READ;
	}

	@Override
	public Module getRootModule(Address rootAddress,
			Address shutdownModuleAddress) {
		return new MainModule(config, rootAddress, shutdownModuleAddress);
	}

}
