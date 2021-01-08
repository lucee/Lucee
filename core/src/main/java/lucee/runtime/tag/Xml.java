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

import java.io.StringReader;

import org.xml.sax.InputSource;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;

/**
 * Creates a XML document object that contains the markup in the tag body. This tag can include XML
 * and CFML tags. the engine processes the CFML code in the tag body, then assigns the resulting
 * text to an XML document object variable.
 *
 *
 *
 **/
public final class Xml extends BodyTagImpl {

	/** name of an xml variable */
	private String variable;
	private String validator;

	/** yes: maintains the case of document elements and attributes */
	private boolean casesensitive;

	private String strXML;
	private boolean lenient = false;

	@Override
	public void release() {
		super.release();
		variable = null;
		casesensitive = false;
		strXML = null;
		validator = null;
		lenient = false;
	}

	/**
	 * set the value variable name of an xml variable
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * set the value casesensitive yes: maintains the case of document elements and attributes
	 * 
	 * @param casesensitive value to set
	 **/
	public void setCasesensitive(boolean casesensitive) {
		this.casesensitive = casesensitive;
	}

	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			InputSource vis = StringUtil.isEmpty(validator) ? null : XMLUtil.toInputSource(pageContext, validator);
			pageContext.setVariable(variable, XMLCaster.toXMLStruct(XMLUtil.parse(new InputSource(new StringReader(strXML)), vis, lenient), casesensitive));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		strXML = bodyContent.getString().trim();
		return SKIP_BODY;
	}

	/**
	 * @param validator the validator to set
	 */
	public void setValidator(String validator) {
		this.validator = validator;
	}
}