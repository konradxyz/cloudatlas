package pl.edu.mimuw.cloudatlas.agent.modules.framework.example;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleFramework;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public final class ExampleModuleFramework extends ModuleFramework {
	
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
		return new EchoModule(rootAddress, 
				shutdownModuleAddress);
	}

}
