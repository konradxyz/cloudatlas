package pl.edu.mimuw.cloudatlas.modules.framework.example;

import pl.edu.mimuw.cloudatlas.modules.framework.AddressGenerator;
import pl.edu.mimuw.cloudatlas.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ModuleFramework;
import pl.edu.mimuw.cloudatlas.modules.framework.ShutdownModule;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public final class ExampleModuleFramework extends ModuleFramework {
	
	@Override
	public Message getInitializationMessage() {
		return new SimpleMessage<String>("Module test");
	}


	@Override
	public Module getRootModule(AddressGenerator addressGenerator,
			ShutdownModule shutdownModule) {
		return new EchoModule(addressGenerator.getUniqueAddress(), 
				shutdownModule);
	}


	@Override
	public int getInitializationMessageType() {
		return ReaderModule.LINE_READ;
	}

}
