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
		try {

			// no validator
			if (StringUtil.isEmpty(objValidator)) {
				InputSource xml = XMLUtil.toInputSource(pc, strXml.trim());
				return XMLUtil.validate(xml, null, null, null);
			}

			// single validator
			if (!Decision.isArray(objValidator)) {
				InputSource xml = XMLUtil.toInputSource(pc, strXml.trim());
				String strValidator = Caster.toString(objValidator);
				return XMLUtil.validate(xml, XMLUtil.toInputSource(pc, strValidator), strValidator, null);
			}

			// multiple validators
			Struct result = null;
			String[] strValidators = ListUtil.toStringArray(Caster.toArray(objValidator));
			for (String strValidator: strValidators) {
				InputSource xml = XMLUtil.toInputSource(pc, strXml.trim());
				result = XMLUtil.validate(xml, XMLUtil.toInputSource(pc, strValidator), strValidator, result);
			}
			return result;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}
}