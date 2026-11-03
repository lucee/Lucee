package lucee.runtime.ai;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Listener of an AI session (LDEV-6540), a component or a struct of functions with the optional
 * hooks onBeforePromptSent, onResponse and onStreamingResponse.
 * 
 * Every hook gets one struct (event) and can
 * <ul>
 * <li>change the member of the event (request, response, chunk) in place</li>
 * <li>return a struct that replaces that member</li>
 * <li>return nothing or true to continue</li>
 * <li>return false: onBeforePromptSent cancels the request, onStreamingResponse aborts the stream.
 * For onResponse false has no meaning.</li>
 * <li>throw an exception, that is passed to the caller of the inquiry as is</li>
 * </ul>
 */
public final class AISessionListener {

	public static final Key ON_BEFORE_PROMPT_SENT = KeyImpl.init("onBeforePromptSent");
	public static final Key ON_RESPONSE = KeyImpl.init("onResponse");
	public static final Key ON_STREAMING_RESPONSE = KeyImpl.init("onStreamingResponse");

	public static final String ERROR_CODE_CANCELLED = "ai.listener.cancelled";
	public static final String ERROR_CODE_ABORTED = "ai.listener.aborted";

	private static final Key REQUEST = KeyImpl.init("request");
	private static final Key RESPONSE = KeyImpl.init("response");
	private static final Key CHUNK = KeyImpl.init("chunk");

	private final Struct target;
	private final boolean hasBefore;
	private final boolean hasResponse;
	private final boolean hasStreaming;

	private AISessionListener(Struct target, boolean hasBefore, boolean hasResponse, boolean hasStreaming) {
		this.target = target;
		this.hasBefore = hasBefore;
		this.hasResponse = hasResponse;
		this.hasStreaming = hasStreaming;
	}

	/**
	 * @param pc the current PageContext
	 * @param listener a component or a struct of functions
	 * @return the listener or null when listener is null
	 * @throws PageException when the listener has none of the hooks
	 */
	public static AISessionListener getInstance(PageContext pc, Object listener) throws PageException {
		if (listener == null) return null;
		Struct target;
		if (listener instanceof Component || listener instanceof Struct) target = (Struct) listener;
		else throw new ApplicationException("the listener of an AI session has to be a component or a struct with functions, but it is a [" + Caster.toTypeName(listener) + "]");

		boolean before = has(pc, target, ON_BEFORE_PROMPT_SENT);
		boolean response = has(pc, target, ON_RESPONSE);
		boolean streaming = has(pc, target, ON_STREAMING_RESPONSE);
		if (!before && !response && !streaming) {
			throw new ApplicationException("the listener of an AI session needs at least one of the functions [" + ON_BEFORE_PROMPT_SENT + ", " + ON_RESPONSE + ", "
					+ ON_STREAMING_RESPONSE + "]");
		}
		return new AISessionListener(target, before, response, streaming);
	}

	private static boolean has(PageContext pc, Struct target, Key name) {
		if (target instanceof Component) return ((Component) target).contains(pc, name);
		return target.get(name, null) instanceof UDF;
	}

	public boolean hasBeforePromptSent() {
		return hasBefore;
	}

	public boolean hasResponse() {
		return hasResponse;
	}

	public boolean hasStreamingResponse() {
		return hasStreaming;
	}

