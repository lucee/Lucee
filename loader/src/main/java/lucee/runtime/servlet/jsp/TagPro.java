package lucee.runtime.servlet.jsp;

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.exp.PageException;

public interface TagPro extends Tag {

	public void setAppendix(String appendix);
	
	public void setMetaData(String name, Object value);
}
