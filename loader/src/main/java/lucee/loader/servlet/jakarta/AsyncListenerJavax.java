package lucee.loader.servlet.jakarta;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class AsyncListenerJavax implements AsyncListener, Jakarta {

	private final jakarta.servlet.AsyncListener listener;

	public AsyncListenerJavax(jakarta.servlet.AsyncListener listener) {
		this.listener = listener;
	}

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		listener.onComplete((jakarta.servlet.AsyncEvent) ((AsyncEventJavax) event).getJakartaInstance());
	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		listener.onTimeout((jakarta.servlet.AsyncEvent) ((AsyncEventJavax) event).getJakartaInstance());
	}

	@Override
	public void onError(AsyncEvent event) throws IOException {
		listener.onError((jakarta.servlet.AsyncEvent) ((AsyncEventJavax) event).getJakartaInstance());
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
		listener.onStartAsync((jakarta.servlet.AsyncEvent) ((AsyncEventJavax) event).getJakartaInstance());
	}

	@Override
	public Object getJakartaInstance() {
		return listener;
	}

	private static class AsyncEventJavax extends AsyncEvent implements Jakarta {

		private final jakarta.servlet.AsyncEvent event;

		public AsyncEventJavax(jakarta.servlet.AsyncEvent event) {
			super(null); // The superclass requires a valid constructor argument.
			this.event = event;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return new AsyncContextJavax(event.getAsyncContext());
		}

		@Override
		public ServletRequest getSuppliedRequest() {
			return new ServletRequestJavax(event.getSuppliedRequest());
		}

		@Override
		public ServletResponse getSuppliedResponse() {
			return new ServletResponseJavax(event.getSuppliedResponse());
		}

		@Override
		public Throwable getThrowable() {
			return event.getThrowable();
		}

		@Override
		public Object getJakartaInstance() {
			return event;
		}
	}

}
