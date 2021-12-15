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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.scope.Undefined;
import lucee.runtime.util.NumberIterator;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.DecisionIntVisitor;
import lucee.transformer.bytecode.visitor.NotVisitor;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.ParseBodyVisitor;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.bytecode.visitor.WhileVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;

public class TagGroupUtil {

	// Undefined us()
	public static final Type UNDEFINED = Type.getType(Undefined.class);
	public static final Method US = new Method("us", UNDEFINED, new Type[] {});

	// void addQuery(Query coll)
	public static final Method ADD_QUERY = new Method("addQuery", Types.VOID, new Type[] { Types.QUERY });

	// void removeQuery()
	public static final Method REMOVE_QUERY = new Method("removeQuery", Types.VOID, new Type[] {});

	// int getRecordcount()
	public static final Method GET_RECORDCOUNT = new Method("getRecordcount", Types.INT_VALUE, new Type[] {});

	// double range(double number, double from)
	public static final Method RANGE = new Method("range", Types.INT_VALUE, new Type[] { Types.INT_VALUE, Types.INT_VALUE });

	public static final Type NUMBER_ITERATOR = Type.getType(NumberIterator.class);

	// NumberIterator load(double from, double to, double max)
	public static final Method LOAD_MAX = new Method("loadMax", NUMBER_ITERATOR, new Type[] { Types.INT_VALUE, Types.INT_VALUE, Types.INT_VALUE });

	public static final Method LOAD_END = new Method("loadEnd", NUMBER_ITERATOR, new Type[] { Types.INT_VALUE, Types.INT_VALUE, Types.INT_VALUE });

	// NumberIterator load(double from, double to, double max)
	public static final Method LOAD_2 = new Method("load", NUMBER_ITERATOR, new Type[] { Types.INT_VALUE, Types.INT_VALUE });

	// NumberIterator load(NumberIterator ni, Query query, String groupName, boolean caseSensitive)
	public static final Method LOAD_5 = new Method("load", NUMBER_ITERATOR, new Type[] { Types.PAGE_CONTEXT, NUMBER_ITERATOR, Types.QUERY, Types.STRING, Types.BOOLEAN_VALUE });

	// boolean isValid()
	/*
	 * public static final Method IS_VALID_0 = new Method( "isValid", Types.BOOLEAN_VALUE, new
	 * Type[]{});
	 */

	public static final Method IS_VALID_1 = new Method("isValid", Types.BOOLEAN_VALUE, new Type[] { Types.INT_VALUE });

	// int current()
	public static final Method CURRENT = new Method("current", Types.INT_VALUE, new Type[] {});

	// void release(NumberIterator ni)
	public static final Method REALEASE = new Method("release", Types.VOID, new Type[] { NUMBER_ITERATOR });

	// void setCurrent(int current)
	public static final Method SET_CURRENT = new Method("setCurrent", Types.VOID, new Type[] { Types.INT_VALUE });

	// void reset()
	public static final Method RESET = new Method("reset", Types.VOID, new Type[] { Types.INT_VALUE });

	// int first()
	public static final Method FIRST = new Method("first", Types.INT_VALUE, new Type[] {});
	public static final Method GET_ID = new Method("getId", Types.INT_VALUE, new Type[] {});

