/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Elvis;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;

public final class OpElvis extends ExpressionBase {

	private static final Type ELVIS = Type.getType(Elvis.class);

	public static final Method INVOKE_STR = new Method("load", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.STRING_ARRAY });
	public static final Method INVOKE_KEY = new Method("load", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.COLLECTION_KEY_ARRAY });

	private Variable left;
	private Expression right;

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		if (ASMUtil.hasOnlyDataMembers(left)) return _writeOutPureDataMember(bc, mode);

		String name = createRandom(bc);
		GeneratorAdapter ga = bc.getAdapter();
		ga.loadThis(); // Load 'this' onto the stack
		ga.loadArg(0);
		ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc.getClassName(), name, "(Llucee/runtime/PageContext;)Ljava/lang/Object;", false);

		return Types.OBJECT;
	}

	private String createRandom(BytecodeContext parent) throws TransformerException {
		String name = "el" + CreateUniqueId.invoke().toUpperCase() + Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);

		Method m = new Method(name, Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });
		GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, m, null, new Type[] { Types.THROWABLE }, parent.getClassWriter());
		BytecodeContext bc = new BytecodeContext(parent.getConstructor(), parent.getKeys(), parent, ga, m);

		Label tryStart = new Label();
		Label tryEnd = new Label();
		Label catchBlock = new Label();
		Label returnDefault = new Label();

		ga.mark(tryStart);
		// left
		bc.visitLine(left.getStart());
		left.writeOut(bc, MODE_REF);
		bc.visitLine(left.getEnd());

		ga.dup();
		ga.visitJumpInsn(Opcodes.IFNULL, catchBlock);
		// ga.loadLocal(localVal);
		ga.returnValue();
		ga.mark(tryEnd);
		ga.goTo(returnDefault);

		ga.mark(catchBlock);
		ga.pop();
		ga.goTo(returnDefault);

		ga.visitTryCatchBlock(tryStart, tryEnd, catchBlock, "java/lang/Exception");

		ga.mark(returnDefault);
		bc.visitLine(right.getStart());
		right.writeOut(bc, MODE_REF);
		bc.visitLine(right.getEnd());
		ga.returnValue();

		ga.endMethod();

		return name;
	}

	public Type _writeOutPureDataMember(BytecodeContext bc, int mode) throws TransformerException {
		// TODO use function isNull for this
		GeneratorAdapter adapter = bc.getAdapter();

		Label end = new Label();
		Label elseLabel = adapter.newLabel();

		List<Member> members = left.getMembers();

		// to array
		Iterator<Member> it = members.iterator();
		List<DataMember> list = new ArrayList<DataMember>();
		while (it.hasNext()) {
			list.add((DataMember) it.next());
		}
		DataMember[] arr = list.toArray(new DataMember[members.size()]);

		bc.visitLine(left.getStart());

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

		adapter.invokeStatic(ELVIS, allLiteral ? INVOKE_KEY : INVOKE_STR);
		adapter.dup(); // duplicate the result on the stack
		bc.visitLine(left.getEnd());

		// If the result is null, jump to 'elseLabel'
		adapter.visitJumpInsn(Opcodes.IFNULL, elseLabel);

		// bcause we did a dup above there is no further action needed

		// Jump to 'endLabel', skipping the 'else' part
		adapter.goTo(end);

		// right
		adapter.mark(elseLabel);
		adapter.pop(); // Remove the duplicated null value from the stack
		bc.visitLine(right.getStart());
		right.writeOut(bc, MODE_REF);
		bc.visitLine(right.getEnd());

		adapter.mark(end);

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