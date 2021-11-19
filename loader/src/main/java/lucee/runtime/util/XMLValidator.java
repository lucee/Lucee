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
package lucee.runtime.util;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.XMLUtilImpl.XMLEntityResolverDefaultHandler;

class XMLValidator extends XMLEntityResolverDefaultHandler {

	@Override
	public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
		// print.out(publicID+":"+systemID);
		return super.resolveEntity(publicID, systemID);
	}

	private Array warnings;
	private Array errors;
	private Array fatals;
	private boolean hasErrors;
	private String strSchema;

	public XMLValidator(InputSource validator, String strSchema) {
		super(validator);
		this.strSchema = strSchema;
	}

	private void release() {
		warnings = null;
		errors = null;
		fatals = null;
		hasErrors = false;
	}

	@Override
	public void warning(SAXParseException spe) {
		log(spe, "Warning", warnings);
	}

	@Override
	public void error(SAXParseException spe) {
		hasErrors = true;
		log(spe, "Error", errors);
	}

	@Override
	public void fatalError(SAXParseException spe) throws SAXException {
		hasErrors = true;
		log(spe, "Fatal Error", fatals);
	}

	private void log(SAXParseException spe, String type, Array array) {
		StringBuffer sb = new StringBuffer("[" + type + "] ");

		String id = spe.getSystemId();
		if (!Util.isEmpty(id)) {
			int li = id.lastIndexOf('/');
			if (li != -1) sb.append(id.substring(li + 1));
			else sb.append(id);
		}
		sb.append(':');
		sb.append(spe.getLineNumber());
		sb.append(':');
		sb.append(spe.getColumnNumber());
		sb.append(": ");
		sb.append(spe.getMessage());
		sb.append(" ");
		array.appendEL(sb.toString());
	}

	public Struct validate(InputSource xml) throws PageException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		warnings = engine.getCreationUtil().createArray();
		errors = engine.getCreationUtil().createArray();
		fatals = engine.getCreationUtil().createArray();

		try {
			XMLReader parser = new XMLUtilImpl().createXMLReader("org.apache.xerces.parsers.SAXParser");
			parser.setContentHandler(this);
			parser.setErrorHandler(this);
			parser.setEntityResolver(this);
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
			// if(!validateNamespace)
			if (!Util.isEmpty(strSchema)) parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", strSchema);
			parser.parse(xml);
		}
		catch (SAXException e) {}
		catch (IOException e) {
			throw engine.getExceptionUtil().createXMLException(e.getMessage());
		}

		// result
		Struct result = engine.getCreationUtil().createStruct();
		result.setEL("warnings", warnings);
		result.setEL("errors", errors);
		result.setEL("fatalerrors", fatals);
		result.setEL("status", engine.getCastUtil().toBoolean(!hasErrors));
		release();
		return result;
	}

}