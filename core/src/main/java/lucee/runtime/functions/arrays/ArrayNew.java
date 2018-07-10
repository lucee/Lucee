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
 * Implements the CFML Function arraynew
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ArrayUtil;

public final class ArrayNew extends BIF {

	private static final long serialVersionUID = -5923269433550568279L;

	public static Array call(PageContext pc) throws ExpressionException  {
		return new ArrayImpl();
	}
	
	public static Array call(PageContext pc , double dimension) throws ExpressionException {
		return ArrayUtil.getInstance((int)dimension);
	}
	
	public static Array call(PageContext pc , double dimension, boolean isSynchronized) throws ExpressionException {
		if(dimension>1)
			return ArrayUtil.getInstance((int)dimension);
		return new ArrayImpl(isSynchronized);
	}
	
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==0) return call(pc);
		if(args.length==1) return call(pc,Caster.toDoubleValue(args[0]));
		else throw new FunctionException(pc, "ArrayNew", 0, 1, args.length);
	}
	
}