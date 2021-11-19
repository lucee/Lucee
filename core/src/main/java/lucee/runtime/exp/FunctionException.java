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
package lucee.runtime.exp;

import java.util.ArrayList;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

/**
 * specified exception for Built-In Function
 */
public final class FunctionException extends ExpressionException {

	/*
	 * * constructor of the class
	 * 
	 * @param pc current Page Context
	 * 
	 * @param functionName Name of the function that thorw the Exception
	 * 
	 * @param badArgumentPosition Position of the bad argument in the Argument List of the function
	 * 
	 * @param badArgumentName Name of the bad Argument
	 * 
	 * @param message additional Exception message / public FunctionException(PageContext pc,String
	 * functionName, String badArgumentPosition, String badArgumentName, String message) {
	 * this((PageContext)pc,functionName,badArgumentPosition,badArgumentName,message); }
	 */

	/**
	 * constructor of the class
	 * 
	 * @param pc current Page Context
	 * @param functionName Name of the function that thorw the Exception
	 * @param badArgumentPosition Position of the bad argument in the Argument List of the function
	 * @param badArgumentName Name of the bad Argument
	 * @param message additional Exception message
	 */
	public FunctionException(PageContext pc, String functionName, int badArgumentPosition, String badArgumentName, String message) {
		this(pc, functionName, toStringBadArgumentPosition(badArgumentPosition), badArgumentName, message, null);
	}

	public FunctionException(PageContext pc, String functionName, int badArgumentPosition, String badArgumentName, String message, String detail) {
		this(pc, functionName, toStringBadArgumentPosition(badArgumentPosition), badArgumentName, message, detail);
	}

	private static String toStringBadArgumentPosition(int pos) {
		switch (pos) {
		case 1:
			return "first";
		case 2:
			return "second";
		case 3:
			return "third";
		case 4:
			return "forth";
		case 5:
			return "fifth";
		case 6:
			return "sixth";
		case 7:
			return "seventh";
		case 8:
			return "eighth";
		case 9:
			return "ninth";
		case 10:
			return "tenth";
		case 11:
			return "eleventh";
		case 12:
			return "twelfth";
		}
		// TODO Auto-generated method stub
		return pos + "th";
	}

	public FunctionException(PageContext pc, String functionName, String badArgumentPosition, String badArgumentName, String message, String detail) {
		super("Invalid call of the function [" + functionName + "], " + (badArgumentPosition) + " Argument [" + badArgumentName + "] is invalid, " + message, detail);
		setAdditional(KeyConstants._pattern, getFunctionInfo(pc, functionName));
	}

	public FunctionException(PageContext pc, String functionName, int min, int max, int actual) {
		super(actual < min ? "too few arguments for function [" + functionName+ "] call" : "too many arguments for function [" + functionName+ "] call");
	}

	private static String getFunctionInfo(PageContext pc, String functionName) {
		FunctionLib[] flds;
		int dialect = pc.getCurrentTemplateDialect();
		flds = ((ConfigPro) pc.getConfig()).getFLDs(dialect);

		FunctionLibFunction function = null;
		for (int i = 0; i < flds.length; i++) {
			function = flds[i].getFunction(functionName.toLowerCase());
			if (function != null) break;
		}
		if (function == null) return "";

		StringBuilder rtn = new StringBuilder();
		rtn.append(function.getName() + "(");

		int optionals = 0;
		ArrayList<FunctionLibFunctionArg> args = function.getArg();
		for (int i = 0; i < args.size(); i++) {
			FunctionLibFunctionArg arg = args.get(i);
			if (i != 0) rtn.append(", ");
			if (!arg.getRequired()) {
				rtn.append("[");
				optionals++;
			}
			rtn.append(arg.getName());
			rtn.append(":");
			rtn.append(arg.getTypeAsString());
		}
		for (int i = 0; i < optionals; i++)
			rtn.append("]");
		rtn.append("):" + function.getReturnTypeAsString());

		return rtn.toString();
	}

}