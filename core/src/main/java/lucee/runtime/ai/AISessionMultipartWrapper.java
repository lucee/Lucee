package lucee.runtime.ai;

import java.lang.reflect.Method;
import java.util.List;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class AISessionMultipartWrapper implements AISessionMultipart {

	private final AISession delegate;

	// Cached reflective methods for multipart calls
	private static volatile Method inquiryPartsMethod;
	private static volatile Method inquiryPartsListenerMethod;

	private AISessionMultipartWrapper(AISession delegate) {
		this.delegate = delegate;
	}

	// ---------- Static factory ----------

	public static AISessionMultipart createAISessionMultipartWrap(AISession ais) {
		// We already know from the caller that this is an AISessionMultiParts instance
		// (checked via Class.forName(...).isInstance(ais)), so we just wrap it.
		return new AISessionMultipartWrapper(ais);
	}

	// ---------- Helper: reflection invocation ----------

	private static Method getInquiryPartsMethod(AISession delegate) throws NoSuchMethodException {
		if (inquiryPartsMethod == null) {
			synchronized (AISessionMultipartWrapper.class) {
				if (inquiryPartsMethod == null) {
					inquiryPartsMethod = delegate.getClass().getMethod("inquiry", List.class);
				}
			}
		}
		return inquiryPartsMethod;
	}

	private static Method getInquiryPartsListenerMethod(AISession delegate) throws NoSuchMethodException {
		if (inquiryPartsListenerMethod == null) {
			synchronized (AISessionMultipartWrapper.class) {
				if (inquiryPartsListenerMethod == null) {
					inquiryPartsListenerMethod = delegate.getClass().getMethod("inquiry", List.class, AIResponseListener.class);
				}
			}
		}
		return inquiryPartsListenerMethod;
	}

	// ---------- AISession methods (direct delegation) ----------

	@Override
	public Response inquiry(String message) throws PageException {
		return delegate.inquiry(message);
	}

	@Override
	public Response inquiry(String message, AIResponseListener listener) throws PageException {
		return delegate.inquiry(message, listener);
	}

	@Override
	public String getSystemMessage() {
		return delegate.getSystemMessage();
	}

	@Override
	public Conversation[] getHistory() {
		return delegate.getHistory();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public AIEngine getEngine() {
		return delegate.getEngine();
	}

	@Override
	public int getConversationSizeLimit() {
		return delegate.getConversationSizeLimit();
	}

	@Override
	public Double getTemperature() {
		return delegate.getTemperature();
	}

	@Override
	public int getSocketTimeout() {
		return delegate.getSocketTimeout();
	}

	@Override
	public int getConnectTimeout() {
		return delegate.getConnectTimeout();
	}

	@Override
	public void release() throws PageException {
		delegate.release();
	}

	// ---------- AISessionMultipart methods (multipart, via reflection) ----------

	@Override
	public Response inquiry(List<Part> parts) throws PageException {
		try {
			Method m = getInquiryPartsMethod(delegate);
			Object result = m.invoke(delegate, parts);
			return (Response) result;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Response inquiry(List<Part> parts, AIResponseListener listener) throws PageException {
		try {
			Method m = getInquiryPartsListenerMethod(delegate);
			Object result = m.invoke(delegate, parts, listener);
			return (Response) result;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}
