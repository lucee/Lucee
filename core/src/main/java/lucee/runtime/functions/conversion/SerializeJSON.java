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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.it.ForEachQueryIterator;

/**
 * Decodes Binary Data that are encoded as String
 */
public final class SerializeJSON implements Function {

	private static final long serialVersionUID = -4632952919389635891L;

	public static String call(PageContext pc, Object var) throws PageException {
		return _call(pc, var, false, pc.getWebCharset());
	}

	// FUTURE remove, this methods are only used by compiled code in archives older than 5.2.3
	public static String call(PageContext pc, Object var, boolean serializeQueryByColumns) throws PageException {
		return _call(pc, var, serializeQueryByColumns, pc.getWebCharset());
	}

	// FUTURE remove, this methods are only used by compiled code in archives older than 5.2.3
	public static String call(PageContext pc, Object var, boolean serializeQueryByColumns, String strCharset) throws PageException {
		Charset cs = StringUtil.isEmpty(strCharset) ? pc.getWebCharset() : CharsetUtil.toCharset(strCharset);
		return _call(pc, var, serializeQueryByColumns, cs);
	}

	public static String call(PageContext pc, Object var, Object options) throws PageException {
		return _call(pc, var, options, pc.getWebCharset());
	}

	public static String call(PageContext pc, Object var, Object options, String strCharset) throws PageException {
		Charset cs = StringUtil.isEmpty(strCharset) ? pc.getWebCharset() : CharsetUtil.toCharset(strCharset);
		return _call(pc, var, options, cs);
	}

	private static String _call(PageContext pc, Object var, Object options, Charset charset) throws PageException {
		try {

			JSONConverter json = new JSONConverter(true, charset);

			// default == false == row | true == column
			String sOpt = "";
			if(Decision.isSimpleValue(options))
				sOpt = Caster.toString(options);

			Boolean bOpt = null;
			if (Decision.isBoolean(options))
				bOpt = Caster.toBoolean(options);
			else if("row".equalsIgnoreCase(sOpt))
				bOpt = Boolean.FALSE;
			else if("column".equalsIgnoreCase(sOpt))
				bOpt = Boolean.TRUE;

			if(bOpt != null)
				return json.serialize(pc, var, bOpt);

			if(Decision.isQuery(var)) {
				if("struct".equalsIgnoreCase(sOpt)) {

					Array arr = new ArrayImpl();
					ForEachQueryIterator it = new ForEachQueryIterator((Query)var, pc.getId());
					try {
						while(it.hasNext()) {
							arr.append(it.next()); // append each record from the query as a struct
						}
					} finally {
						it.reset();
					}

					return json.serialize(pc, arr, false);
				}
				else throw new FunctionException(pc, SerializeJSON.class.getSimpleName(), 2, "options",
					"When var is a Query, argument [options] must be either a boolean value or a string with the value of [struct], [row], or [column]");
			}

			// var is not a query so options doesn't make a difference here
			return json.serialize(pc, var, false);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
		}
	}
}