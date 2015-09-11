/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.tag;

import java.io.IOException;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.StringUtil;
import lucee.commons.pdf.PDFPageMark;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;

public final class DocumentItem extends BodyTagImpl {

	private static final int TYPE_PAGE_BREAK = 0;
	private static final int TYPE_HEADER = 1;
	private static final int TYPE_FOOTER = 2;
	private static final int TYPE_BOOKMARK = 3;

	private int type;
	private String name;
	private PDFPageMark body;
	private boolean evalAtPrint;
	
	@Override
	public void release() {
		super.release();
		this.body=null;
		name=null;
	}

	/**
	 * @param type the type to set
	 * @throws ApplicationException 
	 */
	public void setType(String strType) throws ApplicationException {
		strType=StringUtil.toLowerCase(strType.trim());
		if("pagebreak".equals(strType))		type=TYPE_PAGE_BREAK;
		else if("header".equals(strType))	type=TYPE_HEADER;
		else if("footer".equals(strType))	type=TYPE_FOOTER;
		else if("bookmark".equals(strType))	type=TYPE_BOOKMARK;
		else throw new ApplicationException("invalid type ["+strType+"], valid types are [pagebreak,header,footer,bookmark]");
		//else throw new ApplicationException("invalid type ["+strType+"], valid types are [pagebreak,header,footer]");
		
	}

	public void setEvalatprint(boolean evalAtPrint){
		this.evalAtPrint=evalAtPrint;
	}

    @Override
	public int doStartTag()	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody()	{}
	
	@Override
	public int doAfterBody()	{
		if(TYPE_HEADER==type || TYPE_FOOTER==type) {
			body=new PDFPageMark(-1,translate(bodyContent.getString()));
		}
		
		return SKIP_BODY;
	}
	
	private String translate(String html) {
		html=StringUtil.replace(html.trim(), "{currentsectionpagenumber}", "${page}", false);
		html=StringUtil.replace(html, "{totalsectionpagecount}", "${total}", false);
		
		html=StringUtil.replace(html.trim(), "{currentpagenumber}", "${page}", false);
		html=StringUtil.replace(html, "{totalpagecount}", "${total}", false);
		

	    //cfdoc.setEL("currentpagenumber", "{currentpagenumber}");
	    //cfdoc.setEL("totalpagecount", "{totalpagecount}");
	    
		
		return html;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}	
		return EVAL_PAGE;
	}
	private void _doEndTag() throws IOException, ApplicationException {
		if(TYPE_PAGE_BREAK==type) {
			pageContext.forceWrite("<pd4ml:page.break>");
			return;
		}
		else if(TYPE_BOOKMARK==type) {
			if(StringUtil.isEmpty(name))
				throw new ApplicationException("attribute [name] is required when type is [bookmark]");
			pageContext.forceWrite("<pd4ml:bookmark>"+name+"</pd4ml:bookmark>");
		}
		else if(body!=null) {
			provideDocumentItem();
		}
		
	}

	private void provideDocumentItem() 	{
		// get Document Tag
		Tag parent=getParent();
		while(parent!=null && !(parent instanceof Document) && !(parent instanceof DocumentSection)) {
			parent=parent.getParent();
		}

		if(parent instanceof Document) {
			Document doc = (Document)parent;
			if(TYPE_HEADER==type)doc.setHeader(body);
			else if(TYPE_FOOTER==type)doc.setFooter(body);
			return ;
		}
		else if(parent instanceof DocumentSection) {
			DocumentSection doc = (DocumentSection)parent;
			if(TYPE_HEADER==type)doc.setHeader(body);
			else if(TYPE_FOOTER==type)doc.setFooter(body);
			return ;
		}
	}
	
	/**
	 * sets if has body or not
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
	    
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}