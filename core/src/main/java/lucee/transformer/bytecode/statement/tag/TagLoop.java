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

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.FlowControlBreak;
import lucee.transformer.bytecode.statement.FlowControlContinue;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.statement.ForEach;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Methods_Caster;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.DecisionDoubleVisitor;
import lucee.transformer.bytecode.visitor.DecisionIntVisitor;
import lucee.transformer.bytecode.visitor.DecisionObjectVisitor;
import lucee.transformer.bytecode.visitor.DoWhileVisitor;
import lucee.transformer.bytecode.visitor.ForDoubleVisitor;
import lucee.transformer.bytecode.visitor.ForVisitor;
import lucee.transformer.bytecode.visitor.LoopVisitor;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.bytecode.visitor.WhileVisitor;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.Expression;

public final class TagLoop extends TagGroup implements FlowControlBreak, FlowControlContinue {

	public static final int TYPE_FILE = 1;
	public static final int TYPE_LIST = 2;
	public static final int TYPE_FROM_TO = 3;
	public static final int TYPE_CONDITION = 4;
	public static final int TYPE_QUERY = 5;
	public static final int TYPE_COLLECTION = 6;
	public static final int TYPE_ARRAY = 7;
	public static final int TYPE_GROUP = 8;
	public static final int TYPE_INNER_GROUP = 9;
	public static final int TYPE_INNER_QUERY = 10;
	public static final int TYPE_NOTHING = 11;
	public static final int TYPE_STRUCT = 12;
	public static final int TYPE_TIMES = 13;

	// VariableReference getVariableReference(PageContext pc,String var)
	private static final Method GET_VARIABLE_REFERENCE = new Method("getVariableReference", Types.VARIABLE_REFERENCE, new Type[] { Types.PAGE_CONTEXT, Types.STRING });

