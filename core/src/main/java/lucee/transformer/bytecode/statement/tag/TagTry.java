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
package lucee.transformer.bytecode.statement.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.cast.CastString;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.statement.FlowControlFinalImpl;
import lucee.transformer.bytecode.statement.FlowControlRetry;
import lucee.transformer.bytecode.statement.TryCatchFinally;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryCatchFinallyVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;

public final class TagTry extends TagBase implements FlowControlRetry {

	// private static final ExprString ANY=LitString.toExprString("any");

	private static final Method GET_VARIABLE = new Method("getVariable", Types.OBJECT, new Type[] { Types.STRING });

	private static final Method TO_PAGE_EXCEPTION = new Method("toPageException", Types.PAGE_EXCEPTION, new Type[] { Types.THROWABLE });

	public static final Method SET_CATCH_PE = new Method("setCatch", Types.VOID, new Type[] { Types.PAGE_EXCEPTION });
	public static final Method SET_CATCH3 = new Method("setCatch", Types.VOID, new Type[] { Types.PAGE_EXCEPTION, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE });
	public static final Method SET_CATCH4 = new Method("setCatch", Types.VOID, new Type[] { Types.PAGE_EXCEPTION, Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE });
	// public static final Method SET_CATCH3x = new Method("setCatch", Types.VOID, new Type[] {
	// Types.PAGE_EXCEPTION, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE });
	public static final Method GET_CATCH = new Method("getCatch", Types.PAGE_EXCEPTION, new Type[] {});

	// public boolean typeEqual(String type);
	private static final Method TYPE_EQUAL = new Method("typeEqual", Types.BOOLEAN_VALUE, new Type[] { Types.STRING });

	private FlowControlFinal fcf;

	private boolean checked;
	private Label begin = new Label();

	public TagTry(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		final GeneratorAdapter adapter = bc.getAdapter();
		adapter.visitLabel(begin);
		Body tryBody = new BodyBase(getFactory());
		List<Tag> catches = new ArrayList<Tag>();
		Tag tmpFinal = null;

		tryBody.setParent(getBody().getParent());

		List<Statement> statements = getBody().getStatements();
		Statement stat;
		Tag tag;
		{
			Iterator<Statement> it = statements.iterator();
			while (it.hasNext()) {
				stat = it.next();
				if (stat instanceof Tag) {
					tag = (Tag) stat;
					if (tag.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Catch")) {
						catches.add(tag);
						continue;
					}
					else if (tag.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Finally")) {
						tmpFinal = tag;
						continue;
					}
				}
				tryBody.addStatement(stat);
			}
		}
		final Tag _finally = tmpFinal;

		// has no try body, if there is no try body, no catches are executed, only finally
		if (!tryBody.hasStatements()) {

			if (_finally != null && _finally.getBody() != null) {
				BodyBase.writeOut(bc, _finally.getBody());
				// ExpressionUtil.writeOut(_finally.getBody(), bc);
			}
			return;
		}

		final int old = adapter.newLocal(Types.PAGE_EXCEPTION);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_CATCH);
		adapter.storeLocal(old);

