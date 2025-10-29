package lucee.commons.lang;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * thead that init a PageContext with ThreadLocal, only use this Thread when you are sure it ends
 * before the parent thread
 */
public abstract class PageContextThread extends ParentThreasRefThread {

	private PageContext pageContext;

	public PageContextThread(PageContext pc) {
		this.pageContext = pc;
		setDaemon(true);
	}

	@Override
	public final void run() {
		Thread t = pageContext.getThread();
		// Java 25: Establish ScopedValue scope for child thread execution
		ScopedValue.where( ThreadLocalPageContext.CURRENT, pageContext )
				.where( ThreadLocalConfig.CURRENT, pageContext.getConfig() )
				.run( () -> {
					try {
						run( pageContext );
					}
					finally {
						if (t != null) ((PageContextImpl) pageContext).setThread( t );
					}
				} );
	}

	public abstract void run(PageContext pageContext);
}
