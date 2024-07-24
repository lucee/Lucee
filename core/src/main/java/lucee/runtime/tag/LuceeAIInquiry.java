
package lucee.runtime.tag;

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.ai.Response;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;

public final class LuceeAIInquiry extends TagImpl {

	private static final String DEFAULT_ANSWER_NAME = "cfai";

	private String question;
	private String answer = DEFAULT_ANSWER_NAME;

	@Override
	public void release() {
		super.release();
		question = null;
		answer = DEFAULT_ANSWER_NAME;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public int doStartTag() throws PageException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof LuceeAI)) {
			parent = parent.getParent();
		}

		if (parent instanceof LuceeAI) {
			Response rsp = ((LuceeAI) parent).question(question);
			pageContext.setVariable(answer, rsp.getAnswer());
		}
		else {
			throw new ApplicationException("the tag [LuceeAIInquiry] need to be insiide the tag [LuceeAI]");
		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}