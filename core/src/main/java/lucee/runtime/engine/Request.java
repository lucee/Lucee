package lucee.runtime.engine;

import java.io.IOException;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public final class Request extends Thread {

	public static final short TYPE_CFML = 1;
	public static final short TYPE_REST = 3;

	private PageContext pc;
	private Thread parent;
	private boolean done;
	private short type;

	public Request(PageContext pc, short type) {
		this.parent = Thread.currentThread();
		this.pc = pc;
		this.type = type;

	}

	@Override
	public void run() {
		try {
			exe(pc, type, false, true);
		}
		catch (Throwable _t) {
		}
		done = true;
		SystemUtil.notify(parent);
	}

	public static void exe(PageContext pc, short type, boolean throwExcpetion, boolean registerWithThread) throws IOException, PageException {
		if (registerWithThread) {
			// Java 25: Establish ScopedValue scopes for PageContext and Config
			// This provides automatic cleanup and better virtual thread support
			ScopedValue.where(ThreadLocalPageContext.CURRENT, pc)
					.where(ThreadLocalConfig.CURRENT, pc.getConfig())
					.run(() -> {
						try {
							executeRequest(pc, type, throwExcpetion);
						}
						catch (IOException | PageException e) {
							throw new RuntimeException(e);
						}
					});
		}
		else {
			// No scope registration - direct execution
			executeRequest(pc, type, throwExcpetion);
		}
	}

	private static void executeRequest(PageContext pc, short type, boolean throwExcpetion) throws IOException, PageException {
		ThreadQueue queue = null;
		try {
			ThreadQueue tmp = pc.getConfig().getThreadQueue();
			tmp.enter(pc);
			queue = tmp;
			if (type == TYPE_REST) pc.executeRest(pc.getHttpServletRequest().getServletPath(), throwExcpetion);
			else pc.executeCFML(pc.getHttpServletRequest().getServletPath(), throwExcpetion, true);
		}
		finally {
			if (queue != null) queue.exit(pc);
		}
	}

	public boolean isDone() {
		return done;
	}

}
