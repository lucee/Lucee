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
package lucee.runtime.helpers;

import java.io.InputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;

/**
 * Sax Parse with callback to CFC Methods
 */
public final class XMLEventParser extends DefaultHandler {

	private UDF startDocument;
	private UDF startElement;
	private UDF body;
	private UDF endElement;
	private UDF endDocument;
	private UDF error;

	private Stack<StringBuilder> bodies = new Stack<StringBuilder>();
	private PageContext pc;
	private Struct att;

	/**
	 * Field <code>DEFAULT_SAX_PARSER</code>
	 */

	/**
	 * constructor of the class
	 * 
	 * @param pc
	 * @param startDocument
	 * @param startElement
	 * @param body
	 * @param endElement
	 * @param endDocument
	 * @param error
	 */
	public XMLEventParser(PageContext pc, UDF startDocument, UDF startElement, UDF body, UDF endElement, UDF endDocument, UDF error) {

		this.pc = pc;
		this.startDocument = startDocument;
		this.startElement = startElement;
		this.body = body;
		this.endElement = endElement;
		this.endDocument = endDocument;
		this.error = error;

	}

	/**
	 * start execution of the parser
	 * 
	 * @param xmlFile
	 * @param saxParserCass
	 * @throws PageException
	 */
	public void start(Resource xmlFile) throws PageException {
		InputStream is = null;
		try {
			XMLReader xmlReader = XMLUtil.createXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
			xmlReader.parse(new InputSource(is = IOUtil.toBufferedInputStream(xmlFile.getInputStream())));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			IOUtil.closeEL(is);
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		bodies.peek().append(ch, start, length);
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		error(Caster.toPageException(e));
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		error(Caster.toPageException(e));
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		bodies.add(new StringBuilder());
		att = toStruct(attributes);
		call(startElement, new Object[] { uri, StringUtil.isEmpty(localName) ? qName : localName, qName, att });
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		call(body, new Object[] { bodies.pop().toString() });
		call(endElement, new Object[] { uri, StringUtil.isEmpty(localName) ? qName : localName, qName, att });
	}

	@Override
	public void startDocument() throws SAXException {
		call(startDocument, ArrayUtil.OBJECT_EMPTY);
	}

	@Override
	public void endDocument() throws SAXException {
		call(endDocument, ArrayUtil.OBJECT_EMPTY);
	}

	/**
	 * call a user defined function
	 * 
	 * @param udf
	 * @param arguments
	 */
	private void call(UDF udf, Object[] arguments) {
		try {
			udf.call(pc, arguments, false);
		}
		catch (PageException pe) {
			error(pe);
		}
	}

	/**
	 * call back error function if an error occours
	 * 
	 * @param pe
	 */
	private void error(PageException pe) {
		if (error == null) throw new PageRuntimeException(pe);
		try {
			pc = ThreadLocalPageContext.get(pc);
			error.call(pc, new Object[] { pe.getCatchBlock(pc.getConfig()) }, false);
		}
		catch (PageException e) {}
	}

	/**
	 * cast an Attributes object to a Struct
	 * 
	 * @param att
	 * @return Attributes as Struct
	 */
	private Struct toStruct(Attributes att) {
		int len = att.getLength();
		Struct sct = new StructImpl();
		for (int i = 0; i < len; i++) {
			sct.setEL(att.getQName(i), att.getValue(i));
		}
		return sct;
	}
}