
package lucee.runtime.tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.Response;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;

public final class LuceeAI extends BodyTagTryCatchFinallyImpl {

	private String _default;
	private String message;
	private String name;
	private AIEngine aie;

	@Override
	public void release() {
		super.release();
		name = null;
		_default = null;
		message = null;
		aie = null;
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

	@Override
	public int doStartTag() throws PageException {
		if (!StringUtil.isEmpty(name, true)) {
			aie = ((PageContextImpl) pageContext).createAISession(name.trim(), message);
		}
		else if (!StringUtil.isEmpty(_default, true)) {
			String name = ((PageContextImpl) pageContext).getNameFromDefault(_default.trim());
			aie = ((PageContextImpl) pageContext).createAISession(name.trim(), message);
		}
		else {
			throw new ApplicationException("you need to define the attribute [name] or [default]");
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public void doFinally() {
		if (aie != null) {
			// ((PageContextImpl) pageContext).returnAISession(aie);
		}
	}

	public Response question(String question) throws PageException {
		return aie.invoke(question);
	}
}