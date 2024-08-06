
package lucee.runtime.tag;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.AISession;
import lucee.runtime.ai.AIUtil;
import lucee.runtime.ai.Response;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;

public final class LuceeAI extends BodyTagTryCatchFinallyImpl {

	private String _default;
	private String message;
	private String name;
	private AISession session;
	private boolean throwonerror = true;
	private long timeout = -1;
	private String meta;

	@Override
	public void release() {
		super.release();
		name = null;
		_default = null;
		message = null;
		meta = null;
		session = null;
		throwonerror = true;
		timeout = -1;
	}

	public void setDefault(String _default) {
		this._default = _default;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public void setThrowonerror(boolean throwonerror) {
		this.throwonerror = throwonerror;
	}

	public void setTimeout(Object timeout) throws PageException {
		if (timeout instanceof TimeSpan) this.timeout = ((TimeSpan) timeout).getMillis();
		// seconds
		else {
			int i = Caster.toIntValue(timeout);
			if (i < 0) throw new ApplicationException("invalid value [" + i + "] for attribute timeout, value must be a positive integer greater or equal than 0");

			this.timeout = new TimeSpanImpl(0, 0, 0, i).getMillis();
		}
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			if (!StringUtil.isEmpty(name, true)) {
				session = ((PageContextImpl) pageContext).createAISession(name.trim(), message);
				setMeta(session);
			}
			else if (!StringUtil.isEmpty(_default, true)) {
				PageContextImpl pci = ((PageContextImpl) pageContext);
				String name = throwonerror ? pci.getNameFromDefault(_default.trim()) : pci.getNameFromDefault(_default.trim(), null);
				if (!throwonerror && name == null) return SKIP_BODY;
				session = ((PageContextImpl) pageContext).createAISession(name.trim(), message, timeout);
				setMeta(session);
			}
			else {
				throwonerror = true;
				throw new ApplicationException("you need to define the attribute [name] or [default]");
			}
		}
		catch (Exception e) {
			if (throwonerror) throw Caster.toPageException(e);
			return SKIP_BODY;
		}
		return EVAL_BODY_INCLUDE;
	}

	private void setMeta(AISession session2) throws PageException {
		if (!StringUtil.isEmpty(meta, true)) {
			pageContext.setVariable(meta, AIUtil.getMetaData(session.getEngine()));
		}
	}

	@Override
	public void doCatch(Throwable t) throws Throwable {
		ExceptionUtil.rethrowIfNecessary(t);
		if (throwonerror) throw Caster.toPageException(t);
		if (bodyContent != null) bodyContent.clearBody();
	}

	@Override
	public void doFinally() {
		if (session != null) {
			try {
				session.release();
			}
			catch (PageException e) {
				throw new PageRuntimeException(e);
			}
		}
	}

	public Response question(String question) throws PageException {
		return session.inquiry(question);
	}
}