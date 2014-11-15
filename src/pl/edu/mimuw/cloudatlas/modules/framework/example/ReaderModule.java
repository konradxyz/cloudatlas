package pl.edu.mimuw.cloudatlas.modules.framework.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import pl.edu.mimuw.cloudatlas.modules.ModuleAddresses;
import pl.edu.mimuw.cloudatlas.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.modules.framework.Module;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class ReaderModule extends Module implements Runnable {

	public static final int LINE_READ = 0;

	private final int target;
	private Thread readingThread;
	private Scanner scanner = new Scanner(System.in);

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		return handlers;
	}

	@Override
	protected Integer getAddress() {
		return ModuleAddresses.READER;
	}

	@Override
	public void initialize() {
		readingThread = new Thread(this);
		readingThread.start();
	}

	@Override
	public void shutdown() {
		scanner.close();
		readingThread.interrupt();
	}

	public ReaderModule(int target) {
		super();
		this.target = target;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted() && scanner.hasNextLine()) {
			String line = scanner.nextLine();
			sendMessage(target, LINE_READ, new SimpleMessage<String>(line));
		}
	}

}