	/**
	 * called with the request body, before it is sent to the provider
	 * 
	 * @return the request to send
	 * @throws AIListenerCancelException when the request is cancelled or the listener throws an
	 *             exception, the request is not sent
	 */
	public Struct beforePromptSent(AISession session, String provider, String model, boolean stream, Struct request) {
		if (!hasBefore) return request;
		try {
			Struct event = createEvent(session, provider, model);
			event.set("stream", stream);
			event.set(REQUEST, request);
			Object rtn = invoke(ON_BEFORE_PROMPT_SENT, event);
			if (Boolean.FALSE.equals(rtn)) {
				LogUtil.logx(null, Log.LEVEL_WARN, "ai", "request cancelled by the listener ai.connection=" + session.getEngine().getName() + " gen_ai.conversation.id=" + session.getId(),
						"ai", "application");
				ApplicationException ae = new ApplicationException("The request to the AI connection [" + session.getEngine().getName() + "] was cancelled by the listener.");
				ae.setErrorCode(ERROR_CODE_CANCELLED);
				throw ae;
			}
			Struct result = result(ON_BEFORE_PROMPT_SENT, rtn, event.get(REQUEST, null), request);
			// a request that was not a stream cannot be turned into a stream, nor the other way around
			Object s = result.get("stream", null);
			if (s != null && Caster.toBooleanValue(s, stream) != stream) {
				throw new ApplicationException("the listener function [" + ON_BEFORE_PROMPT_SENT + "] must not change the value of [stream] of the request");
			}
			return result;
		}
		catch (PageException pe) {
			throw new AIListenerCancelException(pe);
		}
	}

	/**
	 * called with the response of a request that was not streamed
	 * 
	 * @return the response to use
	 */
	public Struct response(AISession session, String provider, String model, Struct response) throws PageException {
		if (!hasResponse) return response;
		Struct event = createEvent(session, provider, model);
		event.set(RESPONSE, response);
		Object rtn = invoke(ON_RESPONSE, event);
		return result(ON_RESPONSE, Boolean.FALSE.equals(rtn) ? null : rtn, event.get(RESPONSE, null), response);
	}

	/**
	 * called with every chunk of a streamed response, before the chunk is passed on
	 * 
	 * @param index the position of the chunk, starting with 1
	 * @return the chunk to use
	 * @throws PageException when the stream is aborted
	 */
	public Struct streamingResponse(AISession session, String provider, String model, int index, Struct chunk) throws PageException {
		if (!hasStreaming) return chunk;
		Struct event = createEvent(session, provider, model);
		event.set("index", index);
		event.set(CHUNK, chunk);
		Object rtn = invoke(ON_STREAMING_RESPONSE, event);
		if (Boolean.FALSE.equals(rtn)) {
			ApplicationException ae = new ApplicationException(
					"The streamed response of the AI connection [" + session.getEngine().getName() + "] was aborted by the listener after " + (index - 1) + " chunks.");
			ae.setErrorCode(ERROR_CODE_ABORTED);
			throw ae;
		}
		return result(ON_STREAMING_RESPONSE, rtn, event.get(CHUNK, null), chunk);
	}

	private Struct createEvent(AISession session, String provider, String model) throws PageException {
		Struct event = new StructImpl(StructImpl.TYPE_LINKED);
		event.set("session", session);
		event.set("connection", session.getEngine().getName());
		event.set("provider", provider);
		event.set(KeyConstants._model, model);
		return event;
	}

	private Object invoke(Key name, Struct event) throws PageException {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc == null) throw new ApplicationException("the listener function [" + name + "] of the AI session can only be called from a request");
		if (target instanceof Component) return ((Component) target).call(pc, name.getString(), new Object[] { event });
		return ((UDF) target.get(name)).call(pc, new Object[] { event }, true);
	}

	/**
	 * a hook returns a struct (replaces), nothing/true (the member of the event, that the hook may have
	 * changed in place) but nothing else
	 */
	private static Struct result(Key hook, Object returned, Object member, Struct original) throws PageException {
		if (returned != null && !Boolean.TRUE.equals(returned)) {
			if (returned instanceof Boolean || !(returned instanceof Struct)) {
				throw new ApplicationException("the listener function [" + hook + "] has to return a struct, true, false or nothing, but it returned a [" + Caster.toTypeName(returned) + "]");
			}
			return (Struct) returned;
		}
		if (member == null) return original;
		Struct s = Caster.toStruct(member, null);
		if (s == null) throw new ApplicationException("the listener function [" + hook + "] replaced the content of the event with a [" + Caster.toTypeName(member) + "], it has to be a struct");
		return s;
	}
}