	// Object set(PageContext pc, Object value)
	private static final Method SET = new Method("set", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	// Object set(double value)
	private static final Method SET_DOUBLE = new Method("set", Types.VOID, new Type[] { Types.DOUBLE_VALUE });

	/*
	 * private static final Method KEYS = new Method( "keyIterator", Types.COLLECTION_KEY_ARRAY, new
	 * Type[]{});
	 */

	private static final Method GET = new Method("get", Types.OBJECT, new Type[] { Types.INT_VALUE, Types.OBJECT });

	private static final Method NEXT = new Method("next", Types.OBJECT, new Type[] {});

	private static final Method HAS_NEXT = new Method("hasNext", Types.BOOLEAN_VALUE, new Type[] {});

	// File toFileExisting(PageContext pc ,String destination)
	private static final Type RESOURCE_UTIL = Type.getType(ResourceUtil.class);
	private static final Method TO_RESOURCE_EXISTING = new Method("toResourceExisting", Types.RESOURCE, new Type[] { Types.PAGE_CONTEXT, Types.STRING });

	// Config getConfig()
	private static final Method GET_CONFIG = new Method("getConfig", Types.CONFIG_WEB, new Type[] {});

	// SecurityManager getSecurityManager()
	private static final Method GET_SECURITY_MANAGER = new Method("getSecurityManager", Types.SECURITY_MANAGER, new Type[] {});

	// void checkFileLocation(File file)
	private static final Method CHECK_FILE_LOCATION = new Method("checkFileLocation", Types.VOID, new Type[] { Types.RESOURCE });

	// Reader getReader(File file, String charset)
	private static final Type IO_UTIL = Type.getType(IOUtil.class);
	private static final Method GET_BUFFERED_READER = new Method("getBufferedReader", Types.BUFFERED_READER, new Type[] { Types.RESOURCE, Types.STRING });

	// void closeEL(Reader r)
	private static final Method CLOSE_EL = new Method("closeEL", Types.VOID, new Type[] { Types.READER });

	// String readLine()
	private static final Method READ_LINE = new Method("readLine", Types.STRING, new Type[] {});

	// Array listToArrayRemoveEmpty(String list, String delimiter)
	private static final Method LIST_TO_ARRAY_REMOVE_EMPTY_SS = new Method("listToArrayRemoveEmpty", Types.ARRAY, new Type[] { Types.STRING, Types.STRING });

	// Array listToArrayRemoveEmpty(String list, char delimiter)
	private static final Method LIST_TO_ARRAY_REMOVE_EMPTY_SC = new Method("listToArrayRemoveEmpty", Types.ARRAY, new Type[] { Types.STRING, Types.CHAR });

	private static final Method SIZE = new Method("size", Types.INT_VALUE, new Type[] {});

	// Object get(int key) klo
	private static final Method GETE = new Method("getE", Types.OBJECT, new Type[] { Types.INT_VALUE });

	// Query getQuery(String key)
	public static final Method GET_QUERY_OBJ = new Method("getQuery", Types.QUERY, new Type[] { Types.OBJECT });
	public static final Method GET_QUERY_STRING = new Method("getQuery", Types.QUERY, new Type[] { Types.STRING });

	// int getCurrentrow()
	static final Method GET_CURRENTROW_1 = new Method("getCurrentrow", Types.INT_VALUE, new Type[] { Types.INT_VALUE });

	static final Method GO = new Method("go", Types.BOOLEAN_VALUE, new Type[] { Types.INT_VALUE, Types.INT_VALUE });

	static final Method GET_ID = new Method("getId", Types.INT_VALUE, new Type[] {});
	private static final Method READ = new Method("read", Types.STRING, new Type[] { Types.READER, Types.INT_VALUE });
	private static final Method ENTRY_ITERATOR = new Method("entryIterator", Types.ITERATOR, new Type[] {});
	private static final Method GET_KEY = new Method("getKey", Types.OBJECT, new Type[] {});
	private static final Method GET_VALUE = new Method("getValue", Types.OBJECT, new Type[] {});

	private int type;
	private LoopVisitor loopVisitor;
	private String label;

	public TagLoop(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		boolean old;

		switch (type) {
		case TYPE_STRUCT:
		case TYPE_COLLECTION:
			writeOutTypeCollection(bc);
			break;
		case TYPE_CONDITION:
			writeOutTypeCondition(bc);
			break;
		case TYPE_FILE:
			writeOutTypeFile(bc);
			break;
		case TYPE_FROM_TO:
			writeOutTypeFromTo(bc);
			break;
		case TYPE_LIST:
			writeOutTypeListArray(bc, false);
			break;
		case TYPE_ARRAY:
			writeOutTypeListArray(bc, true);
			break;
		case TYPE_QUERY:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeQuery(this, bc);
			bc.changeDoSubFunctions(old);
			// writeOutTypeQuery(bc);
			break;

		case TYPE_GROUP:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeGroup(this, bc);
			bc.changeDoSubFunctions(old);
			// writeOutTypeQuery(bc);
			break;

		case TYPE_INNER_GROUP:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeInnerGroup(this, bc);
			bc.changeDoSubFunctions(old);
			break;

		case TYPE_INNER_QUERY:
			old = bc.changeDoSubFunctions(false);
			TagGroupUtil.writeOutTypeInnerQuery(this, bc);
			bc.changeDoSubFunctions(old);
			break;
		case TYPE_TIMES:
			writeOutTypeTimes(bc);
			break;
		case TYPE_NOTHING:
			GeneratorAdapter a = bc.getAdapter();
			DoWhileVisitor dwv = new DoWhileVisitor();
			setLoopVisitor(dwv);
			dwv.visitBeginBody(a);
			getBody().writeOut(bc);
			dwv.visitEndBodyBeginExpr(a);
			a.push(false);
			dwv.visitEndExpr(a);

			break;

		default:
			throw new TransformerException("invalid type", getStart());
		}
	}

	private void writeOutTypeTimes(BytecodeContext bc) throws TransformerException {
		Factory f = bc.getFactory();
		GeneratorAdapter adapter = bc.getAdapter();

		int times = adapter.newLocal(Types.INT_VALUE);
		ExprInt timesExpr = f.toExprInt(getAttribute("times").getValue());
		ExpressionUtil.writeOutSilent(timesExpr, bc, Expression.MODE_VALUE);
		adapter.storeLocal(times);

		ForVisitor fiv = new ForVisitor();
		fiv.visitBegin(adapter, 1, false);
		getBody().writeOut(bc);
		fiv.visitEnd(bc, times, true, getStart());

	}

	/**
	 * write out collection loop
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeCollection(BytecodeContext bc) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();

		// VariableReference item=VariableInterpreter.getVariableReference(pc,index);
		int index = -1;
		Attribute attrIndex = getAttribute("index");
		if (attrIndex == null) attrIndex = getAttribute("key");
		if (attrIndex != null) {
			index = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrIndex.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(index);
		}

		// VariableReference item=VariableInterpreter.getVariableReference(pc,item);
		int item = -1;
		Attribute attrItem = getAttribute("item");
		if (attrItem == null) attrItem = getAttribute("value");
		if (attrItem != null) {
			item = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrItem.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(item);
		}
		boolean hasIndexAndItem = index != -1 && item != -1;
		boolean hasItem = item != -1;

		WhileVisitor whileVisitor = new WhileVisitor();
		loopVisitor = whileVisitor;
		// java.util.Iterator it=Caster.toIterator(@collection');
		int it = adapter.newLocal(Types.ITERATOR);
		Attribute coll = getAttribute("struct");
		if (coll == null) coll = getAttribute("collection");
		coll.getValue().writeOut(bc, Expression.MODE_REF);

		// item and index
		int entry = -1;
		if (hasIndexAndItem) {
			entry = adapter.newLocal(Types.MAP_ENTRY);
			// Caster.toCollection(collection)
			adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COLLECTION);
			// coll.entryIterator();
			adapter.invokeInterface(Types.COLLECTION, ENTRY_ITERATOR);
		}
		else {
			// if(hasItem) adapter.invokeStatic(ForEach.FOR_EACH_UTIL,ForEach.FOR_EACH);
			// else
			adapter.invokeStatic(ForEach.FOR_EACH_UTIL, ForEach.LOOP_COLLECTION);
		}

		adapter.storeLocal(it);

		// while(it.hasNext()) {
		whileVisitor.visitBeforeExpression(bc);
		adapter.loadLocal(it);
		adapter.invokeInterface(Types.ITERATOR, HAS_NEXT);

		whileVisitor.visitAfterExpressionBeforeBody(bc);
		if (hasIndexAndItem) {
			// entry=it.next();
			adapter.loadLocal(it);
			adapter.invokeInterface(Types.ITERATOR, NEXT);
			adapter.storeLocal(entry);

			// keyRef.set(pc,entry.getKey())
			adapter.loadLocal(index);
			adapter.loadArg(0);
			adapter.loadLocal(entry);
			adapter.invokeInterface(Types.MAP_ENTRY, GET_KEY);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_STRING);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();

			// valueRef.set(pc,entry.getKey())
			adapter.loadLocal(item);
			adapter.loadArg(0);
			adapter.loadLocal(entry);
			adapter.invokeInterface(Types.MAP_ENTRY, GET_VALUE);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();

		}
		else {
			if (index == -1) adapter.loadLocal(item);
			else adapter.loadLocal(index);

			adapter.loadArg(0);
			adapter.loadLocal(it);
			adapter.invokeInterface(Types.ITERATOR, NEXT);

			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();
		}
		getBody().writeOut(bc);
		whileVisitor.visitAfterBody(bc, getEnd());

		// Reset
		adapter.loadLocal(it);
		adapter.invokeStatic(ForEach.FOR_EACH_UTIL, ForEach.RESET);
	}

	/**
	 * write out condition loop
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeCondition(BytecodeContext bc) throws TransformerException {
		WhileVisitor whileVisitor = new WhileVisitor();
		loopVisitor = whileVisitor;
		whileVisitor.visitBeforeExpression(bc);
		bc.getFactory().toExprBoolean(getAttribute("condition").getValue()).writeOut(bc, Expression.MODE_VALUE);
		whileVisitor.visitAfterExpressionBeforeBody(bc);
		getBody().writeOut(bc);
		whileVisitor.visitAfterBody(bc, getEnd());

	}

	/**
	 * write out file loop
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeFile(BytecodeContext bc) throws TransformerException {
		WhileVisitor whileVisitor = new WhileVisitor();
		loopVisitor = whileVisitor;
		GeneratorAdapter adapter = bc.getAdapter();

		// charset=@charset
		int charset = adapter.newLocal(Types.STRING);
		Attribute attrCharset = getAttribute("charset");
		if (attrCharset == null) adapter.visitInsn(Opcodes.ACONST_NULL);
		else attrCharset.getValue().writeOut(bc, Expression.MODE_REF);
		adapter.storeLocal(charset);

		// startline=@startline
		int startline = adapter.newLocal(Types.INT_VALUE);
		Attribute attrStartLine = getAttribute("startline");
		if (attrStartLine == null) attrStartLine = getAttribute("from"); // CF8
		if (attrStartLine == null) adapter.push(1);
		else {
			attrStartLine.getValue().writeOut(bc, Expression.MODE_VALUE);
			adapter.visitInsn(Opcodes.D2I);
		}
		adapter.storeLocal(startline);

		// endline=@endline
		int endline = adapter.newLocal(Types.INT_VALUE);
		Attribute attrEndLine = getAttribute("endline");
		if (attrEndLine == null) attrEndLine = getAttribute("to");
		if (attrEndLine == null) adapter.push(-1);
		else {
			attrEndLine.getValue().writeOut(bc, Expression.MODE_VALUE);
			adapter.visitInsn(Opcodes.D2I);
		}
		adapter.storeLocal(endline);

		// VariableReference index=VariableInterpreter.getVariableReference(pc,@index);
		int index = -1, item = -1;

		// item
		Attribute attrItem = getAttribute("item");
		if (attrItem != null) {
			item = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrItem.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(item);
		}

		// index
		Attribute attrIndex = getAttribute("index");
		if (attrIndex != null) {
			index = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrIndex.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(index);
		}

		// java.io.File file=FileUtil.toResourceExisting(pc,@file);
		int resource = adapter.newLocal(Types.RESOURCE);
		adapter.loadArg(0);
		getAttribute("file").getValue().writeOut(bc, Expression.MODE_REF);
		adapter.invokeStatic(RESOURCE_UTIL, TO_RESOURCE_EXISTING);
		adapter.storeLocal(resource);

		// pc.getConfig().getSecurityManager().checkFileLocation(resource);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_CONFIG);
		adapter.invokeInterface(Types.CONFIG_WEB, GET_SECURITY_MANAGER);
		adapter.loadLocal(resource);
		adapter.invokeInterface(Types.SECURITY_MANAGER, CHECK_FILE_LOCATION);

		// char[] carr=new char[characters];
		Attribute attr = getAttribute("characters");
		int carr = -1;
		if (attr != null) {
			carr = adapter.newLocal(Types.CHAR_ARRAY);
			attr.getValue().writeOut(bc, Expression.MODE_VALUE);
			adapter.cast(Types.DOUBLE_VALUE, Types.INT_VALUE);
			adapter.newArray(Types.CHAR);
			adapter.storeLocal(carr);
		}

		// BufferedReader reader = IOUtil.getBufferedReader(resource,charset);
		final int br = adapter.newLocal(Types.BUFFERED_READER);
		adapter.loadLocal(resource);
		adapter.loadLocal(charset);
		adapter.invokeStatic(IO_UTIL, GET_BUFFERED_READER);
		adapter.storeLocal(br);

		// String line;
		int line = adapter.newLocal(Types.STRING);

		// int count=0;
		int count = adapter.newLocal(Types.INT_VALUE);
		adapter.push(0);
		adapter.storeLocal(count);

		TryFinallyVisitor tfv = new TryFinallyVisitor(new OnFinally() {
			@Override
			public void _writeOut(BytecodeContext bc) {
				bc.getAdapter().loadLocal(br);
				bc.getAdapter().invokeStatic(IO_UTIL, CLOSE_EL);
			}
		}, null);
		// TryFinallyVisitor tcfv=new TryFinallyVisitor();

		// try
		tfv.visitTryBegin(bc);
		// tcfv.visitTryBegin(bc);
		// while((line=br.readLine())!=null) {
		// WhileVisitor wv=new WhileVisitor();
		whileVisitor.visitBeforeExpression(bc);
		DecisionObjectVisitor dv = new DecisionObjectVisitor();
		dv.visitBegin();
		if (attr != null) {
			// IOUtil.read(bufferedreader,12)
			adapter.loadLocal(br);
			adapter.loadLocal(carr);
			adapter.arrayLength();
			adapter.invokeStatic(Types.IOUTIL, READ);
		}
		else {
			// br.readLine()
			adapter.loadLocal(br);
			adapter.invokeVirtual(Types.BUFFERED_READER, READ_LINE);
		}
		adapter.dup();
		adapter.storeLocal(line);

		dv.visitNEQ();
		adapter.visitInsn(Opcodes.ACONST_NULL);
		dv.visitEnd(bc);

		whileVisitor.visitAfterExpressionBeforeBody(bc);
		// if(++count < startLine) continue;
		DecisionIntVisitor dv2 = new DecisionIntVisitor();
		dv2.visitBegin();
		adapter.iinc(count, 1);
		adapter.loadLocal(count);
		dv2.visitLT();
		adapter.loadLocal(startline);
		dv2.visitEnd(bc);
		Label end = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, end);
		whileVisitor.visitContinue(bc);
		adapter.visitLabel(end);

		// if(endLine!=-1 && count > endLine) break;
		DecisionIntVisitor div = new DecisionIntVisitor();
		div.visitBegin();
		adapter.loadLocal(endline);
		div.visitNEQ();
		adapter.push(-1);
		div.visitEnd(bc);
		Label end2 = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, end2);

		DecisionIntVisitor div2 = new DecisionIntVisitor();
		div2.visitBegin();
		adapter.loadLocal(count);
		div2.visitGT();
		adapter.loadLocal(endline);
		div2.visitEnd(bc);
		Label end3 = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, end3);
		whileVisitor.visitBreak(bc);
		adapter.visitLabel(end3);
		adapter.visitLabel(end2);

		// index and item
		if (index != -1 && item != -1) {
			// index.set(pc,line);
			adapter.loadLocal(index);
			adapter.loadArg(0);
			adapter.loadLocal(count);
			adapter.cast(Types.INT_VALUE, Types.DOUBLE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_DOUBLE_VALUE);

			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();

			// item.set(pc,line);
			adapter.loadLocal(item);
			adapter.loadArg(0);
			adapter.loadLocal(line);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();

		}
		// only index
		else if (index != -1) {
			// index.set(pc,line);
			adapter.loadLocal(index);
			adapter.loadArg(0);
			adapter.loadLocal(line);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();

		}
		// only item
		else {
			// item.set(pc,line);
			adapter.loadLocal(item);
			adapter.loadArg(0);
			adapter.loadLocal(line);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();
		}

		getBody().writeOut(bc);

		whileVisitor.visitAfterBody(bc, getEnd());

		tfv.visitTryEnd(bc);

	}

	/**
	 * write out index loop
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeFromTo(BytecodeContext bc) throws TransformerException {
		ForDoubleVisitor forDoubleVisitor = new ForDoubleVisitor();
		loopVisitor = forDoubleVisitor;
		GeneratorAdapter adapter = bc.getAdapter();

		// int from=(int)@from;
		int from = adapter.newLocal(Types.DOUBLE_VALUE);
		ExpressionUtil.writeOutSilent(getAttribute("from").getValue(), bc, Expression.MODE_VALUE);
		adapter.storeLocal(from);

		// int to=(int)@to;
		int to = adapter.newLocal(Types.DOUBLE_VALUE);
		ExpressionUtil.writeOutSilent(getAttribute("to").getValue(), bc, Expression.MODE_VALUE);
		adapter.storeLocal(to);

		// int step=(int)@step;
		int step = adapter.newLocal(Types.DOUBLE_VALUE);
		Attribute attrStep = getAttribute("step");
		if (attrStep != null) {
			ExpressionUtil.writeOutSilent(attrStep.getValue(), bc, Expression.MODE_VALUE);
		}
		else {
			adapter.push(1D);
		}
		adapter.storeLocal(step);

		// boolean dirPlus=(step > 0);
		int dirPlus = adapter.newLocal(Types.BOOLEAN_VALUE);
		DecisionDoubleVisitor div = new DecisionDoubleVisitor();
		div.visitBegin();
		adapter.loadLocal(step);
		div.visitGT();
		adapter.push(0D);
		div.visitEnd(bc);
		adapter.storeLocal(dirPlus);

		// if(step!=0) {
		div = new DecisionDoubleVisitor();
		div.visitBegin();
		adapter.loadLocal(step);
		div.visitNEQ();
		adapter.push(0D);
		div.visitEnd(bc);
		Label ifEnd = new Label();
		adapter.ifZCmp(Opcodes.IFEQ, ifEnd);

		// VariableReference index>=VariableInterpreter.getVariableReference(pc,@index));
		int index = adapter.newLocal(Types.VARIABLE_REFERENCE);
		adapter.loadArg(0);
		Attribute attr = getAttribute("index");
		if (attr == null) attr = getAttribute("item");
		ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
		adapter.storeLocal(index);

		// index.set(from);
		adapter.loadLocal(index);
		adapter.loadLocal(from);
		adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);

		// for

		// int i=forConditionVisitor.visitBeforeExpression(adapter,from,step,true);

		// init
		adapter.visitLabel(forDoubleVisitor.beforeInit);
		forDoubleVisitor.forInit(adapter, from, true);
		adapter.goTo(forDoubleVisitor.beforeExpr);

		// update
		adapter.visitLabel(forDoubleVisitor.beforeUpdate);
		adapter.loadLocal(index);
		// forConditionVisitor.forUpdate(adapter, step, true);
		adapter.visitVarInsn(Opcodes.DLOAD, forDoubleVisitor.i);
		adapter.loadLocal(step);
		adapter.visitInsn(Opcodes.DADD);
		adapter.visitInsn(Opcodes.DUP2);
		adapter.visitVarInsn(Opcodes.DSTORE, forDoubleVisitor.i);

		adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);

		// expression
		adapter.visitLabel(forDoubleVisitor.beforeExpr);
		int i = forDoubleVisitor.i;

		adapter.loadLocal(dirPlus);
		Label l1 = new Label();
		adapter.visitJumpInsn(Opcodes.IFEQ, l1);

		div = new DecisionDoubleVisitor();
		div.visitBegin();
		adapter.visitVarInsn(Opcodes.DLOAD, i);
		div.visitLTE();
		adapter.loadLocal(to);
		div.visitEnd(bc);

		Label l2 = new Label();
		adapter.visitJumpInsn(Opcodes.GOTO, l2);
		adapter.visitLabel(l1);

		div = new DecisionDoubleVisitor();
		div.visitBegin();
		adapter.visitVarInsn(Opcodes.DLOAD, i);
		div.visitGTE();
		adapter.loadLocal(to);
		div.visitEnd(bc);

		adapter.visitLabel(l2);
		forDoubleVisitor.visitAfterExpressionBeginBody(adapter);

		// adapter.loadLocal(index);
		// adapter.visitVarInsn(Opcodes.DLOAD, i);
		// adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);

		getBody().writeOut(bc);

		forDoubleVisitor.visitEndBody(bc, getEnd());

		////// set i after usage
		// adapter.loadLocal(index);
		// adapter.visitVarInsn(Opcodes.DLOAD, i);
		// adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);

		adapter.visitLabel(ifEnd);

	}

	/**
	 * write out list loop
	 * 
	 * @param adapter
	 * @throws TemplateException
	 */
	private void writeOutTypeListArray(BytecodeContext bc, boolean isArray) throws TransformerException {
		ForVisitor forVisitor = new ForVisitor();
		loopVisitor = forVisitor;
		GeneratorAdapter adapter = bc.getAdapter();

		// List.listToArrayRemoveEmpty("", 'c')
		int array = adapter.newLocal(Types.ARRAY);
		int len = adapter.newLocal(Types.INT_VALUE);

		if (isArray) {
			getAttribute("array").getValue().writeOut(bc, Expression.MODE_REF);
		}
		else {
			// array=List.listToArrayRemoveEmpty(list, delimter)
			getAttribute("list").getValue().writeOut(bc, Expression.MODE_REF);
			if (containsAttribute("delimiters")) {
				getAttribute("delimiters").getValue().writeOut(bc, Expression.MODE_REF);
				adapter.invokeStatic(Types.LIST_UTIL, LIST_TO_ARRAY_REMOVE_EMPTY_SS);
			}
			else {
				adapter.visitIntInsn(Opcodes.BIPUSH, 44);// ','
				// adapter.push(',');
				adapter.invokeStatic(Types.LIST_UTIL, LIST_TO_ARRAY_REMOVE_EMPTY_SC);
			}
		}
		adapter.storeLocal(array);

		// int len=array.size();
		adapter.loadLocal(array);
		adapter.invokeInterface(Types.ARRAY, SIZE);
		adapter.storeLocal(len);

		// VariableInterpreter.getVariableReference(pc,Caster.toString(index));
		Attribute attrIndex = getAttribute("index");
		int index = -1;
		if (attrIndex != null) {
			index = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrIndex.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(index);
		}

		// VariableInterpreter.getVariableReference(pc,Caster.toString(item));
		Attribute attrItem = getAttribute("item");
		int item = -1;
		if (attrItem != null) {
			item = adapter.newLocal(Types.VARIABLE_REFERENCE);
			adapter.loadArg(0);
			attrItem.getValue().writeOut(bc, Expression.MODE_REF);
			adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE);
			adapter.storeLocal(item);
		}

		int obj = 0;
		if (isArray) obj = adapter.newLocal(Types.OBJECT);

		// for(int i=1;i<=len;i++) {
		int i = forVisitor.visitBegin(adapter, 1, false);
		// index.set(pc, list.get(i));

		if (isArray) {

			// value
			adapter.loadLocal(array);
			adapter.visitVarInsn(Opcodes.ILOAD, i);
			ASMConstants.NULL(adapter);
			adapter.invokeInterface(Types.ARRAY, GET);
			adapter.dup();
			adapter.storeLocal(obj);
			Label endIf = new Label();
			// adapter.loadLocal(obj);
			adapter.visitJumpInsn(Opcodes.IFNONNULL, endIf);
			adapter.goTo(forVisitor.getContinueLabel());
			adapter.visitLabel(endIf);

			if (item == -1) adapter.loadLocal(index);
			else adapter.loadLocal(item);

			adapter.loadArg(0);

			adapter.loadLocal(obj);

		}
		else {
			if (item == -1) adapter.loadLocal(index);
			else adapter.loadLocal(item);
			adapter.loadArg(0);
			adapter.loadLocal(array);
			adapter.visitVarInsn(Opcodes.ILOAD, i);
			adapter.invokeInterface(Types.ARRAY, GETE);

		}
		adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
		adapter.pop();

		// key
		if (index != -1 && item != -1) {
			adapter.loadLocal(index);
			adapter.loadArg(0);
			adapter.visitVarInsn(Opcodes.ILOAD, i);
			adapter.cast(Types.INT_VALUE, Types.DOUBLE_VALUE);
			adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DOUBLE[Methods_Caster.DOUBLE]);
			adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET);
			adapter.pop();
		}

		getBody().writeOut(bc);
		forVisitor.visitEnd(bc, len, true, getStart());
	}

	/**
	 * @see lucee.transformer.bytecode.statement.FlowControl#getBreakLabel()
	 */
	@Override
	public Label getBreakLabel() {
		return loopVisitor.getBreakLabel();
	}

	/**
	 * @see lucee.transformer.bytecode.statement.FlowControl#getContinueLabel()
	 */
	@Override
	public Label getContinueLabel() {
		return loopVisitor.getContinueLabel();
	}

	@Override
	public short getType() {
		return TAG_LOOP;
	}

	public void setLoopVisitor(LoopVisitor loopVisitor) {
		this.loopVisitor = loopVisitor;
	}

	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}
}