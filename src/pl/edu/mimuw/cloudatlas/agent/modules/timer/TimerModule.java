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
		long period;
		Address target;
		int gatewayMessageType;

		public SchedulePriority(int requestId, long timeStamp, long period, Address target, int gatewayMessageType) {
			System.err.println("Creating schedule priority " + requestId + " " + timeStamp);
			this.requestId = requestId;
			this.timeStamp = timeStamp;
			this.period = period;
			this.target = target;
			this.gatewayMessageType = gatewayMessageType;
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
		
		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}
		
	}

	public TimerModule(Address address) {
		super(address);

	}

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
					(long) message.getDelay() + now.getTimeInMillis(), message.getPeriod(), message.getTarget(), message.getGatewayMessageType()));
			timingThread.interrupt();
		}
	};

	@Override
	public void initialize() {
		timingThread = new Thread(this);
		timingThread.start();

	}

	@Override
	public void shutdown() {
		scheduleQueue.add(new SchedulePriority(-1,-1,-1, null, -1));
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
				if (schedulePriority.timeStamp < 0) {
					return;
				}
				Calendar now = GregorianCalendar.getInstance();
				long nowMillis = now.getTimeInMillis();
				if (nowMillis >= schedulePriority.timeStamp) {
					AlarmMessage alarmMessage = new AlarmMessage(
							schedulePriority.getRequestId());
					sendMessage(schedulePriority.target, schedulePriority.gatewayMessageType, alarmMessage);
					schedulePriority.setTimeStamp(schedulePriority.timeStamp+schedulePriority.period);
					scheduleQueue.add(schedulePriority);
					
				} else {
					scheduleQueue.put(schedulePriority);
					Thread.sleep(schedulePriority.timeStamp - nowMillis);
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
