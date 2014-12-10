package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class ReaderModule extends Module implements Runnable {

	public ReaderModule(Address address, Address target) {
		super(address);
		this.target = target;
	}

	public static final int LINE_READ = 0;

	private final Address target;
	private Thread readingThread;
	private BufferedReader reader = null;

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		return handlers;
	}


	@Override
	public void initialize() {
		reader = new BufferedReader(new InputStreamReader(System.in));
		readingThread = new Thread(this);
		readingThread.start();

	}

	@Override
	public void shutdown() {
		readingThread.interrupt();
		try {
			readingThread.join();
		} catch (InterruptedException e) {
			// Cleanup error - I don't think we can do something more.
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				while (reader.ready()) {
					String line = reader.readLine();
					sendMessage(target, LINE_READ, new SimpleMessage<String>(
							line));
				}
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			if (Thread.interrupted()) {
				return;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
