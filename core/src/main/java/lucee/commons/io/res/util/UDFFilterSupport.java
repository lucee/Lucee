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
package lucee.commons.io.res.util;

import lucee.commons.lang.CFTypes;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;

public abstract class UDFFilterSupport {

	protected UDF udf;
	protected Object[] args = new Object[1];

	public UDFFilterSupport(UDF udf) throws ExpressionException {
		this.udf = udf;

		// check UDF return type
		int type = udf.getReturnType();
		if (type != CFTypes.TYPE_BOOLEAN && type != CFTypes.TYPE_ANY)
			throw new ExpressionException("invalid return type [" + udf.getReturnTypeAsString() + "] for UDF Filter, valid return types are [boolean,any]");

		// check UDF arguments
		FunctionArgument[] args = udf.getFunctionArguments();
		if (args.length > 1) throw new ExpressionException("UDF filter has to many arguments [" + args.length + "], should have at maximum 1 argument");

		if (args.length == 1) {
			type = args[0].getType();
			if (type != CFTypes.TYPE_STRING && type != CFTypes.TYPE_ANY)
				throw new ExpressionException("invalid type [" + args[0].getTypeAsString() + "] for first argument of UDF Filter, valid return types are [string,any]");
		}

	}

	@Override
	public String toString() {
		return "UDFFilter:" + udf;
	}
}