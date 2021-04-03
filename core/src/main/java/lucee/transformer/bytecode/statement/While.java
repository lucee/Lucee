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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import lucee.runtime.interpreter.ref.op.EQ;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.bytecode.util.Types;


public final class While extends StatementBaseNoFinal implements FlowControlBreak, FlowControlContinue, HasBody {

	private ExprBoolean expr;
	private Body body;

	private Label begin = new Label();
	private Label end = new Label();
	private String label;

	private final static Type TYPE_THREAD = Type.getType(Thread.class);
	private final static Type TYPE_EXCEPTION = Type.getType(InterruptedException.class);
	private final static Method METHOD_INTERRUPTED = new Method("interrupted", Type.BOOLEAN_TYPE, new Type[] {});

	/**
	 * Constructor of the class
	 * 
	 * @param expr
	 * @param body
	 * @param line
	 */
	public While(Expression expr, Body body, Position start, Position end, String label) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprBoolean(expr);
		this.body = body;
		body.setParent(this);
		this.label = label;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param b
	 * @param body
	 * @param line
	 */
	public While(boolean b, Body body, Position start, Position end, String label) {
		this(body.getFactory().createLitBoolean(b), body, start, end, label);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		final int toIt = adapter.newLocal(Types.ITERATOR);
		adapter.push(0);
		adapter.storeLocal(toIt, Type.INT_TYPE);
		adapter.visitLabel(begin);

		expr.writeOut(bc, Expression.MODE_VALUE);
		adapter.ifZCmp(Opcodes.IFEQ, end);

		body.writeOut(bc);

		// Check Once every 10K iteration
		adapter.iinc(toIt, 1);
		adapter.loadLocal(toIt);
		adapter.push(10000);
		adapter.ifICmp(Opcodes.IFLT, begin);
		// reset counter
		adapter.push(0);
		adapter.storeLocal(toIt);
		// Check if the thread is interrupted
		adapter.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to begin
		adapter.ifZCmp(Opcodes.IFEQ, begin);
		// Thread interrupted, throw Interrupted Exception
		adapter.throwException(TYPE_EXCEPTION, "Timeout in While loop");

		adapter.visitLabel(end);

		Label endPreempt = new Label();
		// Check if the thread is interrupted
		adapter.invokeStatic(TYPE_THREAD, METHOD_INTERRUPTED);
		// Thread hasn't been interrupted, go to endPreempt
		adapter.ifZCmp(Opcodes.IFEQ, endPreempt);
		// Thread interrupted, throw Interrupted Exception
		adapter.throwException(TYPE_EXCEPTION, "Timeout in For loop");
		// ExpressionUtil.visitLine(bc, getStartLine());
		adapter.visitLabel(endPreempt);		
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
	public String getLabel() {
		return label;
	}
}