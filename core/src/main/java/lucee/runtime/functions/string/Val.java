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
 * Implements the CFML Function val
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class Val implements Function {

	private static final long serialVersionUID = -4333040593277864043L;

	public static double call(PageContext pc, String value) throws PageException {
		if (value == null) return 0;
		value = value.trim();
		int pos = getPos(value);
		if (pos <= 0) return 0;
		return Caster.toDoubleValue(value.substring(0, pos));
	}

	private static int getPos(String str) {
		if (str == null) return 0;

		int pos = 0;
		int len = str.length();
		if (len == 0) return 0;
		char curr = str.charAt(pos);

		if (curr == '+' || curr == '-') {
			if (len == ++pos) return 0;
			curr = str.charAt(pos);
		}

		// at least one digit
		if (curr >= '0' && curr <= '9') {
			curr = str.charAt(pos);
		}
		else if (curr == '.') {
			curr = '.';
		}
		else return 0;

		boolean hasDot = false;
		// boolean hasExp=false;
		for (; pos < len; pos++) {
			curr = str.charAt(pos);
			if (curr < '0') {
				if (curr == '.') {
					if (pos + 1 >= len || hasDot) return pos;
					hasDot = true;
				}
				else return pos;
			}
			else if (curr > '9') {
				/*
				 * if(curr=='e' || curr=='E') { if(pos+1>=len || hasExp) return pos; hasExp=true; hasDot=true; }
				 * else
				 */
				return pos;
			}
		}

		return pos;
	}
}