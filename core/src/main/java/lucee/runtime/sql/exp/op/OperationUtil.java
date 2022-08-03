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
package lucee.runtime.sql.exp.op;

public class OperationUtil {
	public static String toString(int operator) {
		switch (operator) {
		case Operation.OPERATION2_DIVIDE:
			return "/";
		case Operation.OPERATION2_MINUS:
			return "-";
		case Operation.OPERATION2_MULTIPLY:
			return "*";
		case Operation.OPERATION2_PLUS:
			return "+";
		case Operation.OPERATION2_BITWISE:
			return "^";
		case Operation.OPERATION2_MOD:
			return "%";

		case Operation.OPERATION2_AND:
			return "and";
		case Operation.OPERATION2_OR:
			return "or";
		case Operation.OPERATION2_XOR:
			return "xor";

		case Operation.OPERATION2_EQ:
			return "=";
		case Operation.OPERATION2_GT:
			return ">";
		case Operation.OPERATION2_GTE:
			return ">=";
		case Operation.OPERATION2_LT:
			return "<";
		case Operation.OPERATION2_LTE:
			return "<=";
		case Operation.OPERATION2_LTGT:
			return "<>";
		case Operation.OPERATION2_NEQ:
			return "!=";
		case Operation.OPERATION2_NOT_LIKE:
			return "not like";
		case Operation.OPERATION2_LIKE:
			return "like";

		case Operation.OPERATION1_PLUS:
			return "+";
		case Operation.OPERATION1_MINUS:
			return "-";
		case Operation.OPERATION1_NOT:
			return "not";
		case Operation.OPERATION1_IS_NOT_NULL:
			return "is not null";
		case Operation.OPERATION1_IS_NULL:
			return "is null";
		}
		return null;
	}
}