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
package lucee.runtime.functions.xml;

import org.xml.sax.InputSource;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class XmlValidate implements Function {

	private static final long serialVersionUID = 3566454779506863837L;

	public static Struct call(PageContext pc, String strXml) throws PageException {
		return call(pc, strXml, null);
	}

	public static Struct call(PageContext pc, String strXml, Object objValidator) throws PageException {
		strXml = strXml.trim();
		try {
			InputSource xml = XMLUtil.toInputSource(pc, strXml);
			InputSource[] validators;
			if (StringUtil.isEmpty(objValidator)) validators = null;
			else {
				String[] strValidators;
				if (Decision.isArray(objValidator)) strValidators = ListUtil.toStringArray(Caster.toArray(objValidator));
				else strValidators = new String[] { Caster.toString(objValidator) };
				// else strValidators = ListUtil.listToStringArray(Caster.toString(objValidator), ',');

				validators = new InputSource[strValidators.length];
				for (int i = 0; i < validators.length; i++) {
					validators[i] = XMLUtil.toInputSource(pc, strValidators[i]);
				}
			}

			return XMLUtil.validate(xml, validators, null);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	/*
	 * public static void main(String[] args) throws IOException, XMLException { String xmlPath =
	 * "/Users/mic/Test/test/webapps/ROOT/validate/test.xml"; FileReader xmlFR = new FileReader(new
	 * File(xmlPath)); InputSource xmlIS = new InputSource(xmlFR);
	 * 
	 * String xsdPath = "/Users/mic/Test/test/webapps/ROOT/validate/test.xsd"; FileReader xsdFR = new
	 * FileReader(new File(xsdPath)); InputSource xsdIS = new InputSource(xsdFR);
	 * 
	 * String xsd2Path = "/Users/mic/Test/test/webapps/ROOT/validate/avs.xsd"; FileReader xsd2FR = new
	 * FileReader(new File(xsd2Path)); InputSource xsd2IS = new InputSource(xsd2FR); try {
	 * print.e(XMLUtil.validate(xmlIS, new InputSource[] { xsdIS, xsd2IS }, xsdPath)); //
	 * print.e(XMLUtil.validate(xmlIS, null, null)); } finally { IOUtil.closeEL(xmlFR);
	 * IOUtil.closeEL(xsdFR); }
	 * 
	 * }
	 */
}