package lucee.runtime.tag;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CatchBlockImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;

// MUST change behavior of multiple headers now is an array, it das so?

/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 * 
 **/
public final class Timeout extends BodyTagImpl {

	private boolean forcestop = false;
	private PageContext pc;
	private ThreadImpl thread;
	private UDF onTimeout = null;
	private UDF onError = null;
	private long timeoutInMillis;

	@Override
	public void release() {
		super.release();
		timeoutInMillis = 0;
		pc = null;
		thread = null;
		forcestop = false;
		onTimeout = null;
		onError = null;
	}

	public void setOntimeout(Object obj) throws PageException {
		if (obj == null) return;
		onTimeout = Caster.toFunction(obj);
	}

	public void setOnerror(Object obj) throws PageException {
		if (obj == null) return;
		onError = Caster.toFunction(obj);
	}

	public void setForcestop(boolean forcestop) {
		this.forcestop = forcestop;
	}

	public void setTimespan(Object timeout) throws PageException {
		if (timeout instanceof TimeSpan) this.timeoutInMillis = ((TimeSpan) timeout).getMillis();
		else this.timeoutInMillis = Caster.toLongValue(Caster.toDoubleValue(timeout) * 1000D);
	}

	@Override
	public int doStartTag() throws PageException {
		this.pc = pageContext;// do not remove, this is needed
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		return EVAL_PAGE;
	}

	public void register(Page currentPage, int threadIndex) throws PageException {
		try {
			thread = new ThreadImpl((PageContextImpl) pc, currentPage, threadIndex);
			thread.setDaemon(false);
			thread.start();

			try {
				if (timeoutInMillis != 0) thread.join(timeoutInMillis);
				else thread.join();
			}
			catch (InterruptedException e) {
			}

			// handle exception
			handleException(thread);

			if (!thread.hasEnded()) {
				if (forcestop) {
					SystemUtil.stop(pc, thread);
				}
				handleTimeout(thread);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private void handleTimeout(ThreadImpl thread2) throws PageException {
		if (onTimeout != null) onTimeout.call(pc, new Object[] { TimeSpanImpl.fromMillis(timeoutInMillis) }, true);
		else throw new ApplicationException("a timeout occurred within the tag timeout", "timeout is set to " + timeoutInMillis + " ms");
	}

	private void handleException(ThreadImpl thread2) throws PageException {
		PageException ex = thread.getException();
		if (ex != null) {
			if (onError != null) onError.call(pc, new Object[] { new CatchBlockImpl(ex) }, true);
			else throw ex;
		}
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {

	}

	public static class ThreadImpl extends Thread {

		private final Page page;
		private final int threadIndex;
		private final PageContextImpl pc;

		boolean terminated;
		private long startTime;
		private long endTime;
		private PageException pe;

		public ThreadImpl(PageContextImpl pc, Page page, int threadIndex) {
			this.pc = pc;
			this.threadIndex = threadIndex;
			this.page = page;
		}

		@Override
		public void run() {
			startTime = System.currentTimeMillis();
			try {
				page.threadCall(pc, threadIndex);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (!Abort.isSilentAbort(t)) {
					pe = Caster.toPageException(t);
				}
			}
			finally {
				endTime = System.currentTimeMillis();
			}
		}

		public boolean hasStarted() {
			return startTime > 0;
		}

		public boolean hasEnded() {
			return endTime > 0;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getEndTime() {
			return endTime;
		}

		public PageException getException() {
			return pe;
		}

		public long executionTime() {
			if (startTime == 0) return 0L;
			if (endTime == 0) return System.currentTimeMillis() - startTime;
			return endTime - startTime;
		}
	}
}