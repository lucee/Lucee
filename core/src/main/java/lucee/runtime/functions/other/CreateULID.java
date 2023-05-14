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

package lucee.runtime.functions.other;

import com.github.f4b6a3.ulid.UlidCreator;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.op.Caster;

/**
 * Implements the CFML Function createulid
 */
public final class CreateULID implements Function {

	/**
	 * method to invoke the function
	 * 
	 * @param pc
	 * @return ULID String
	 */
	public static String call(PageContext pc) throws PageException {
		return invoke(pc, null, -1, null);
	}

	public static String call(PageContext pc, String type) throws PageException {
		return invoke(pc, type, -1, null);
	}

	public static String call(PageContext pc, String type, double input1, String input2) throws PageException {
		return invoke(pc, type, input1, input2);
	}

	public static String invoke(PageContext pc, String type, double input1, String input2) throws PageException{
		if (type == null) return UlidCreator.getUlid().toString();
		else if ("monotonic".equalsIgnoreCase(type)) return UlidCreator.getMonotonicUlid().toString();
		else if ("hash".equalsIgnoreCase(type)) return UlidCreator.getHashUlid( Caster.toLong(input1), input2 ).toString();
		else throw new FunctionException(pc, "CreateULID", 1, "type", "Type [" + type + "] is not supported.");
	}
}