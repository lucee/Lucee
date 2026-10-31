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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.LocalExpression;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.statement.HasBody;

public final class For extends StatementBaseNoFinal implements FlowControlBreak, FlowControlContinue, HasBody {

	private Expression init;
	private Expression condition;
	private Expression update;
	private Body body;

	// private static final int I=1;

	private static final Method IS_LOOP_COUNTER = new Method("isLoopCounter", Type.BOOLEAN_TYPE, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	private static final Method COMPARE_DOUBLE = new Method("compareLoop", Type.INT_TYPE, new Type[] { Type.DOUBLE_TYPE, Type.DOUBLE_TYPE });
	private static final Method COMPARE_DOUBLE_OBJECT = new Method("compareLoop", Type.INT_TYPE, new Type[] { Types.PAGE_CONTEXT, Type.DOUBLE_TYPE, Types.OBJECT });
	private static final Method DOUBLE_VALUE = new Method("doubleValue", Type.DOUBLE_TYPE, new Type[] {});
	private static final Method VALUE_OF = new Method("valueOf", Types.DOUBLE, new Type[] { Type.DOUBLE_TYPE });

	Label beforeUpdate = new Label();
	Label end = new Label();
	private String label;

	/**
	 * Constructor of the class
	 * 
	 * @param f
	 * @param init
	 * @param condition
	 * @param update
	 * @param body
	 * @param start
	 * @param end
	 * @param label
	 */
	public For(Factory f, Expression init, Expression condition, Expression update, Body body, Position start, Position end, String label) {
		super(f, start, end);
		this.init = init;
		this.condition = condition;
		this.update = update;
		this.body = body;
		this.label = label;
		body.setParent(this);

	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		beforeUpdate = new Label(); // LDEV-5922 fresh labels per emission, a finally body is written more than once
		end = new Label();
		GeneratorAdapter adapter = bc.getAdapter();

		// LDEV-4473 a plain counting loop (for (i = 1; i <= n; i++)) keeps its counter in a double
		ForCounter counter = ForCounter.detect(condition, update, body);
		if (counter != null) {
			_writeOutCounter(bc, counter);
			return;
		}

		Label beforeInit = new Label();
		Label afterInit = new Label();
		Label afterUpdate = new Label();

		bc.visitLine(getStart());
		adapter.visitLabel(beforeInit);
		if (init != null) {
			init.writeOut(bc, Expression.MODE_VALUE);
			adapter.pop();
		}
		adapter.visitJumpInsn(Opcodes.GOTO, afterUpdate);
		adapter.visitLabel(afterInit);

		body.writeOut(bc);

		adapter.visitLabel(beforeUpdate);
		// ExpressionUtil.visitLine(bc, getStartLine());
		if (update != null) {
			update.writeOut(bc, Expression.MODE_VALUE);
			ASMUtil.pop(adapter, update, Expression.MODE_VALUE);
		}
		// ExpressionUtil.visitLine(bc, getStartLine());
		adapter.visitLabel(afterUpdate);

		if (condition != null) condition.writeOut(bc, Expression.MODE_VALUE);
		else bc.getFactory().TRUE().writeOut(bc, Expression.MODE_VALUE);
		adapter.visitJumpInsn(Opcodes.IFNE, afterInit);
		// ExpressionUtil.visitLine(bc, getEndLine());
		adapter.visitLabel(end);

	}

	Expression getInit() {
		return init;
	}

	Expression getCondition() {
		return condition;
	}

	Expression getUpdate() {
		return update;
	}

	/**
	 * Same loop as the generic version, but while the counter holds a plain number (checked once after
	 * the init) it is kept in a Java double, so the loop does not have to look up the variable and
	 * convert its value for the condition and the update. The counter is still written back to its
	 * scope in every iteration, so the body, the condition and the code after the loop see exactly the
	 * values they would see without this optimization, break, continue and labels work the same as in
	 * the generic version. If the counter is not a plain number (or precise math is enabled) the generic
	 * code in the same loop is used. ForCounter makes sure nothing in the loop can change the counter.
	 */
	private void _writeOutCounter(BytecodeContext bc, ForCounter counter) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		Factory f = bc.getFactory();
		Label beforeInit = new Label();
		Label afterInit = new Label();
		Label afterUpdate = new Label();
		Label genericUpdate = new Label();
		Label genericCondition = new Label();
		Label noCounter = new Label();

		int lFast = adapter.newLocal(Type.BOOLEAN_TYPE);
		int lCounter = adapter.newLocal(Type.DOUBLE_TYPE);
		int lBoxed = adapter.newLocal(Types.DOUBLE);
		int lValue = adapter.newLocal(Types.OBJECT);

		bc.visitLine(getStart());
		adapter.visitLabel(beforeInit);
		if (init != null) {
			init.writeOut(bc, Expression.MODE_VALUE);
			adapter.pop();
		}

		// is the counter a plain number?
		adapter.push(false);
		adapter.storeLocal(lFast);
		adapter.push(0d);
		adapter.storeLocal(lCounter);
		adapter.visitInsn(Opcodes.ACONST_NULL);
		adapter.storeLocal(lBoxed);

		counter.createVariable(f).writeOut(bc, Expression.MODE_REF);
		adapter.storeLocal(lValue);
		adapter.loadArg(0);
		adapter.loadLocal(lValue);
		adapter.invokeStatic(Types.OP_UTIL, IS_LOOP_COUNTER);
		adapter.storeLocal(lFast);
		adapter.loadLocal(lFast);
		adapter.ifZCmp(GeneratorAdapter.EQ, noCounter);
		adapter.loadLocal(lValue);
		adapter.checkCast(Types.NUMBER);
		adapter.invokeVirtual(Types.NUMBER, DOUBLE_VALUE);
		adapter.storeLocal(lCounter);
		adapter.visitLabel(noCounter);

		adapter.visitJumpInsn(Opcodes.GOTO, afterUpdate);
		adapter.visitLabel(afterInit);

		body.writeOut(bc);

		// update
		adapter.visitLabel(beforeUpdate);
		adapter.loadLocal(lFast);
		adapter.ifZCmp(GeneratorAdapter.EQ, genericUpdate);
		adapter.loadLocal(lCounter);
		adapter.push(Math.abs(counter.step));
		adapter.visitInsn(counter.step < 0 ? Opcodes.DSUB : Opcodes.DADD);
		adapter.dup2();
		adapter.storeLocal(lCounter);
		adapter.invokeStatic(Types.DOUBLE, VALUE_OF);
		adapter.storeLocal(lBoxed);
		{
			Assign assign = new Assign(counter.createVariable(f), new LocalExpression(f, getStart(), getEnd(), lBoxed, Types.DOUBLE), getEnd());
			ASMUtil.pop(adapter, assign.writeOutAsType(bc, Expression.MODE_VALUE));
		}
		adapter.visitJumpInsn(Opcodes.GOTO, afterUpdate);

		adapter.visitLabel(genericUpdate);
		update.writeOut(bc, Expression.MODE_VALUE);
		ASMUtil.pop(adapter, update, Expression.MODE_VALUE);

		// condition
		adapter.visitLabel(afterUpdate);
		adapter.loadLocal(lFast);
		adapter.ifZCmp(GeneratorAdapter.EQ, genericCondition);
		if (counter.literalBound != null) {
			adapter.loadLocal(lCounter);
			adapter.push(counter.literalBound.doubleValue());
			adapter.invokeStatic(Types.OP_UTIL, COMPARE_DOUBLE);
		}
		else {
			adapter.loadArg(0);
			adapter.loadLocal(lCounter);
			counter.bound.writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.OP_UTIL, COMPARE_DOUBLE_OBJECT);
		}
		int jump;
		if (counter.comparison == Factory.OP_DEC_LT) jump = Opcodes.IFLT;
		else if (counter.comparison == Factory.OP_DEC_LTE) jump = Opcodes.IFLE;
		else if (counter.comparison == Factory.OP_DEC_GT) jump = Opcodes.IFGT;
		else jump = Opcodes.IFGE;
		adapter.visitJumpInsn(jump, afterInit);
		adapter.visitJumpInsn(Opcodes.GOTO, end);

		adapter.visitLabel(genericCondition);
		condition.writeOut(bc, Expression.MODE_VALUE);
		adapter.visitJumpInsn(Opcodes.IFNE, afterInit);
		adapter.visitLabel(end);
	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return beforeUpdate;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "ForStatement");

		// label
		if ( label != null ) {
			sct.setEL(KeyConstants._label, label);
		}

		// init
		if ( this.init != null ) {
			Struct init = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			this.init.dump(init);
			sct.setEL(KeyConstants._init, init);
		}
		// test
		if ( condition != null ) {
			Struct test = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			condition.dump(test);
			sct.setEL(KeyConstants._test, test);
		}
		// alternate
		if ( this.update != null ) {
			Struct update = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			this.update.dump(update);
			sct.setEL(KeyConstants._update, update);
		}
		// body
		{
			Struct body = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			this.body.dump(body);
			sct.setEL(KeyConstants._body, body);
		}
	}
}