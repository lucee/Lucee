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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.ForEachUtil;
import lucee.transformer.Body;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.VariableRef;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.statement.HasBody;

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
	 * @param start
	 * @param end
	 * @param label
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
		final int it = adapter.newLocal(Types.ITERATOR);
		final int item = adapter.newLocal(Types.REFERENCE);

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
		bc.visitLine(getStart());
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

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "ForOfStatement");

		// LDEV-6041: Preserve var keyword declaration
		if (key.getVariable().getScope() == Scope.SCOPE_VAR) {
			sct.setEL(KeyConstants._declaration, "var");
		}

		// label
		if ( label != null ) {
			sct.setEL(KeyConstants._label, label);
		}

		// left
		{
			Struct left = new StructImpl(Struct.TYPE_LINKED);
			key.dump(left);
			sct.setEL(KeyConstants._left, left);
		}
		// right
		{
			Struct right = new StructImpl(Struct.TYPE_LINKED);
			value.dump(right);
			sct.setEL(KeyConstants._right, right);
		}
		// body
		{
			Struct body = new StructImpl(Struct.TYPE_LINKED);
			this.body.dump(body);
			sct.setEL(KeyConstants._body, body);
		}
	}
}