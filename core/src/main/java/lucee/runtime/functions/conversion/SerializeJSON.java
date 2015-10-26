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
import lucee.runtime.PageContextImpl;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * Decodes Binary Data that are encoded as String
 */
public final class SerializeJSON implements Function {

	private static final long serialVersionUID = -4632952919389635891L;

	public static String call(PageContext pc, Object var) throws PageException {
		return _call(pc, var, false, pc.getWebCharset());
	}
	public static String call(PageContext pc, Object var,boolean serializeQueryByColumns) throws PageException {
		return _call(pc, var, serializeQueryByColumns, pc.getWebCharset());
	}
	public static String call(PageContext pc, Object var,boolean serializeQueryByColumns, String strCharset) throws PageException {
		Charset cs=StringUtil.isEmpty(strCharset)?pc.getWebCharset():CharsetUtil.toCharset(strCharset);
		return _call(pc, var, serializeQueryByColumns, cs);
	}
	private static String _call(PageContext pc, Object var,boolean serializeQueryByColumns, Charset charset) throws PageException {
		try {
            return new JSONConverter(true,charset).serialize(pc,var,serializeQueryByColumns);
        } catch (ConverterException e) {
            throw Caster.toPageException(e);
        }
	}
}