package lucee.runtime.engine;

import java.io.IOException;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class Request extends Thread {

	public static final short TYPE_CFML = 1;
	public static final short TYPE_LUCEE = 2;
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

	public void run() {
		try {
			exe(pc, type, false, true);
		}
		catch (Throwable _t) {}
		done = true;
		SystemUtil.notify(parent);
	}

	public static void exe(PageContext pc, short type, boolean throwExcpetion, boolean registerWithThread) throws IOException, PageException {
		ThreadQueue queue = null;
		try {
			if (registerWithThread) ThreadLocalPageContext.register(pc);
			ThreadQueue tmp = pc.getConfig().getThreadQueue();
			tmp.enter(pc);
			queue = tmp;

			if (type == TYPE_CFML) pc.executeCFML(pc.getHttpServletRequest().getServletPath(), throwExcpetion, true);
			else if (type == TYPE_LUCEE) pc.execute(pc.getHttpServletRequest().getServletPath(), throwExcpetion, true);
			else pc.executeRest(pc.getHttpServletRequest().getServletPath(), throwExcpetion);
		}
		finally {
			if (queue != null) queue.exit(pc);
			if (registerWithThread) ThreadLocalPageContext.release();
		}
	}

	public boolean isDone() {
		return done;
	}

}
