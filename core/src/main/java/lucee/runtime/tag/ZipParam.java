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

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.UDFFilter;
import lucee.commons.io.res.util.WildcardPatternFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;

public final class ZipParam extends TagImpl {
	
	private String charset;
	private Object content;
	private String entryPath;
	private ResourceFilter filter;
	private String pattern;
	private String patternDelimiters;
	private String prefix;
	private lucee.commons.io.res.Resource source;
	private Boolean recurse=null;
	private Zip zip;
	


	@Override
	public void release()	{
		super.release();
		charset=null;
		content=null;
		entryPath=null;
		filter=null;
		prefix=null;
		source=null;
		recurse=null;
		zip=null;
		pattern = null;
		patternDelimiters = null;
	}
	
	
	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset=charset;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(Object content) {
		this.content=content;
	}

	/**
	 * @param entryPath the entryPath to set
	 */
	public void setEntrypath(String entryPath) {
		this.entryPath=entryPath;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Object filter) throws PageException {

		if (filter instanceof UDF)
			this.setFilter((UDF)filter);
		else if (filter instanceof String)
			this.setFilter((String)filter);
	}

	public void setFilter(UDF filter) throws PageException	{

		this.filter = UDFFilter.createResourceAndResourceNameFilter(filter);
	}

	public void setFilter(String pattern) {

		this.pattern = pattern;
	}

	public void setFilterdelimiters(String patternDelimiters) {

		this.patternDelimiters = patternDelimiters;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix=prefix;
	}

	/**
	 * @param strSource the source to set
	 * @throws PageException 
	 */
	public void setSource(String strSource) throws PageException {
		Resource zipSrc = getZip().getSource();
		if(zipSrc!=null)source=zipSrc.getRealResource(strSource);
		if(source==null || !source.exists())
			source=ResourceUtil.toResourceExisting(pageContext, strSource);
	}

	/**
	 * @param recurse the recurse to set
	 */
	public void setRecurse(boolean recurse) {
		this.recurse=Caster.toBoolean(recurse);
	}

	@Override
	public int doStartTag() throws PageException	{

		if (this.filter == null && !StringUtil.isEmpty(this.pattern))
			this.filter = new WildcardPatternFilter(pattern, patternDelimiters);
		
		if(source!=null) {
			notAllowed("source","charset", charset);
			notAllowed("source","content", content);
		
			getZip().setParam( new ZipParamSource( source, entryPath, filter, prefix, recurse() ) );
		}
		else if(content!=null) {
			required("content","entrypath",entryPath);
			notAllowed("content,entrypath","filter", filter);
			notAllowed("content,entrypath","prefix", prefix);
			notAllowed("content,entrypath","source", source);
			notAllowed("content,entrypath","recurse", recurse);
			
			getZip().setParam(new ZipParamContent(content,entryPath,charset));
		}
		/*else if(filter!=null) {
			notAllowed("filter","charset", charset);
			notAllowed("filter","content", content);
			notAllowed("filter","prefix", prefix);
			notAllowed("filter","source", source);
			getZip().setParam(new ZipParamFilter(filter,entryPath,recurse()));
		}
		else if(entryPath!=null) {
			notAllowed("entryPath","charset", charset);
			notAllowed("entryPath","content", content);
			notAllowed("entryPath","prefix", prefix);
			notAllowed("entryPath","source", source);
			getZip().setParam(new ZipParamFilter(filter,entryPath,recurse()));
		}*/
		else 
			throw new ApplicationException("invalid attribute combination");
			

		
		
		return SKIP_BODY;
	}

	private boolean recurse() {
		return recurse==null?true:recurse.booleanValue();
	}


	private Zip getZip() throws ApplicationException {
		if(zip!=null) return zip;
		Tag parent=getParent();
		while(parent!=null && !(parent instanceof Zip)) {
			parent=parent.getParent();
		}
		
		if(parent instanceof Zip) {
			return zip=(Zip)parent;
		}
		throw new ApplicationException("Wrong Context, tag ZipParam must be inside a Zip tag");	
	}


	private void notAllowed(String combi, String name, Object value) throws ApplicationException {
		if(value!=null)
			throw new ApplicationException("attribute ["+name+"] is not allowed in combination with attribute(s) ["+combi+"]");	
	}
	@Override
	public void required(String combi, String name, Object value) throws ApplicationException {
		if(value==null)
			throw new ApplicationException("attribute ["+name+"] is required in combination with attribute(s) ["+combi+"]");	
	}

	@Override
	public int doEndTag()	{
		return EVAL_PAGE;
	}
}