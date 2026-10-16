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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.var.VariableRef;
import lucee.transformer.bytecode.expression.var.VariableString;
import lucee.transformer.bytecode.statement.tag.TagTry;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryCatchFinallyVisitor;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.statement.HasBodies;

/**
 * produce try-catch-finally
 */
public final class TryCatchFinally extends StatementBase implements Opcodes, HasBodies, FlowControlRetry {

	// private static LitString ANY=LitString.toExprString("any", -1);

	private static final Method TO_PAGE_EXCEPTION = new Method("toPageException", Types.PAGE_EXCEPTION, new Type[] { Types.THROWABLE });

	// public boolean typeEqual(String type);
	private static final Method TYPE_EQUAL = new Method("typeEqual", Types.BOOLEAN_VALUE, new Type[] { Types.STRING });

	// Struct getCatchBlock(PageContext pc);
	private static final Method GET_CATCH_BLOCK = new Method("getCatchBlock", Types.STRUCT, new Type[] { Types.PAGE_CONTEXT });

	// void isAbort(e)
	public static final Method IS_ABORT = new Method("isAbort", Types.BOOLEAN_VALUE, new Type[] { Types.THROWABLE });

	private final static Method SET = new Method("set", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });

	private static final Method REMOVE_EL = new Method("removeEL", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });

	private Body tryBody;
	private Body finallyBody;
	private List<Catch> catches = new ArrayList<Catch>();
	private Position finallyLine;

	private Label begin = new Label();

	private FlowControlFinal fcf;

	/**
	 * Constructor of the class
	 * 
	 * @param factory
	 * @param body
	 * @param start
	 * @param end
	 */
	public TryCatchFinally(Factory factory, Body body, Position start, Position end) {
		super(factory, start, end);
		this.tryBody = body;
		body.setParent(this);
	}

	/**
	 * sets finally body
	 * 
	 * @param body
	 * @param finallyLine
	 */
	public void setFinally(Body body, Position finallyLine) {
		body.setParent(this);
		this.finallyBody = body;
		this.finallyLine = finallyLine;
	}

	/**
	 * data for a single catch block
	 */
	private class Catch {

		private ExprString type;
		private Body body;
		private VariableRef name;
		private Position line;

		public Catch(ExprString type, VariableRef name, Body body, Position line) {
			this.type = type;
			this.name = name;
			this.body = body;
			this.line = line;
		}

	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.StatementBase#_writeOut(lucee.transformer.bytecode.BytecodeContext)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		final GeneratorAdapter adapter = bc.getAdapter();

		adapter.visitLabel(begin);

		// Reference ref=null;
		final int lRef = adapter.newLocal(Types.REFERENCE);
		adapter.visitInsn(Opcodes.ACONST_NULL);
		adapter.storeLocal(lRef);

		// has no try body, if there is no try body, no catches are executed, only finally
		if (!tryBody.hasStatements()) {
			if (finallyBody != null) finallyBody.writeOut(bc);
			return;
		}

		// PageExceptionImpl old=pc.getCatch();
		final int old = adapter.newLocal(Types.PAGE_EXCEPTION);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.GET_CATCH);
		adapter.storeLocal(old);

		TryCatchFinallyVisitor tcfv = new TryCatchFinallyVisitor(new OnFinally() {

			@Override
			public void _writeOut(BytecodeContext bc) throws TransformerException {

				adapter.loadArg(0);
				adapter.loadLocal(old);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH_PE);

				_writeOutFinally(bc, lRef);
			}
		}, getFlowControlFinal());

		// try
		tcfv.visitTryBegin(bc);
		tryBody.writeOut(bc);
		int lThrow = tcfv.visitTryEndCatchBeging(bc);
		_writeOutCatch(bc, lRef, lThrow, old);
		tcfv.visitCatchEnd(bc);

	}

	private void _writeOutFinally(BytecodeContext bc, int lRef) throws TransformerException {
		// ref.remove(pc);
		// Reference r=null;
		GeneratorAdapter adapter = bc.getAdapter();

		// if(fcf!=null &&
		// fcf.getAfterFinalGOTOLabel()!=null)ASMUtil.visitLabel(adapter,fcf.getFinalEntryLabel());
		bc.visitLine(finallyLine);

		// if (reference != null)
		// reference.removeEL(pagecontext);
		Label removeEnd = new Label();
		adapter.loadLocal(lRef);
		adapter.ifNull(removeEnd);
		adapter.loadLocal(lRef);
		adapter.loadArg(0);
		adapter.invokeInterface(Types.REFERENCE, REMOVE_EL);
		adapter.pop();
		adapter.visitLabel(removeEnd);

		if (finallyBody != null) finallyBody.writeOut(bc); // finally
		/*
		 * if(fcf!=null){ Label l = fcf.getAfterFinalGOTOLabel();
		 * if(l!=null)adapter.visitJumpInsn(Opcodes.GOTO, l); }
		 */
	}

	private void _writeOutCatch(BytecodeContext bc, int lRef, int lThrow, int old) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		int pe = adapter.newLocal(Types.PAGE_EXCEPTION);

		// instance of Abort
		Label abortEnd = new Label();
		adapter.loadLocal(lThrow);
		adapter.invokeStatic(Types.ABORT, TryCatchFinally.IS_ABORT);
		// adapter.instanceOf(Types.ABORT);
		adapter.ifZCmp(Opcodes.IFEQ, abortEnd);
		adapter.loadLocal(lThrow);
		adapter.throwException();
		adapter.visitLabel(abortEnd);

		/*
		 * // PageExceptionImpl old=pc.getCatch(); int old=adapter.newLocal(Types.PAGE_EXCEPTION);
		 * adapter.loadArg(0); adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.GET_CATCH);
		 * adapter.storeLocal(old);
		 */

		// cast to PageException Caster.toPagException(t);
		adapter.loadLocal(lThrow);
		adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);

		// PageException pe=...
		adapter.storeLocal(pe);

		// catch loop
		Label endAllIf = new Label();
		Iterator<Catch> it = catches.iterator();
		Catch ctElse = null;
		while (it.hasNext()) {
			Catch ct = it.next();
			// store any for else
			if (ct.type != null && ct.type instanceof LitString && ((LitString) ct.type).getString().equalsIgnoreCase("any")) {
				ctElse = ct;
				continue;
			}

			bc.visitLine(ct.line);

			// pe.typeEqual(type)
			if (ct.type == null) {
				getFactory().TRUE().writeOut(bc, Expression.MODE_VALUE);
			}
			else {
				adapter.loadLocal(pe);
				ct.type.writeOut(bc, Expression.MODE_REF);
				adapter.invokeVirtual(Types.PAGE_EXCEPTION, TYPE_EQUAL);
			}

			Label endIf = new Label();
			adapter.ifZCmp(Opcodes.IFEQ, endIf);

			catchBody(bc, adapter, ct, pe, lRef, true, true);

			adapter.visitJumpInsn(Opcodes.GOTO, endAllIf);
			adapter.visitLabel(endIf);

		}

		if (ctElse != null) {
			catchBody(bc, adapter, ctElse, pe, lRef, true, true);
		}
		else {
			// pc.setCatch(pe,true);
			adapter.loadArg(0);
			adapter.loadLocal(pe);
			adapter.push(false);
			adapter.push(false);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH3);

			adapter.loadLocal(pe);
			adapter.throwException();
		}
		adapter.visitLabel(endAllIf);

		/*
		 * adapter.loadArg(0); adapter.loadLocal(old); adapter.invokeVirtual(Types.PAGE_CONTEXT,
		 * TagTry.SET_CATCH_PE);
		 */

	}

	private static void catchBody(BytecodeContext bc, GeneratorAdapter adapter, Catch ct, int pe, int lRef, boolean caugth, boolean store) throws TransformerException {
		// pc.setCatch(pe,true);
		adapter.loadArg(0);
		adapter.loadLocal(pe);
		adapter.push(caugth);
		adapter.push(store);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH3);

		// ref=
		ct.name.writeOut(bc, Expression.MODE_REF);
		adapter.storeLocal(lRef);

		adapter.loadLocal(lRef);
		adapter.loadArg(0);
		adapter.loadLocal(pe);// (...,pe.getCatchBlock(pc))
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_EXCEPTION, GET_CATCH_BLOCK);
		adapter.invokeInterface(Types.REFERENCE, SET);
		adapter.pop();

		ct.body.writeOut(bc);
	}

	/**
	 * @param type
	 * @param name
	 * @param body
	 * @param line
	 */
	public void addCatch(ExprString type, VariableRef name, Body body, Position line) {
		body.setParent(this);
		catches.add(new Catch(type, name, body, line));
	}

	/**
	 * @param type
	 * @param name
	 * @param b
	 * @param line
	 * @throws TransformerException
	 */
	public void addCatch(BytecodeContext bc, Expression type, Expression name, Body b, Position line) throws TransformerException {
		// MUSTMUST
		// type
		if (type == null || type instanceof ExprString) {
		}
		else if (type instanceof Variable) {
			type = VariableString.toExprString(type);
		}
		else throw new TransformerException(bc, "type from catch statement is invalid", type.getStart());

		// name
		if (name instanceof LitString) {
			Variable v = getFactory().createVariable(Scope.SCOPE_UNDEFINED, name.getStart(), name.getEnd());
			v.addMember(getFactory().createDataMember(getFactory().toExprString(name)));
			name = new VariableRef(v, true);
		}
		else if (name instanceof Variable) name = new VariableRef((Variable) name, true);
		else throw new TransformerException(bc, "name from catch statement is invalid", name.getStart());

		addCatch((ExprString) type, (VariableRef) name, b, line);
	}

	/**
	 * @see lucee.transformer.statement.HasBodies#getBodies()
	 */
	@Override
	public Body[] getBodies() {

		int len = catches.size(), count = 0;
		if (tryBody != null) len++;
		if (finallyBody != null) len++;
		Body[] bodies = new Body[len];
		Catch c;
		Iterator<Catch> it = catches.iterator();
		while (it.hasNext()) {
			c = it.next();
			bodies[count++] = c.body;
		}
		if (tryBody != null) bodies[count++] = tryBody;
		if (finallyBody != null) bodies[count++] = finallyBody;

		return bodies;
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		if (fcf == null) fcf = new FlowControlFinalImpl();
		return fcf;
	}

	@Override
	public Label getRetryLabel() {
		return begin;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "TryStatement");

		// body
		{
			Struct body = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			tryBody.dump(body);
			sct.setEL(KeyConstants._body, body);
		}
		// handlers
		if (catches != null && catches.size() > 0) {
			Array handlers = new ArrayImpl(8, false);
			for (Catch c: catches) {
				Struct sctCatch = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
				sctCatch.setEL(KeyConstants._type, "CatchClause");

				// param
				Struct sctParam = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
				sctCatch.setEL(KeyConstants._param, sctParam);
				if (c.type instanceof LitString) {
					sctParam.setEL(KeyConstants._type, ((LitString) c.type).getString());
				}
				else if (c.type != null) {
					Struct sctType = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
					c.type.dump(sctType);
					sctParam.setEL(KeyConstants._type, sctType);
				}
				if (c.name != null) {
					Struct sctName = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
					c.name.dump(sctName);
					sctParam.setEL(KeyConstants._name, sctName);
				}
				// body
				if (c.body != null) {
					Struct sctBody = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
					c.body.dump(sctBody);
					sctCatch.setEL(KeyConstants._body, sctBody);
				}

				handlers.appendEL(sctCatch);
				// "typeAnnotation": "template"
				// expr.dump(test);

			}
			sct.setEL(KeyConstants._handlers, handlers);
		}
		// finalizer
		if (finallyBody != null) {
			Struct finalizer = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			finallyBody.dump(finalizer);
			sct.setEL(KeyConstants._finalizer, finalizer);
		}
	}
}