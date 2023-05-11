package lucee.runtime.engine;

import java.io.IOException;

import lucee.runtime.PageContext;

public class ThreadQueueNone implements ThreadQueue {

	public static final ThreadQueue instance = new ThreadQueueNone();

	@Override
	public void enter(PageContext pc) throws IOException {
	}

	@Override
	public void exit(PageContext pc) {
	}

	@Override
	public void clear() {
	}

	@Override
	public int size() {
		return 0;
	}
}
