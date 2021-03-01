package lucee.commons.cpu;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.types.RefLong;
import lucee.commons.lang.types.RefLongImpl;

public class CPULogger {

	private Logger logger;
	private long slotTime;
	private double threshold;
	private List<Listener> listeners;

	public CPULogger(long slotTime, double threshold, List<Listener> listeners) {
		this.slotTime = slotTime;
		this.threshold = threshold;
		this.listeners = listeners;
	}

	public void startIt() {
		if (logger == null || !logger.isAlive() || !logger.run) {
			logger = new Logger(slotTime, threshold, listeners);
			logger.start();
		}
	}

	public void stopIt() {
		if (logger != null && logger.isAlive()) {
			logger.run = false;
			SystemUtil.stop(logger);
		}
	}

	public Logger getLogger() {
		return logger;
	}

	private static class Logger extends Thread {

		private boolean run = true;
		private Map<String, Data> log = new ConcurrentHashMap<>();
		private long range;
		private double threshold;
		private List<Listener> listeners;

		public Logger(long range, double threshold, List<Listener> listeners) {
			this.range = range;
			this.threshold = threshold;
			this.listeners = listeners;
		}

		@Override
		public void run() {
			ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();

			while (run) {
				try {
					Iterator<Thread> it = Thread.getAllStackTraces().keySet().iterator();
					Thread t;
					// ThreadInfo ti;
					String key;
					Data data = null;
					RefLong total = new RefLongImpl(0);
					Map<String, Data> tmp = new ConcurrentHashMap<>();
					while (it.hasNext()) {
						t = it.next();
						if (State.TIMED_WAITING.equals(t.getState()) || State.WAITING.equals(t.getState()) || State.TERMINATED.equals(t.getState())) continue;
						// ti = tmxb.getThreadInfo(t.getId());
						key = t.getName();// + ":" + ti.getWaitedCount();
						long cpuTime = tmxb.getThreadCpuTime(t.getId());
						data = log.get(key);
						if (data == null) {
							data = new Data(t, cpuTime);
						}
						else data.add(total, cpuTime);
						tmp.put(key, data);
					}
					log = tmp;
					List<StaticData> list = cloneIt(log);
					if (list != null && listeners != null) {
						Iterator<Listener> itt = listeners.iterator();
						while (itt.hasNext()) {
							itt.next().listen(list);
						}
					}
					if (range > 0) SystemUtil.sleep(range);

				}
				catch (Exception e) {
					LogUtil.log(null, "application", "cpu", e);
				}
			}
		}

		private List<StaticData> cloneIt(Map<String, Data> log) {
			List<StaticData> staticData = new ArrayList<>();
			{
				Iterator<Entry<String, Data>> it = log.entrySet().iterator();
				Entry<String, Data> entry;
				while (it.hasNext()) {
					entry = it.next();
					StaticData sd = new StaticData(entry.getValue());
					if (threshold <= sd.percentage) staticData.add(sd);
				}
			}
			return staticData;
		}

	}

	private static class Data {

		private final long start;
		private long time;
		private Thread thread;
		private RefLong total;

		public Data(Thread thread, long start) {
			this.thread = thread;
			this.start = start;
		}

		public long add(RefLong total, long time) {
			this.total = total;
			this.time = time - start;
			total.plus(this.time);
			return this.time;
		}

		public long getTime() {
			return time;
		}

		public double getPercentage() {
			if (total == null) return 0;
			double percentage = time == 0 ? 0 : 1D / total.toLongValue() * time;
			int tmp = (int) (percentage * 100D);
			return tmp / 100D;
		}

		public Thread getThread() {
			return thread;
		}
	}

	public static class StaticData {

		public String name;
		private final long start;
		private long time;

		private long total;
		private double percentage;
		private String stacktrace;

		public StaticData(Data data) {
			this.name = data.thread.getName();
			this.start = data.start;
			this.time = data.time;
			this.percentage = data.getPercentage();
			this.stacktrace = ExceptionUtil.toString(data.thread.getStackTrace());
		}

		public String getName() {
			return name;
		}

		public long getStart() {
			return start;
		}

		public long getTime() {
			return time;
		}

		public long getTotal() {
			return total;
		}

		public double getPercentage() {
			return percentage;
		}

		public String getStacktrace() {
			return stacktrace;
		}
	}
}
