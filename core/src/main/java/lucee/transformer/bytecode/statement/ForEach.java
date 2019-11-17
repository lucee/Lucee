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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.util.ForEachUtil;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.VariableRef;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.PageContext;

public final class ForEach extends StatementBase implements FlowControlBreak, FlowControlContinue, HasBody {

	private Body body;
	private VariableRef key;
	private Expression value;

	private final static Method HAS_NEXT = new Method("hasNext", Types.BOOLEAN_VALUE, new Type[] {});
	private final static Method NEXT = new Method("next", Types.OBJECT, new Type[] {});
	private final static Method SET = new Method("set", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	public static final Method LOOP_COLLECTION = new Method("loopCollection", Types.ITERATOR, new Type[] { Types.OBJECT });
	public static final Method FOR_EACH = new Method("forEach", Types.ITERATOR, new Type[] { Types.OBJECT });

	public static final Type FOR_EACH_UTIL = Type.getType(ForEachUtil.class);
	public static final Method RESET = new Method("reset", Types.VOID, new Type[] { Types.ITERATOR });

	private static final Type TYPE_PCU = Type.getType(PageContextUtil.class);
	private static final Type TYPE_PC = Type.getType(PageContext.class);
	private static final Method METHOD_CRT = new Method("checkRequestTimeout", Type.VOID_TYPE, new Type[] {TYPE_PC});

	// private static final Type COLLECTION_UTIL = Type.getType(CollectionUtil.class);

	private Label begin = new Label();
	private Label end = new Label();
	private FlowControlFinal fcf;
	private String label;

	/**
	 * Constructor of the class
	 * 
	 * @param key
	 * @param value
	 * @param body
	 * @param line
	 */
	public ForEach(Variable key, Expression value, Body body, Position start, Position end, String label) {
		super(key.getFactory(), start, end);
		this.key = new VariableRef(key, false);
		this.value = value;
		this.body = body;
		this.label = label;
		body.setParent(this);

	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		final int toIt = adapter.newLocal(Types.ITERATOR);
		final int it = adapter.newLocal(Types.ITERATOR);
		final int item = adapter.newLocal(Types.REFERENCE);

		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);

		// Value
		// ForEachUtil.toIterator(value)
		value.writeOut(bc, Expression.MODE_REF);
		adapter.invokeStatic(FOR_EACH_UTIL, FOR_EACH);
		// adapter.invokeStatic(COLLECTION_UTIL, TO_ITERATOR);
		// Iterator it=...
		adapter.storeLocal(it);
		TryFinallyVisitor tfv = new TryFinallyVisitor(new OnFinally() {

			@Override
			public void _writeOut(BytecodeContext bc) throws TransformerException {
				GeneratorAdapter a = bc.getAdapter();
				// if(fcf!=null &&
				// fcf.getAfterFinalGOTOLabel()!=null)ASMUtil.visitLabel(a,fcf.getFinalEntryLabel());
				a.loadLocal(it);
				a.invokeStatic(FOR_EACH_UTIL, RESET);
				/*
				 * if(fcf!=null){ Label l=fcf.getAfterFinalGOTOLabel(); if(l!=null)a.visitJumpInsn(Opcodes.GOTO, l);
				 * }
				 */
			}
		}, getFlowControlFinal());
		tfv.visitTryBegin(bc);
		// Key
		// new VariableReference(...)
		key.writeOut(bc, Expression.MODE_REF);
		// VariableReference item=...
		adapter.storeLocal(item);

		// while
		ExpressionUtil.visitLine(bc, getStart());
		adapter.visitLabel(begin);

		// hasNext
		adapter.loadLocal(it);
		adapter.invokeInterface(Types.ITERATOR, HAS_NEXT);
		adapter.ifZCmp(Opcodes.IFEQ, end);

		// item.set(pc,it.next());
		adapter.loadLocal(item);
		adapter.loadArg(0);
		adapter.loadLocal(it);
		adapter.invokeInterface(Types.ITERATOR, NEXT);
		adapter.invokeInterface(Types.REFERENCE, SET);
		adapter.pop();

		// Body
		body.writeOut(bc);

		// only test timeout once out of 10K iteration (performance optimization)
		adapter.iinc(toIt, 1);
		adapter.loadLocal(toIt);
		adapter.push(10000);
		adapter.ifICmp(Opcodes.IFLT, begin);
		// reset counter
		adapter.push(0);
		adapter.storeLocal(toIt);
		// Check if the thread is timedout
		adapter.loadArg(0);
		adapter.invokeStatic(TYPE_PCU, METHOD_CRT);

		adapter.visitJumpInsn(Opcodes.GOTO, begin);
		adapter.visitLabel(end);
		tfv.visitTryEnd(bc);

	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return begin;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		if (fcf == null) fcf = new FlowControlFinalImpl();
		return fcf;
	}

	@Override
	public String getLabel() {
		return label;
	}
}