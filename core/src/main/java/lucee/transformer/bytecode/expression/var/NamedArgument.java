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
package lucee.transformer.bytecode.expression.var;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.literal.Null;
import lucee.transformer.bytecode.literal.NullConstant;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;

public final class NamedArgument extends Argument {

	private static final int VALUE = 0;
	private static final int ARRAY = 1;
	private static final int KEY = 0;
	private static final int STRING = 1;

	private final static Method[][] NEW_INSTANCE = new Method[][] {
			new Method[] { new Method("newInstance", Types.FUNCTION_VALUE, new Type[] { Types.COLLECTION_KEY, Types.OBJECT }),
					new Method("newInstance", Types.FUNCTION_VALUE, new Type[] { Types.COLLECTION_KEY_ARRAY, Types.OBJECT }) },
			new Method[] { new Method("newInstance", Types.FUNCTION_VALUE, new Type[] { Types.STRING, Types.OBJECT }),
					new Method("newInstance", Types.FUNCTION_VALUE, new Type[] { Types.STRING_ARRAY, Types.OBJECT }) } };

	private Expression name;
	private boolean varKeyUpperCase;

	public NamedArgument(Expression name, Expression value, String type, boolean varKeyUpperCase) {
		super(value, type);
		this.name = name instanceof Null || name instanceof NullConstant ? name.getFactory().createLitString(varKeyUpperCase ? "NULL" : "null") : name;
		this.varKeyUpperCase = varKeyUpperCase;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {

		int form = VALUE;
		int type = STRING;
		if (name instanceof Variable && !((Variable) name).fromHash()) {
			GeneratorAdapter adapter = bc.getAdapter();
			String[] arr = VariableString.variableToStringArray((Variable) name, true);
			if (arr.length > 1) {
				form = ARRAY;
				ArrayVisitor av = new ArrayVisitor();
				av.visitBegin(adapter, Types.STRING, arr.length);
				for (int y = 0; y < arr.length; y++) {
					av.visitBeginItem(adapter, y);
					adapter.push(varKeyUpperCase ? arr[y].toUpperCase() : arr[y]);
					av.visitEndItem(bc.getAdapter());
				}
				av.visitEnd();
			}
			else {
				// VariableString.toExprString(name).writeOut(bc, MODE_REF);
				String str = VariableString.variableToString((Variable) name, true);
				name = bc.getFactory().createLitString(varKeyUpperCase ? str.toUpperCase() : str);
				getFactory().registerKey(bc, VariableString.toExprString(name), false);
				type = KEY;
			}
		}
		else {
			getFactory().registerKey(bc, name.getFactory().toExprString(name), false);
			type = KEY;
		}
		// name.writeOut(bc, MODE_REF);
		super._writeOut(bc, MODE_REF);
		// bc.getAdapter().push(variableString);
		bc.getAdapter().invokeStatic(Types.FUNCTION_VALUE_IMPL, NEW_INSTANCE[type][form]);
		return Types.FUNCTION_VALUE;
	}

	@Override
	public Type writeOutValue(BytecodeContext bc, int mode) throws TransformerException {
		return super.writeOutValue(bc, mode);
	}

	/**
	 * @return the name
	 */
	public Expression getName() {
		return name;
	}
}