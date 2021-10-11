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
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.arrays.ArrayFind;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class ArrayRemoveDuplicates extends BIF {
    
    public static Array call(PageContext pc, Array arr) throws PageException {
            return call(pc, arr, false);
    }

    public static Array call(PageContext pc, Array arr, boolean ignoreCase) throws PageException {
        Array a = new ArrayImpl();
        int i;
        for(i=1; i <= arr.size(); i++) {
            Object value = arr.getE(i);
            if(ArrayFind.find(a, value, !ignoreCase) == 0) a.appendEL(value);
        }
        return a; 
    }

    @Override
    public Object invoke(PageContext pc, Object[] args) throws PageException {
        if (args.length == 1) return call(pc, Caster.toArray(args[0]));
        else if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toBoolean(args[1]));
        else throw new FunctionException(pc, "ArrayRemoveDuplicates", 1, 2 , args.length);
    }
}