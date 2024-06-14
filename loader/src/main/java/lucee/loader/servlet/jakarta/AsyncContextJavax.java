package lucee.loader.servlet.jakarta;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jakarta.servlet.AsyncListener;

public class AsyncContextJavax implements AsyncContext, Jakarta {

	private jakarta.servlet.AsyncContext asyncContext;

	public AsyncContextJavax(jakarta.servlet.AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	@Override
	public ServletRequest getRequest() {
		return new ServletRequestJavax(asyncContext.getRequest());
	}

	@Override
	public ServletResponse getResponse() {
		return new ServletResponseJavax(asyncContext.getResponse());
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return asyncContext.hasOriginalRequestAndResponse();
	}

	@Override
	public void dispatch() {
		asyncContext.dispatch();
	}

	@Override
	public void dispatch(String path) {
		asyncContext.dispatch(path);
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		asyncContext.dispatch(((ServletContextJavax) context).getJakartaContext(), path);
	}

	@Override
	public void complete() {
		asyncContext.complete();
	}

	@Override
	public void start(Runnable run) {
		asyncContext.start(run);
	}

	@Override
	public void addListener(javax.servlet.AsyncListener listener) {
		asyncContext.addListener(new jakarta.servlet.AsyncListener() {
			@Override
			public void onComplete(jakarta.servlet.AsyncEvent event) throws IOException {
				listener.onComplete(new javax.servlet.AsyncEvent(new AsyncContextJavax(event.getAsyncContext()), new ServletRequestJavax(event.getSuppliedRequest()),
						new ServletResponseJavax(event.getSuppliedResponse())));
			}

			@Override
			public void onError(jakarta.servlet.AsyncEvent event) throws IOException {
				listener.onError(new javax.servlet.AsyncEvent(new AsyncContextJavax(event.getAsyncContext()), new ServletRequestJavax(event.getSuppliedRequest()),
						new ServletResponseJavax(event.getSuppliedResponse()), event.getThrowable()));
			}

			@Override
			public void onStartAsync(jakarta.servlet.AsyncEvent event) throws IOException {
				listener.onStartAsync(new javax.servlet.AsyncEvent(new AsyncContextJavax(event.getAsyncContext()), new ServletRequestJavax(event.getSuppliedRequest()),
						new ServletResponseJavax(event.getSuppliedResponse())));
			}

			@Override
			public void onTimeout(jakarta.servlet.AsyncEvent event) throws IOException {
				listener.onTimeout(new javax.servlet.AsyncEvent(new AsyncContextJavax(event.getAsyncContext()), new ServletRequestJavax(event.getSuppliedRequest()),
						new ServletResponseJavax(event.getSuppliedResponse())));
			}
		});
	}

	@Override
	public void addListener(javax.servlet.AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		asyncContext.addListener(

				(AsyncListener) ((AsyncListenerJavax) listener).getJakartaInstance(),

				(jakarta.servlet.ServletRequest) ((ServletRequestJavax) servletRequest).getJakartaInstance(),

				(jakarta.servlet.ServletResponse) ((ServletResponseJavax) servletResponse).getJakartaInstance()

		);
	}

	@Override
	public <T extends javax.servlet.AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new ServletException("Could not create listener", e);
		}
	}

	@Override
	public void setTimeout(long timeout) {
		asyncContext.setTimeout(timeout);
	}

	@Override
	public long getTimeout() {
		return asyncContext.getTimeout();
	}

	@Override
	public Object getJakartaInstance() {
		return asyncContext;
	}
}