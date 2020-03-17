package lucee.runtime.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContextImpl;

public class MonitorState {

	public static List<BlockedThread> checkForBlockedThreads(Collection<PageContextImpl> values) {
		List<BlockedThread> blockets = new ArrayList<MonitorState.BlockedThread>();
		Iterator<PageContextImpl> it = values.iterator();
		BlockedThread bt;
		while (it.hasNext()) {
			bt = checkForBlockedThreads(it.next());
			if (bt != null) blockets.add(bt);
		}
		return blockets;
	}

	public static String getBlockedThreads(PageContextImpl pc) {
		BlockedThread bt = checkForBlockedThreads(pc);
		return bt == null ? "" : bt.getMessage();
	}

	public static BlockedThread checkForBlockedThreads(PageContextImpl pc) {
		// if(pc.getStartTime() + pc.getRequestTimeout() > System.currentTimeMillis()) return null;

		Thread t = pc.getThread();
		if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
			StackTraceElement[] ste = t.getStackTrace();
			return new BlockedThread(t, ste, getPossibleThreadsCausingThis(t, ste[0]));
		}
		return null;
	}

	public static List<BlockedThread> checkForBlockedThreadsx(Collection<PageContextImpl> values) {
		List<BlockedThread> blockets = new ArrayList<MonitorState.BlockedThread>();
		Iterator<PageContextImpl> it = values.iterator();
		PageContextImpl pc;
		Thread t;
		while (it.hasNext()) {
			pc = it.next();
			t = pc.getThread();
			if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
				StackTraceElement[] ste = t.getStackTrace();
				blockets.add(new BlockedThread(t, ste, getPossibleThreadsCausingThis(t, ste[0])));
			}
		}
		return blockets;
	}

	public static List<BlockedThread> checkForBlockedThreads() {
		List<BlockedThread> blockets = new ArrayList<MonitorState.BlockedThread>();
		Iterator<Entry<Thread, StackTraceElement[]>> it = Thread.getAllStackTraces().entrySet().iterator();
		Entry<Thread, StackTraceElement[]> e;
		Thread t;
		while (it.hasNext()) {
			e = it.next();
			t = e.getKey();
			if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
				blockets.add(new BlockedThread(e.getKey(), e.getValue(), getPossibleThreadsCausingThis(t, e.getValue()[0])));
			}
		}
		return blockets;
	}

	private static boolean ignore(Thread t) {
		return t == null || "Finalizer".equals(t.getName()) || "Reference Handler".equals(t.getName()) || "Signal Dispatcher".equals(t.getName());
	}

	private static List<Entry<Thread, StackTraceElement[]>> getPossibleThreadsCausingThis(Thread blockedThread, StackTraceElement blockedSTE) {
		List<Entry<Thread, StackTraceElement[]>> list = new ArrayList<Entry<Thread, StackTraceElement[]>>();
		Iterator<Entry<Thread, StackTraceElement[]>> it = Thread.getAllStackTraces().entrySet().iterator();
		Entry<Thread, StackTraceElement[]> e;
		StackTraceElement[] ste;
		Thread t;
		while (it.hasNext()) {
			e = it.next();
			t = e.getKey();
			if (t == blockedThread || ignore(t)) continue;

			ste = e.getValue();
			int index = match(blockedSTE, ste, t);
			if (index == -1) continue;

			list.add(e);
		}
		return list;
	}

	private static int match(StackTraceElement blockedSTE, StackTraceElement[] stes, Thread t) {
		for (int i = 0; i < stes.length; i++) {
			StackTraceElement ste = stes[i];

			if (ste.getClassName().equals(blockedSTE.getClassName()) && ste.getMethodName().equals(blockedSTE.getMethodName())) {
				int stel = ste.getLineNumber();
				int bstel = blockedSTE.getLineNumber();

				if (stel > bstel) return i;
				if (stel == bstel && i > 0) {
					if (i > 0) return i;
					if (!Thread.State.BLOCKED.equals(t.getState())) return i;
				}
				break;
			}
		}
		return -1;
	}

	private static class T extends Thread {
		private static Object o = new Object();

		@Override
		public void run() {
			checkit();
		}

		private void checkit() {
			synchronized (o) {
				w();
				SystemUtil.wait(this, 10);

			}
		}

		private void w() {
			// TODO Auto-generated method stub

		}
	}

	public static class BlockedThread {

		public final List<Entry<Thread, StackTraceElement[]>> possibleBlockers;
		public final Thread blockedThread;
		public final StackTraceElement[] blockedST;

		public BlockedThread(Thread blockedThread, StackTraceElement[] blockedST, List<Entry<Thread, StackTraceElement[]>> possibleBlockers) {
			this.blockedThread = blockedThread;
			this.blockedST = blockedST;
			this.possibleBlockers = possibleBlockers;
		}

		public String getMessage() {
			if (possibleBlockers.isEmpty()) return "The thread is blocked.";

			StringBuilder sb = new StringBuilder(
					possibleBlockers.size() > 1 ? "The thread is possibly blocked by the following threads:\n" : "The thread is possibly blocked by the following thread:\n");

			Iterator<Entry<Thread, StackTraceElement[]>> it = possibleBlockers.iterator();
			Entry<Thread, StackTraceElement[]> e;
			while (it.hasNext()) {
				e = it.next();
				sb.append(ExceptionUtil.toString(e.getValue())).append("\n");

			}
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder().append("Blocked:\n").append(ExceptionUtil.toString(blockedST)).append("\nPossible Blockers:\n");

			Iterator<Entry<Thread, StackTraceElement[]>> it = possibleBlockers.iterator();
			Entry<Thread, StackTraceElement[]> e;
			while (it.hasNext()) {
				e = it.next();
				sb.append(ExceptionUtil.toString(e.getValue())).append("\n");

			}
			return sb.toString();
		}
	}
}
