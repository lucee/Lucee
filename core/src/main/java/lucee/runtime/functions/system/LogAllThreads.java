
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.Controler;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class LogAllThreads implements Function {

	private static final long serialVersionUID = -1922482127354478506L;

	public static String call(PageContext pc, String path) throws PageException {
		return call(pc, path, 0, 10000);
	}

	public static String call(PageContext pc, String path, Number interval) throws PageException {
		return call(pc, path, interval, 10000);
	}

	public static String call(PageContext pc, String path, Number interval, Number duration) throws PageException {
		Resource res = ResourceUtil.toResourceNotExisting(pc, path);
		if (!res.getParentResource().isDirectory())
			throw new FunctionException(pc, "LogAllThreads", 1, "path", "the directory [" + res.getParent() + "] for your log file [" + path + "] does not exist.");

		int tmp = Caster.toIntValue(interval);
		if (tmp < 0) tmp = 10;
		final long interv = tmp;

		long ltmp = Caster.toLongValue(duration);
		if (ltmp < 1) ltmp = 10000;
		final long dur = ltmp;

		long start = System.currentTimeMillis();
		// Create a new thread to run the task
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					if ((start + dur) < System.currentTimeMillis()) {
						break;
					}
					// Call the dumpThreadPositions method
					Controler.dumpThreadPositions(res);

					// Pause for the specified interval
					if (interv > 0) SystemUtil.sleep(interv);
				}
				catch (IOException e) {
					SystemUtil.sleep(1000);
				}
			}
		});

		thread.setDaemon(true);
		// Start the thread
		thread.start();
		return null;
	}
}