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
 * Implements the CFML Function array
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.FunctionValueImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class Struct_ implements Function {

	private static final long serialVersionUID = 8708684598035273346L;

	public static Struct call(PageContext pc, Object[] objArr) throws PageException {
		return _call(objArr, "invalid argument for function struct, only named arguments are allowed like struct(name:\"value\",name2:\"value2\")", StructImpl.TYPE_UNDEFINED);
	}

	protected static Struct _call(Object[] objArr, String expMessage, int type) throws PageException {
		StructImpl sct = type < 0 ? new StructImpl() : new StructImpl(type);
		FunctionValueImpl fv;
		for (int i = 0; i < objArr.length; i++) {
			if (objArr[i] instanceof FunctionValue) {
				fv = ((FunctionValueImpl) objArr[i]);
				if (fv.getNames() == null) {
					sct.set(fv.getNameAsKey(), fv.getValue());
				}
				else {
					String[] arr = fv.getNames();
					Struct s = sct;
					for (int y = 0; y < arr.length - 1; y++) {
						s = touch(s, arr[y]);
					}
					s.set(KeyImpl.init(arr[arr.length - 1]), fv.getValue());
				}
			}
			else {
				throw new ExpressionException(expMessage);
			}
		}
		return sct;
	}

	private static Struct touch(Struct parent, String name) {
		Key key = KeyImpl.init(name.trim());
		Object obj = parent.get(key, null);
		if (obj instanceof Struct) return (Struct) obj;
		Struct sct = new StructImpl();
		parent.setEL(key, sct);
		return sct;
	}

}