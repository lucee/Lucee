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

import lucee.runtime.PageContext;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class ToScript implements Function {

	public static String call(PageContext pc, Object var, String jsName) throws PageException {
		return call(pc, var, jsName, true, false);
	}

	public static String call(PageContext pc, Object var, String jsName, boolean outputFormat) throws PageException {
		return call(pc, var, jsName, outputFormat, false);
	}

	public static String call(PageContext pc, Object var, String jsName, boolean outputFormat, boolean asFormat) throws PageException {
		// if(!Decision.isVariableName(jsName))
		// throw new FunctionException(pc,"toScript",2,"jsName","value does not contain a valid variable
		// String");

		JSConverter converter = new JSConverter();
		converter.useShortcuts(asFormat);
		converter.useWDDX(outputFormat);

		try {
			return converter.serialize(var, jsName);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
		}
	}
}