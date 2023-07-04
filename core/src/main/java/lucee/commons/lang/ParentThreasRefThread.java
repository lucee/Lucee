package lucee.commons.lang;

import java.util.ArrayList;
import java.util.List;

public class ParentThreasRefThread extends Thread {
	private Thread thread;
	private StackTraceElement[] stes;

	public Thread getParentThread() {
		return thread;
	}

	@Override
	public synchronized void start() {
		this.stes = Thread.currentThread().getStackTrace();
		super.start();
	}

	public void addParentStacktrace(Throwable t) {
		StackTraceElement[] tmp = t.getStackTrace();
		List<StackTraceElement> merged = new ArrayList<>(tmp.length + stes.length - 1);

		for (StackTraceElement ste: tmp) {
			merged.add(ste);
		}
		{
			StackTraceElement ste;
			for (int i = 1; i < stes.length; i++) {
				ste = stes[i];
				merged.add(ste);
			}
		}
		t.setStackTrace(merged.toArray(new StackTraceElement[0]));
	}

}
