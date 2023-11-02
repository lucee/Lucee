package lucee.runtime.servlet.jsp;

import javax.servlet.jsp.tagext.BodyTag;

public interface BodyTagPro extends BodyTag, TagPro {

	public void hasBody(boolean hasBody);

}
