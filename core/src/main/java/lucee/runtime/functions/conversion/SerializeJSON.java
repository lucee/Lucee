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
package lucee.runtime.functions.conversion;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

/**
 * Decodes Binary Data that are encoded as String
 */
public final class SerializeJSON implements Function {

	private static final long serialVersionUID = -4632952919389635891L;

	public static String call(PageContext pc, Object var) throws PageException {
		return _call(pc, var, "", pc.getWebCharset(), false);
	}

	public static String call(PageContext pc, Object var, Object queryFormat) throws PageException {
		return _call(pc, var, queryFormat, pc.getWebCharset(), false);
	}

	public static String call(PageContext pc, Object var, Object queryFormat, Object useSecureJSONPrefixOrCharset) throws PageException {
		// TODO all options to be a struct

		// useSecureJSONPrefix
		Charset cs = pc.getWebCharset();
		boolean useSecureJSONPrefix = false;
		if (Decision.isCastableToBoolean(useSecureJSONPrefixOrCharset)) {
			useSecureJSONPrefix = Caster.toBooleanValue(useSecureJSONPrefixOrCharset);
		}
		// charset
		else if (!StringUtil.isEmpty(useSecureJSONPrefixOrCharset)) {
			cs = CharsetUtil.toCharset(Caster.toString(useSecureJSONPrefixOrCharset));
		}
		return _call(pc, var, queryFormat, cs, useSecureJSONPrefix);
	}

	private static String _call(PageContext pc, Object var, Object queryFormat, Charset charset, boolean useSecureJSONPrefix) throws PageException {
		try {
			JSONConverter json = new JSONConverter(true, charset, JSONDateFormat.PATTERN_CF);

			int qf = JSONConverter.toQueryFormat(queryFormat, SerializationSettings.SERIALIZE_AS_UNDEFINED);
			if (qf == SerializationSettings.SERIALIZE_AS_UNDEFINED) {
				if (!StringUtil.isEmpty(queryFormat)) throw new FunctionException(pc, SerializeJSON.class.getSimpleName(), 2, "queryFormat",
						"When var is a Query, argument [queryFormat] must be either a boolean value or a string with the value of [struct], [row], or [column]");
				ApplicationContextSupport acs = (ApplicationContextSupport) pc.getApplicationContext();
				SerializationSettings settings = acs.getSerializationSettings();
				qf = settings.getSerializeQueryAs();
			}

			// TODO get secure prefix from application.cfc
			return useSecureJSONPrefix ? "// " + json.serialize(pc, var, qf) : json.serialize(pc, var, qf);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
		}
	}
}