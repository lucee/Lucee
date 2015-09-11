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

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.ext.tag.TagImpl;

/**
* Required for cfhttp POST operations, cfhttpparam is used to specify the parameters necessary to 
* 	 build a cfhttp POST.
*
*
*
**/
public final class PDFParam extends TagImpl {
	
	PDFParamBean param=new PDFParamBean();


	/**
	 * @param pages the pages to set
	 */
	public void setPages(String pages) {
		param.setPages(pages);
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		param.setPassword(password);
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Object source) {
		param.setSource(source);
	}
	
	
	@Override
	public int doStartTag() throws ApplicationException	{
        
        
		// get HTTP Tag
		Tag parent=getParent();
		while(parent!=null && !(parent instanceof PDF)) {
			parent=parent.getParent();
		}
		
		if(parent instanceof PDF) {
			PDF pdf = (PDF)parent;
			pdf.setParam(param);
		}
		else {
			throw new ApplicationException("Wrong Context, tag PDFParam must be inside a PDF tag");	
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag()	{
		return EVAL_PAGE;
	}

	@Override
	public void release()	{
		super.release();
		param=new PDFParamBean();
	}
}