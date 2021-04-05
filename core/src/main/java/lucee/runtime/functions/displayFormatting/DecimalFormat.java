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
/**
 * Implements the CFML Function decimalformat
 */
package lucee.runtime.functions.displayFormatting;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;

public final class DecimalFormat implements Function {

	private static final long serialVersionUID = -2287888250117784383L;

	/*
	 * @param pc
	 * 
	 * @param object
	 * 
	 * @return
	 * 
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object) throws PageException {
		if (StringUtil.isEmpty(object)) object = Constants.DOUBLE_ZERO;
		return Caster.toDecimal(object, true);
	}
}