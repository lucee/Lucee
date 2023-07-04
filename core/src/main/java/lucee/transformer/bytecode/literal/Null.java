/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.transformer.bytecode.literal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import lucee.runtime.type.scope.Scope;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.Variable;

public class Null extends ExpressionBase implements Literal {

	private static Map<Factory, Null> instances = new ConcurrentHashMap<>();

	public Null(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		ASMConstants.NULL(bc.getAdapter());
		return Types.OBJECT;
	}

	@Override
	public Number getNumber(Number defaultValue) {
		return null;
	}

	@Override
	public String getString() {
		return null;
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return null;
	}

	public Variable toVariable() {
		Variable v = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd());
		v.addMember(getFactory().createDataMember(getFactory().createLitString("null")));
		return v;
	}

	public static Null getSingleInstance(Factory f) {
		Null n = instances.get(f);
		if (n == null) {
			instances.put(f, n = new Null(f, null, null));
		}
		return n;
	}
}