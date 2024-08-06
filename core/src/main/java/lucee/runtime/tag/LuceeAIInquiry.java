
package lucee.runtime.tag;

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.PageContextImpl;
import lucee.runtime.ai.Response;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.util.KeyConstants;

public final class LuceeAIInquiry extends TagImpl {

	private String question;
	private String answer = null;

	@Override
	public void release() {
		super.release();
		question = null;
		answer = null;
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

			if (answer == null) {
				PageContextImpl pci = ((PageContextImpl) pageContext);
				if (pci.undefinedScope().getCheckArguments()) {
					pci.localScope().set(KeyConstants._answer, rsp.getAnswer());
				}
				answer = "answer";

			}
			if (answer != null) pageContext.setVariable(answer, rsp.getAnswer());
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