package lucee.runtime.type;

import java.util.Set;

import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.engine.ThreadLocalPageSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.ComponentUtil;

public abstract class UDFPropertiesBase implements UDFProperties {

	private Page page;
	private String id;
	protected PageSource ps;
	protected PageSource psOrg;
	protected int startLine;
	protected int endLine;

	public UDFPropertiesBase() {
	}

	public UDFPropertiesBase(Page page, PageSource ps, int startLine, int endLine) {
		this.page = page;
		psOrg = ps;

		if (ps == null) {
			ps = ThreadLocalPageSource.get();
			if (ps == null && page != null) {
				ps = page.getPageSource();
			}
		}
		this.ps = ps;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public final Page getPage(PageContext pc) throws PageException {
		Page p = getPage();
		if (p != null) return p;

		// MUST no page source
		PageException pe = null;
		if (getPageSource() != null) {
			try {
				return ComponentUtil.getPage(pc, getPageSource());
			}
			catch (PageException e) {
				pe = e;
			}
		}

		if (pe != null) throw pe;
		throw new ApplicationException("missing Page Source");
	}

	public final String id() {
		if (id == null) {
			// MUST no page source
			if (getPageSource() != null) {
				id = getPageSource().getDisplayPath() + ":" + getIndex();
			}
			else if (getPage() != null) {
				// MUST id for Page
				id = getPage().hashCode() + ":" + getIndex();
			}
		}
		return id;
	}

	protected final Page getPage() {
		return page;
	}

	public final PageSource getPageSource() {
		return ps;
	}

	public final int getStartLine() {
		return startLine;
	}

	public final int getEndLine() {
		return endLine;
	}

	public abstract String getFunctionName();

	public abstract boolean getOutput();

	public abstract Boolean getBufferOutput();

	public abstract int getReturnType();

	public abstract String getReturnTypeAsString();

	public abstract String getDescription();

	public abstract int getReturnFormat();

	public abstract String getReturnFormatAsString();

	public abstract int getIndex();

	public abstract Object getCachedWithin();

	public abstract Boolean getSecureJson();

	public abstract Boolean getVerifyClient();

	public abstract FunctionArgument[] getFunctionArguments();

	public abstract String getDisplayName();

	public abstract String getHint();

	public abstract Struct getMeta();

	public abstract Integer getLocalMode();

	public abstract Set<Key> getArgumentsSet();
}
