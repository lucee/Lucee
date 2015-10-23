package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.ComponentUtil;

public abstract class UDFPropertiesBase implements UDFProperties {

	private String id;

	public abstract String getFunctionName();

	public abstract boolean getOutput();

	public abstract Boolean getBufferOutput();

	public abstract int getReturnType();

	public abstract String getReturnTypeAsString();

	public abstract String getDescription();

	public abstract int getReturnFormat();

	public abstract String getReturnFormatAsString();

	public abstract int getIndex();

	public abstract PageSource getPageSource();
	protected abstract Page getPage();

	public abstract Object getCachedWithin();

	public abstract Boolean getSecureJson();

	public abstract Boolean getVerifyClient();

	public abstract FunctionArgument[] getFunctionArguments();

	public abstract String getDisplayName();

	public abstract String getHint();

	public abstract Struct getMeta();

	public abstract Integer getLocalMode();

	public abstract Set<Key> getArgumentsSet();

	public final Page getPage(PageContext pc) throws PageException {
		
		// MUST no page source
		if(getPageSource()!=null)return ComponentUtil.getPage(pc,getPageSource());
		if(getPage()!=null)return getPage();
		throw new ApplicationException("missing Page Source");
	}

	public final String id() {
		if(id==null) {
			// MUST no page source
			if(getPageSource()!=null) {
				id=getPageSource().getDisplayPath()+":"+getIndex();
			}
			else if(getPage()!=null) {
				// MUST id for Page
				id=getPage().hashCode()+":"+getIndex();
			}
		}
		return id;
	}
}
