package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.cloudatlas.modules.ModuleAddresses;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageWrapper;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.ModuleFramework;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class ExampleModuleFramework extends ModuleFramework {

	@Override
	public List<List<Module>> getModules() {
		List<List<Module>> result = new ArrayList<List<Module>>();
		List<Module> tmp = new ArrayList<Module>();
		tmp.add(getShutdownModule());
		result.add(tmp);
		tmp = new ArrayList<Module>();
		tmp.add(new PrinterModule());
		result.add(tmp);
		tmp = new ArrayList<Module>();
		tmp.add(new ReaderModule(ModuleAddresses.ECHO));
		result.add(tmp);
		tmp = new ArrayList<Module>();
		tmp.add(new EchoModule());
		result.add(tmp);
		return result;
	}

	
	// Sooo ugly
	@Override
	public MessageWrapper getInitializationMessage() {
		return new MessageWrapper(ModuleAddresses.ECHO,
				ReaderModule.LINE_READ, new SimpleMessage<String>(
						"Module test"), ModuleAddresses.ANY);
	}

}