	public static void writeOutTypeQuery(final TagGroup tag, BytecodeContext bc) throws TransformerException {
		final GeneratorAdapter adapter = bc.getAdapter();

		tag.setNumberIterator(adapter.newLocal(NUMBER_ITERATOR));
		boolean isOutput = tag.getType() == TagGroup.TAG_OUTPUT;
		ParseBodyVisitor pbv = isOutput ? new ParseBodyVisitor() : null;
		if (isOutput) pbv.visitBegin(bc);

		// Query query=pc.getQuery(@query);
		tag.setQuery(adapter.newLocal(Types.QUERY));
		adapter.loadArg(0);
		Expression val = tag.getAttribute("query").getValue();
		val.writeOut(bc, Expression.MODE_REF);
		if (val instanceof LitString) adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_QUERY_STRING);
		else adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_QUERY_OBJ);

		adapter.storeLocal(tag.getQuery());

		tag.setPID(adapter.newLocal(Types.INT_VALUE));
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID);
		adapter.storeLocal(tag.getPID());

		// int startAt=query.getCurrentrow();
		final int startAt = adapter.newLocal(Types.INT_VALUE);
		adapter.loadLocal(tag.getQuery());

		adapter.loadLocal(tag.getPID());
		// adapter.loadArg(0);
		// adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID);
		adapter.invokeInterface(Types.QUERY, TagLoop.GET_CURRENTROW_1);

		adapter.storeLocal(startAt);

		// if(query.getRecordcount()>0) {
		DecisionIntVisitor div = new DecisionIntVisitor();
		div.visitBegin();
		adapter.loadLocal(tag.getQuery());
		adapter.invokeInterface(Types.QUERY, GET_RECORDCOUNT);
		div.visitGT();
		adapter.push(0);
		div.visitEnd(bc);
		Label ifRecCount = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, ifRecCount);

		// startrow
		int from = adapter.newLocal(Types.INT_VALUE);
		Attribute attrStartRow = tag.getAttribute("startrow");
		if (attrStartRow != null) {
			// NumberRange.range(@startrow,1)
			// attrStartRow.getValue().writeOut(bc, Expression.MODE_VALUE);
			bc.getFactory().toExprInt(attrStartRow.getValue()).writeOut(bc, Expression.MODE_VALUE);
			// adapter.visitInsn(Opcodes.D2I);
			adapter.push(1);
			adapter.invokeStatic(Types.NUMBER_RANGE, RANGE);
			// adapter.visitInsn(Opcodes.D2I);
		}
		else {
			adapter.push(1);
		}
		adapter.storeLocal(from);

		// numberIterator

		adapter.loadLocal(from);
		adapter.loadLocal(tag.getQuery());
		adapter.invokeInterface(Types.QUERY, GET_RECORDCOUNT);
		// adapter.visitInsn(Opcodes.I2D);

		Attribute attrMaxRow = tag.getAttribute("maxrows");
		Attribute attrEndRow = tag.getAttribute("endrow");
		if (attrMaxRow != null) {
			bc.getFactory().toExprInt(attrMaxRow.getValue()).writeOut(bc, Expression.MODE_VALUE);
			adapter.invokeStatic(NUMBER_ITERATOR, LOAD_MAX);
		}
		else if (attrEndRow != null) {
			bc.getFactory().toExprInt(attrEndRow.getValue()).writeOut(bc, Expression.MODE_VALUE);
			adapter.invokeStatic(NUMBER_ITERATOR, LOAD_END);
		}
		else {
			adapter.invokeStatic(NUMBER_ITERATOR, LOAD_2);
		}
		adapter.storeLocal(tag.getNumberIterator());

		// Group
		Attribute attrGroup = tag.getAttribute("group");
		Attribute attrGroupCS = tag.getAttribute("groupcasesensitive");
		tag.setGroup(adapter.newLocal(Types.STRING));
		final int groupCaseSensitive = adapter.newLocal(Types.BOOLEAN_VALUE);
		if (attrGroup != null) {
			attrGroup.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.storeLocal(tag.getGroup());

			if (attrGroupCS != null) attrGroupCS.getValue().writeOut(bc, Expression.MODE_VALUE);
			else adapter.push(false);
			adapter.storeLocal(groupCaseSensitive);
		}

		// pc.us().addQuery(query);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, US);
		adapter.loadLocal(tag.getQuery());
		adapter.invokeInterface(UNDEFINED, ADD_QUERY);

		// current
		final int current = adapter.newLocal(Types.INT_VALUE);
		adapter.loadLocal(from);
		adapter.push(1);
		adapter.visitInsn(Opcodes.ISUB);
		adapter.storeLocal(current);

		// Try
		TryFinallyVisitor tfv = new TryFinallyVisitor(new OnFinally() {
			@Override
			public void _writeOut(BytecodeContext bc) {
				// query.reset();

				// query.go(startAt);
				adapter.loadLocal(tag.getQuery());
				adapter.loadLocal(startAt);

				adapter.loadLocal(tag.getPID());
				// adapter.loadArg(0);
				// adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID);
				adapter.invokeInterface(Types.QUERY, TagLoop.GO);
				adapter.pop();

				// pc.us().removeQuery();
				adapter.loadArg(0);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, US);
				adapter.invokeInterface(UNDEFINED, REMOVE_QUERY);

				// NumberIterator.release(ni);
				adapter.loadLocal(tag.getNumberIterator());
				adapter.invokeStatic(NUMBER_ITERATOR, REALEASE);
			}
		}, null);
		tfv.visitTryBegin(bc);
		WhileVisitor wv = new WhileVisitor();
		if (tag instanceof TagLoop) ((TagLoop) tag).setLoopVisitor(wv);
		wv.visitBeforeExpression(bc);

		// while(ni.isValid()) {
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(current);
		adapter.push(1);
		adapter.visitInsn(Opcodes.IADD);
		adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1);

		wv.visitAfterExpressionBeforeBody(bc);

		// if(!query.go(ni.current()))break;
		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);

		adapter.loadLocal(tag.getPID());
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);

		NotVisitor.visitNot(bc);
		Label _if = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, _if);
		wv.visitBreak(bc);
		adapter.visitLabel(_if);

		if (attrGroup != null) {
			// NumberIterator oldNi=numberIterator;
			int oldNi = adapter.newLocal(NUMBER_ITERATOR);
			adapter.loadLocal(tag.getNumberIterator());
			adapter.storeLocal(oldNi);

			// numberIterator=NumberIterator.load(ni,query,group,grp_case);
			adapter.loadArg(0);
			adapter.loadLocal(tag.getNumberIterator());
			adapter.loadLocal(tag.getQuery());
			adapter.loadLocal(tag.getGroup());
			adapter.loadLocal(groupCaseSensitive);
			adapter.invokeStatic(NUMBER_ITERATOR, LOAD_5);
			adapter.storeLocal(tag.getNumberIterator());

			// current=oldNi.current();
			adapter.loadLocal(oldNi);
			adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
			adapter.storeLocal(current);

			tag.getBody().writeOut(bc);

			// tmp(adapter,current);

			// NumberIterator.release(ni);
			adapter.loadLocal(tag.getNumberIterator());
			adapter.invokeStatic(NUMBER_ITERATOR, REALEASE);

			// numberIterator=oldNi;
			adapter.loadLocal(oldNi);
			adapter.storeLocal(tag.getNumberIterator());
		}
		else {
			// current=ni.current();
			adapter.loadLocal(tag.getNumberIterator());
			adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
			adapter.storeLocal(current);

			tag.getBody().writeOut(bc);
		}

		// ni.setCurrent(current+1);
		/*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(current); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */

		wv.visitAfterBody(bc, tag.getEnd());

		tfv.visitTryEnd(bc);

		adapter.visitLabel(ifRecCount);

		if (isOutput) pbv.visitEnd(bc);
	}

	public static void writeOutTypeGroup(TagGroup tag, BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		boolean isOutput = tag.getType() == TagGroup.TAG_OUTPUT;
		ParseBodyVisitor pbv = isOutput ? new ParseBodyVisitor() : null;
		if (isOutput) pbv.visitBegin(bc);

		// Group
		Attribute attrGroup = tag.getAttribute("group");
		tag.setGroup(adapter.newLocal(Types.STRING));
		attrGroup.getValue().writeOut(bc, Expression.MODE_REF);
		adapter.storeLocal(tag.getGroup());

		// Group Case Sensitve
		Attribute attrGroupCS = tag.getAttribute("groupcasesensitive");
		int groupCaseSensitive = adapter.newLocal(Types.BOOLEAN_VALUE);
		if (attrGroupCS != null) attrGroupCS.getValue().writeOut(bc, Expression.MODE_VALUE);
		else adapter.push(true);
		adapter.storeLocal(groupCaseSensitive);

		TagGroup parent = getParentTagGroupQuery(tag, tag.getType());
		tag.setNumberIterator(parent.getNumberIterator());
		tag.setQuery(parent.getQuery());
		// queryImpl = parent.getQueryImpl();

		// current
		int current = adapter.newLocal(Types.INT_VALUE);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(current);

		// current
		int icurrent = adapter.newLocal(Types.INT_VALUE);

		adapter.loadLocal(current);
		adapter.push(1);
		adapter.visitInsn(Opcodes.ISUB);
		adapter.storeLocal(icurrent);

		WhileVisitor wv = new WhileVisitor();
		if (tag instanceof TagLoop) ((TagLoop) tag).setLoopVisitor(wv);
		wv.visitBeforeExpression(bc);

		// while(ni.isValid()) {
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(icurrent);
		adapter.push(1);
		adapter.visitInsn(Opcodes.IADD);
		adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1);

		wv.visitAfterExpressionBeforeBody(bc);

		// if(!query.go(ni.current()))break;
		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);

		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID);
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);

		NotVisitor.visitNot(bc);
		Label _if = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, _if);
		wv.visitBreak(bc);
		adapter.visitLabel(_if);

		// NumberIterator oldNi=numberIterator;
		int oldNi = adapter.newLocal(NUMBER_ITERATOR);

		adapter.loadLocal(tag.getNumberIterator());
		adapter.storeLocal(oldNi);

		// numberIterator=NumberIterator.load(ni,query,group,grp_case);
		adapter.loadArg(0);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(tag.getGroup());
		adapter.loadLocal(groupCaseSensitive);
		adapter.invokeStatic(NUMBER_ITERATOR, LOAD_5);
		adapter.storeLocal(tag.getNumberIterator());

		// current=oldNi.current();
		adapter.loadLocal(oldNi);
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(icurrent);

		tag.getBody().writeOut(bc);

		// tmp(adapter,current);

		// NumberIterator.release(ni);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeStatic(NUMBER_ITERATOR, REALEASE);

		// numberIterator=oldNi;
		adapter.loadLocal(oldNi);
		adapter.storeLocal(tag.getNumberIterator());

		// ni.setCurrent(current+1);
		/*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */
		wv.visitAfterBody(bc, tag.getEnd());

		// query.go(ni.current(),pc.getId())
		resetCurrentrow(adapter, tag, current);

		// ni.first();
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, FIRST);
		adapter.pop();

		if (isOutput) pbv.visitEnd(bc);
	}

	public static void writeOutTypeInnerGroup(TagGroup tag, BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		TagGroup parent = getParentTagGroupQuery(tag, tag.getType());
		tag.setNumberIterator(parent.getNumberIterator());
		tag.setQuery(parent.getQuery());
		// queryImpl = parent.getQueryImpl();

		int current = adapter.newLocal(Types.INT_VALUE);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(current);

		// inner current
		int icurrent = adapter.newLocal(Types.INT_VALUE);

		adapter.loadLocal(current);
		adapter.push(1);
		adapter.visitInsn(Opcodes.ISUB);
		adapter.storeLocal(icurrent);

		WhileVisitor wv = new WhileVisitor();
		if (tag instanceof TagLoop) ((TagLoop) tag).setLoopVisitor(wv);
		wv.visitBeforeExpression(bc);

		// while(ni.isValid()) {
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(icurrent);
		adapter.push(1);
		adapter.visitInsn(Opcodes.IADD);
		adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1);

		wv.visitAfterExpressionBeforeBody(bc);

		// if(!query.go(ni.current()))break;

		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);

		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID);
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);

		/*
		 * OLD adapter.invokeInterface(Types.QUERY, TagLoop.GO_1);
		 */
		NotVisitor.visitNot(bc);
		Label _if = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, _if);
		wv.visitBreak(bc);
		adapter.visitLabel(_if);

		// current=ni.current();
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(icurrent);

		tag.getBody().writeOut(bc);

		// ni.setCurrent(current+1);
		/*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */

		wv.visitAfterBody(bc, tag.getEnd());

		resetCurrentrow(adapter, tag, current);

		// ni.first();
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, FIRST);
		adapter.pop();
	}

	public static void writeOutTypeInnerQuery(TagGroup tag, BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		// if(tr ue)return ;
		TagGroup parent = getParentTagGroupQuery(tag, tag.getType());
		tag.setNumberIterator(parent.getNumberIterator());
		tag.setQuery(parent.getQuery());
		tag.setPID(parent.getPID());
		// queryImpl = parent.getQueryImpl();

		// int currentOuter=ni.current();
		int current = adapter.newLocal(Types.INT_VALUE);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(current);

		// current
		int icurrent = adapter.newLocal(Types.INT_VALUE);

		adapter.loadLocal(current);
		adapter.push(1);
		adapter.visitInsn(Opcodes.ISUB);
		adapter.storeLocal(icurrent);

		WhileVisitor wv = new WhileVisitor();
		if (tag instanceof TagLoop) ((TagLoop) tag).setLoopVisitor(wv);
		wv.visitBeforeExpression(bc);

		// while(ni.isValid()) {
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(icurrent);
		adapter.push(1);
		adapter.visitInsn(Opcodes.IADD);
		adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1);

		wv.visitAfterExpressionBeforeBody(bc);

		// if(!query.go(ni.current()))break;
		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);

		adapter.loadLocal(tag.getPID());
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);

		NotVisitor.visitNot(bc);
		Label _if = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, _if);
		wv.visitBreak(bc);
		adapter.visitLabel(_if);

		// current=ni.current();
		adapter.loadLocal(tag.getNumberIterator());
		adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT);
		adapter.storeLocal(icurrent);

		tag.getBody().writeOut(bc);

		// ni.setCurrent(current+1);
		/*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */

		wv.visitAfterBody(bc, tag.getEnd());

		// ni.setCurrent(currentOuter);
		adapter.loadLocal(tag.getNumberIterator());
		adapter.loadLocal(current);
		adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);

		adapter.loadLocal(tag.getQuery());
		adapter.loadLocal(current);

		adapter.loadLocal(tag.getPID());
		// adapter.loadArg(0);
		// adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID);
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);
		adapter.pop();
		// adapter.pop();
	}

	public static TagGroup getParentTagGroupQuery(Statement st, short type) throws TransformerException {
		Statement parent = st.getParent();
		if (parent == null) throw new TransformerException("there is no parent output with query", null);
		else if (parent instanceof TagGroup && type == ((TagGroup) parent).getType()) {
			if (((TagGroup) parent).hasQuery()) return ((TagGroup) parent);
		}
		return getParentTagGroupQuery(parent, type);
	}

	private static void resetCurrentrow(GeneratorAdapter adapter, TagGroup tg, int current) {
		// query.go(ni.current(),pc.getId())
		adapter.loadLocal(tg.getQuery());
		adapter.loadLocal(current);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID);
		adapter.invokeInterface(Types.QUERY, TagLoop.GO);

		/*
		 * OLD adapter.invokeInterface(Types.QUERY, TagLoop.GO_1);
		 */
		adapter.pop();

	}

}