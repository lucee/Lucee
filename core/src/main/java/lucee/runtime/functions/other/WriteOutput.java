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
 * Implements the CFML Function writeoutput
 */
package lucee.runtime.functions.other;

import java.io.IOException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.esapi.ESAPIUtil;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class WriteOutput extends BIF {

	public static boolean call(PageContext pc, String string) throws PageException {
		try {
			pc.forceWrite(string);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return true;
	}

	public static boolean call(PageContext pc, String string, String encodeFor) throws PageException {
		try {
			if (!StringUtil.isEmpty(string)) pc.forceWrite(ESAPIUtil.esapiEncode(pc, encodeFor, string));
			else pc.forceWrite(string);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "WriteOutput", 1, 2, args.length);
	}
}