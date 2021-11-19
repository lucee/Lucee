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
package lucee.transformer.bytecode.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.op.Elvis;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;

public final class OpElvis extends ExpressionBase {

	private static final Type ELVIS = Type.getType(Elvis.class);
	public static final Method INVOKE_STR = new Method("operate", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.STRING_ARRAY });

	public static final Method INVOKE_KEY = new Method("operate", Types.BOOLEAN_VALUE, new Type[] { Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.COLLECTION_KEY_ARRAY });

	private Variable left;
	private Expression right;

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		if (ASMUtil.hasOnlyDataMembers(left)) return _writeOutPureDataMember(bc, mode);

		Label notNull = new Label();
		Label end = new Label();

		GeneratorAdapter ga = bc.getAdapter();

		int l = ga.newLocal(Types.OBJECT);
		ExpressionUtil.visitLine(bc, left.getStart());
		left.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, left.getEnd());
		ga.dup();
		ga.storeLocal(l);

		ga.visitJumpInsn(Opcodes.IFNONNULL, notNull);
		ExpressionUtil.visitLine(bc, right.getStart());
		right.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, right.getEnd());
		ga.visitJumpInsn(Opcodes.GOTO, end);
		ga.visitLabel(notNull);
		ga.loadLocal(l);
		ga.visitLabel(end);

		return Types.OBJECT;
	}

	public Type _writeOutPureDataMember(BytecodeContext bc, int mode) throws TransformerException {
		// TODO use function isNull for this
		GeneratorAdapter adapter = bc.getAdapter();

		Label yes = new Label();
		Label end = new Label();

		List<Member> members = left.getMembers();

		// to array
		Iterator<Member> it = members.iterator();
		List<DataMember> list = new ArrayList<DataMember>();
		while (it.hasNext()) {
			list.add((DataMember) it.next());
		}
		DataMember[] arr = list.toArray(new DataMember[members.size()]);

		ExpressionUtil.visitLine(bc, left.getStart());

		// public static boolean call(PageContext pc , double scope,String[] varNames)
		// pc
		adapter.loadArg(0);
		// scope
		adapter.push((double) left.getScope());
		// varNames

		// all literal string?
		boolean allLiteral = true;
		for (int i = 0; i < arr.length; i++) {
			if (!(arr[i].getName() instanceof Literal)) allLiteral = false;
		}

		ArrayVisitor av = new ArrayVisitor();
		if (!allLiteral) {
			// String Array
			av.visitBegin(adapter, Types.STRING, arr.length);
			for (int i = 0; i < arr.length; i++) {
				av.visitBeginItem(adapter, i);
				arr[i].getName().writeOut(bc, MODE_REF);
				av.visitEndItem(adapter);
			}
		}
		else {
			// Collection.Key Array
			av.visitBegin(adapter, Types.COLLECTION_KEY, arr.length);
			for (int i = 0; i < arr.length; i++) {
				av.visitBeginItem(adapter, i);
				getFactory().registerKey(bc, arr[i].getName(), false);
				av.visitEndItem(adapter);
			}
		}
		av.visitEnd();

		// allowNull
		// adapter.push(false);

		// ASMConstants.NULL(adapter);

		// call IsDefined.invoke
		adapter.invokeStatic(ELVIS, allLiteral ? INVOKE_KEY : INVOKE_STR);
		ExpressionUtil.visitLine(bc, left.getEnd());

		adapter.visitJumpInsn(Opcodes.IFEQ, yes);

		// left
		ExpressionUtil.visitLine(bc, left.getStart());
		left.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, left.getEnd());
		adapter.visitJumpInsn(Opcodes.GOTO, end);

		// right
		ExpressionUtil.visitLine(bc, right.getStart());
		adapter.visitLabel(yes);
		right.writeOut(bc, MODE_REF);
		ExpressionUtil.visitLine(bc, right.getEnd());
		adapter.visitLabel(end);

		return Types.OBJECT;

	}

	private OpElvis(Variable left, Expression right) {
		super(left.getFactory(), left.getStart(), right.getEnd());
		this.left = left;
		this.right = right;
	}

	public static Expression toExpr(Variable left, Expression right) {
		return new OpElvis(left, right);
	}
}