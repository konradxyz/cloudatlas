package pl.edu.mimuw.cloudatlas.agent.modules.timer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.HandlerException;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Module;

public class TimerModule extends Module implements Runnable {

	private static final class SchedulePriority implements
			Comparable<SchedulePriority> {
		int requestId;
		long timeStamp;

		public SchedulePriority(int requestId, long timeStamp) {
			System.err.println("Creating schedule priority " + requestId + " " + timeStamp);
			this.requestId = requestId;
			this.timeStamp = timeStamp;
		}

		public int getRequestId() {
			return requestId;
		}

		@Override
		public int compareTo(SchedulePriority schedule) {
			if (timeStamp < schedule.timeStamp)
				return -1;
			if (timeStamp > schedule.timeStamp)
				return 1;
			return requestId - schedule.requestId;
		}
	}

	public TimerModule(Address address, Address target, int gatewayMessageType) {
		super(address);
		this.target = target;
		this.gatewayMessageType = gatewayMessageType;

	}

	private final Address target;
	private final int gatewayMessageType;
	private Thread timingThread;
	public static final int SCHEDULE_MESSAGE = 1;
	private PriorityBlockingQueue<SchedulePriority> scheduleQueue = new PriorityBlockingQueue<SchedulePriority>();

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> handlers = new HashMap<Integer, MessageHandler<?>>();
		handlers.put(SCHEDULE_MESSAGE, receiveHandler);
		return handlers;
	}

	private final MessageHandler<ScheduleAlarmMessage> receiveHandler = new MessageHandler<ScheduleAlarmMessage>() {

		@Override
		public void handleMessage(ScheduleAlarmMessage message)
				throws HandlerException {
			Calendar now = GregorianCalendar.getInstance();
			scheduleQueue.add(new SchedulePriority(message.getRequestId(),
					(long) message.getDelay() + now.getTimeInMillis()));
		}
	};

	@Override
	public void initialize() {
		timingThread = new Thread(this);
		timingThread.start();

	}

	@Override
	public void shutdown() {
		timingThread.interrupt();
		try {
			timingThread.join();
		} catch (InterruptedException e) {
			// Cleanup error - I don't think we can do something more.
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			SchedulePriority schedulePriority;
			try {
				schedulePriority = scheduleQueue.take();
				Calendar now = GregorianCalendar.getInstance();
				if (now.getTimeInMillis() >= schedulePriority.timeStamp) {
					AlarmMessage alarmMessage = new AlarmMessage(
							schedulePriority.getRequestId());
					sendMessage(target, gatewayMessageType, alarmMessage);
				} else {
					scheduleQueue.put(schedulePriority);
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