		TryCatchFinallyVisitor tcfv = new TryCatchFinallyVisitor(new OnFinally() {

			@Override
			public void _writeOut(BytecodeContext bc) throws TransformerException {

				adapter.loadArg(0);
				adapter.loadLocal(old);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_CATCH_PE);

				if (_finally != null) {

					ExpressionUtil.visitLine(bc, _finally.getStart());
					BodyBase.writeOut(bc, _finally.getBody());
					// ExpressionUtil.writeOut(_finally.getBody(), bc);
				}
			}
		}, getFlowControlFinal());

		// Try
		tcfv.visitTryBegin(bc);
		BodyBase.writeOut(bc, tryBody);
		// ExpressionUtil.writeOut(tryBody, bc);
		int e = tcfv.visitTryEndCatchBeging(bc);
		// if(e instanceof lucee.runtime.exp.Abort) throw e;
		Label abortEnd = new Label();
		adapter.loadLocal(e);
		// Abort.isAbort(t);
		adapter.invokeStatic(Types.ABORT, TryCatchFinally.IS_ABORT);
		// adapter.instanceOf(Types.ABORT);

		adapter.ifZCmp(Opcodes.IFEQ, abortEnd);
		adapter.loadLocal(e);
		adapter.throwException();
		adapter.visitLabel(abortEnd);

		// PageExceptionImpl old=pc.getCatch();

		// PageException pe=Caster.toPageEception(e);
		int pe = adapter.newLocal(Types.PAGE_EXCEPTION);
		adapter.loadLocal(e);
		adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);
		adapter.storeLocal(pe);

		Iterator<Tag> it = catches.iterator();
		Attribute attrType;
		Expression type;
		Label endAllIfs = new Label();
		Tag tagElse = null;
		while (it.hasNext()) {
			tag = it.next();
			Label endIf = new Label();
			attrType = tag.getAttribute("type");
			type = bc.getFactory().createLitString("any");
			if (attrType != null) type = attrType.getValue();

			if (type instanceof LitString && ((LitString) type).getString().equalsIgnoreCase("any")) {
				tagElse = tag;
				continue;
			}

			ExpressionUtil.visitLine(bc, tag.getStart());

			// if(pe.typeEqual(@type)
			adapter.loadLocal(pe);
			type.writeOut(bc, Expression.MODE_REF);
			adapter.invokeVirtual(Types.PAGE_EXCEPTION, TYPE_EQUAL);

			adapter.ifZCmp(Opcodes.IFEQ, endIf);
			catchBody(bc, adapter, tag, pe, true, true);

			adapter.visitJumpInsn(Opcodes.GOTO, endAllIfs);

			adapter.visitLabel(endIf);

		}
		// else
		if (tagElse != null) {
			catchBody(bc, adapter, tagElse, pe, true, true);
		}
		else {
			// pc.setCatch(pe,false,true);
			adapter.loadArg(0);
			adapter.loadLocal(pe);
			adapter.push(false);
			adapter.push(true);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_CATCH3);

			// throw pe;
			adapter.loadLocal(pe);
			adapter.throwException();
		}
		adapter.visitLabel(endAllIfs);

		// PageExceptionImpl old=pc.getCatch();

		tcfv.visitCatchEnd(bc);
	}

	private static void catchBody(BytecodeContext bc, GeneratorAdapter adapter, Tag tag, int pe, boolean caugth, boolean store) throws TransformerException {
		Expression name = getName(tag);

		// pc.setCatch(pe,true);
		adapter.loadArg(0);
		if (name != null) adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
		adapter.loadLocal(pe);
		if (name != null) CastString.toExprString(name).writeOut(bc, Expression.MODE_REF);
		adapter.push(caugth);
		adapter.push(store);
		adapter.invokeVirtual(name != null ? Types.PAGE_CONTEXT_IMPL : Types.PAGE_CONTEXT, name != null ? SET_CATCH4 : SET_CATCH3);
		BodyBase.writeOut(bc, tag.getBody());
		// ExpressionUtil.writeOut(tag.getBody(), bc);

	}

	public static Expression getName(Tag tag) {
		Attribute attr = tag == null ? null : tag.getAttribute("name");
		if (attr != null) return attr.getValue();
		return null;
	}

	private boolean hasFinally() {
		List<Statement> statements = getBody().getStatements();
		Statement stat;
		Tag tag;
		Iterator<Statement> it = statements.iterator();
		while (it.hasNext()) {
			stat = it.next();
			if (stat instanceof Tag) {
				tag = (Tag) stat;
				if (tag.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Finally")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		if (!checked) {
			checked = true;
			if (!hasFinally()) return null;
			fcf = new FlowControlFinalImpl();
		}

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

}