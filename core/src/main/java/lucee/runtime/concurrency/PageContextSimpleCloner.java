package lucee.runtime.concurrency;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

import lucee.print;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.thread.ThreadUtil;

public class PageContextSimpleCloner {

	private PageContextImpl parent;
	ConcurrentLinkedDeque<PageContextImpl> pcs = new ConcurrentLinkedDeque<PageContextImpl>();

	public PageContextSimpleCloner(PageContext parent) {
		this.parent = (PageContextImpl) parent;
	}

	public PageContextImpl get(OutputStream os) {
		PageContextImpl pc;
		try {
			pc = pcs.pop();
		}
		catch (NoSuchElementException nsee) {
			pc = null;
		}

		ThreadLocalPageContext.register(parent); // this is needed otherwise cloning fails (NPE)
		if (pc == null) {
			pc = ThreadUtil.clonePageContext(parent, os, false, true, false);
		}
		else {
			long start = System.nanoTime();
			pc.setHttpServletResponse(ThreadUtil.createHttpServletResponse(os));
			print.e("1:" + (System.nanoTime() - start));
			start = System.nanoTime();
			parent.copyStateTo(pc);
			print.e("2:" + (System.nanoTime() - start));
			start = System.nanoTime();
			ThreadLocalPageContext.register(pc);
			print.e("3:" + (System.nanoTime() - start));

		}
		pc.getRootOut().setAllowCompression(false); // make sure content is not compressed
		return pc;
	}

	public void release(PageContextImpl pc) throws IOException {
		pc.getOut().flush();
		pcs.add(pc);
	}

	public void end() {
		CFMLFactory factory = parent.getConfig().getFactory();
		Iterator<PageContextImpl> it = pcs.iterator();
		while (it.hasNext()) {
			factory.releasePageContext(it.next());
		}
		pcs.clear();
	}

}
