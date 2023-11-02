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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.osgi.framework.BundleException;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.Abort;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.tag.MissingAttribute;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.cast.CastOther;
import lucee.transformer.bytecode.expression.type.LiteralStringArray;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryCatchFinallyVisitor;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;

public final class TagHelper {
	private static final Type MISSING_ATTRIBUTE = Type.getType(MissingAttribute.class);
	private static final Type MISSING_ATTRIBUTE_ARRAY = Type.getType(MissingAttribute[].class);
	private static final Type BODY_TAG = Type.getType(BodyTag.class);
	private static final Type TAG = Type.getType(javax.servlet.jsp.tagext.Tag.class);
	private static final Type TRY_CATCH_FINALLY_TAG = Type.getType(javax.servlet.jsp.tagext.TryCatchFinally.class);
	private static final Type TAG_UTIL = Type.getType(lucee.runtime.tag.TagUtil.class);

	// TagUtil.setAttributeCollection(Tag, Struct)
	private static final Method SET_ATTRIBUTE_COLLECTION = new Method("setAttributeCollection", Types.VOID,
			new Type[] { Types.PAGE_CONTEXT, TAG, MISSING_ATTRIBUTE_ARRAY, Types.STRUCT, Types.INT_VALUE });

	// Tag use(String)
	private static final Method USE4 = new Method("use", TAG, new Type[] { Types.STRING, Types.STRING, Types.INT_VALUE, Types.STRING });
	private static final Method USE6 = new Method("use", TAG, new Type[] { Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.INT_VALUE, Types.STRING });

	// void setAppendix(String appendix)
	private static final Method SET_APPENDIX1 = new Method("setAppendix", Type.VOID_TYPE, new Type[] { Types.STRING });

	// void setAppendix(String appendix)
	private static final Method SET_APPENDIX2 = new Method("setAppendix", Type.VOID_TYPE, new Type[] { Types.TAG, Types.STRING });

	// void setDynamicAttribute(String uri, String name, Object value)
	private static final Method SET_DYNAMIC_ATTRIBUTE = new Method("setDynamicAttribute", Type.VOID_TYPE, new Type[] { Types.STRING, Types.COLLECTION_KEY, Types.OBJECT });
	// public static void setAttribute(PageContext pc,boolean doDynamic,boolean silently,Tag tag, String
	// name,Object value) throws PageException {

	private static final Method SET_ATTRIBUTE4 = new Method("setAttribute", Type.VOID_TYPE, new Type[] { Types.PAGE_CONTEXT, TAG, Types.STRING, Types.OBJECT });

	private static final Method SET_META_DATA2 = new Method("setMetaData", Type.VOID_TYPE, new Type[] { Types.STRING, Types.OBJECT });

	private static final Method SET_META_DATA3 = new Method("setMetaData", Type.VOID_TYPE, new Type[] { Types.TAG, Types.STRING, Types.OBJECT });

	// void hasBody(boolean hasBody)
	private static final Method HAS_BODY1 = new Method("hasBody", Type.VOID_TYPE, new Type[] { Types.BOOLEAN_VALUE });

	// void hasBody(boolean hasBody)
	private static final Method HAS_BODY2 = new Method("hasBody", Type.VOID_TYPE, new Type[] { Types.TAG, Types.BOOLEAN_VALUE });

	// int doStartTag()
	private static final Method DO_START_TAG = new Method("doStartTag", Types.INT_VALUE, new Type[] {});

	// int doEndTag()
	private static final Method DO_END_TAG = new Method("doEndTag", Types.INT_VALUE, new Type[] {});

	private static final Type ABORT = Type.getType(Abort.class);
	// private static final Type EXPRESSION_EXCEPTION = Type.getType(ExpressionException.class);

	// ExpressionException newInstance(int)
	private static final Method NEW_INSTANCE = new Method("newInstance", ABORT, new Type[] { Types.INT_VALUE });
	private static final Method NEW_INSTANCE_MAX2 = new Method("newInstance", MISSING_ATTRIBUTE, new Type[] { Types.COLLECTION_KEY, Types.STRING });

	private static final Method NEW_INSTANCE_MAX3 = new Method("newInstance", MISSING_ATTRIBUTE, new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.STRING_ARRAY });

	// void initBody(BodyTag bodyTag, int state)
	private static final Method INIT_BODY = new Method("initBody", Types.VOID, new Type[] { BODY_TAG, Types.INT_VALUE });

	// int doAfterBody()
	private static final Method DO_AFTER_BODY = new Method("doAfterBody", Types.INT_VALUE, new Type[] {});

	// void doCatch(Throwable t)
	private static final Method DO_CATCH = new Method("doCatch", Types.VOID, new Type[] { Types.THROWABLE });

	// void doFinally()
	private static final Method DO_FINALLY = new Method("doFinally", Types.VOID, new Type[] {});

	// JspWriter popBody()
	private static final Method POP_BODY = new Method("popBody", Types.JSP_WRITER, new Type[] {});

	// void reuse(Tag tag)
	private static final Method RE_USE1 = new Method("reuse", Types.VOID, new Type[] { Types.TAG });
	private static final Method RE_USE3 = new Method("reuse", Types.VOID, new Type[] { Types.TAG, Types.STRING, Types.STRING });

	/**
	 * writes out the tag
	 * 
	 * @param tag
	 * @param bc
	 * @param doReuse
	 * @throws TransformerException
	 * @throws BundleException
	 * @throws ClassException
	 */
	public static void writeOut(Tag tag, BytecodeContext bc, boolean doReuse, final FlowControlFinal fcf) throws TransformerException {
		final GeneratorAdapter adapter = bc.getAdapter();
		final TagLibTag tlt = tag.getTagLibTag();

		final ClassDefinition cd = tlt.getTagClassDefinition();
		final boolean fromBundle = cd.getName() != null;
		final Type currType;
		final Type currDoFinallyType;

		if (fromBundle) {
			try {
				if (Reflector.isInstaneOf(cd.getClazz(), BodyTag.class, false)) currType = BODY_TAG;
				else currType = TAG;
				currDoFinallyType = TRY_CATCH_FINALLY_TAG;
			}
			catch (Exception e) {
				if (e instanceof TransformerException) throw (TransformerException) e;
				throw new TransformerException(e, tag.getStart());
			}
		}
		else {
			currDoFinallyType = currType = getTagType(tag);

		}

		final int currLocal = adapter.newLocal(currType);
		Label tagBegin = new Label();
		Label tagEnd = new Label();
		ExpressionUtil.visitLine(bc, tag.getStart());
		// TODO adapter.visitLocalVariable("tag", "L"+currType.getInternalName()+";", null, tagBegin,
		// tagEnd, currLocal);

		adapter.visitLabel(tagBegin);
		// tag=pc.use(String tagClassName,String tagBundleName, String tagBundleVersion, String fullname,int
		// attrType) throws PageException {
		adapter.loadArg(0);
		adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
		adapter.push(cd.getClassName());
		// has bundle info/version
		if (fromBundle) {
			// name
			adapter.push(cd.getName());
			// version
			if (cd.getVersion() != null) adapter.push(cd.getVersionAsString());
			else ASMConstants.NULL(adapter);
		}
		adapter.push(tlt.getFullName());
		adapter.push(tlt.getAttributeType());
		adapter.push((bc.getPageSource() == null ? "<memory>" : bc.getPageSource().getDisplayPath()) + ":" + ((tag.getStart() == null) ? 0 : tag.getStart().line));
		adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, fromBundle ? USE6 : USE4);
		if (currType != TAG) adapter.checkCast(currType);
		adapter.storeLocal(currLocal);

		TryFinallyVisitor outerTcfv = new TryFinallyVisitor(new OnFinally() {
			@Override
			public void _writeOut(BytecodeContext bc) {
				adapter.loadArg(0);
				adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
				adapter.loadLocal(currLocal);
				if (cd.getName() != null) {
					adapter.push(cd.getName());
					if (cd.getVersion() != null) adapter.push(cd.getVersionAsString());
					else ASMConstants.NULL(adapter);
				}
				adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, fromBundle ? RE_USE3 : RE_USE1);
			}
		}, null);
		if (doReuse) outerTcfv.visitTryBegin(bc);

		// appendix
		if (tlt.hasAppendix()) {
			adapter.loadLocal(currLocal);
			adapter.push(tag.getAppendix());
			if (fromBundle) // PageContextUtil.setAppendix(tag,appendix)
				ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, SET_APPENDIX2);
			else // tag.setAppendix(appendix)
				ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, SET_APPENDIX1);
		}

		// hasBody
		boolean hasBody = tag.getBody() != null;
		if (tlt.isBodyFree() && tlt.hasBodyMethodExists()) {
			adapter.loadLocal(currLocal);
			adapter.push(hasBody);

			if (fromBundle) // PageContextUtil.setAppendix(tag,appendix)
				ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, HAS_BODY2);
			else // tag.setAppendix(appendix)
				ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, HAS_BODY1);
		}

		// default attributes (get overwritten by attributeCollection because of that set before)
		setAttributes(bc, tag, currLocal, currType, true, fromBundle);

		// attributeCollection
		Attribute attrColl = tag.getAttribute("attributecollection");
		if (attrColl != null) {
			int attrType = tag.getTagLibTag().getAttributeType();
			if (TagLibTag.ATTRIBUTE_TYPE_NONAME != attrType) {
				tag.removeAttribute("attributecollection");
				// TagUtil.setAttributeCollection(Tag, Struct)
				adapter.loadArg(0);
				adapter.loadLocal(currLocal);
				if (currType != TAG) adapter.cast(currType, TAG);

				///
				TagLibTagAttr[] missings = tag.getMissingAttributes();
				if (!ArrayUtil.isEmpty(missings)) {
					ArrayVisitor av = new ArrayVisitor();
					av.visitBegin(adapter, MISSING_ATTRIBUTE, missings.length);
					int count = 0;
					TagLibTagAttr miss;
					for (int i = 0; i < missings.length; i++) {
						miss = missings[i];
						av.visitBeginItem(adapter, count++);
						bc.getFactory().registerKey(bc, bc.getFactory().createLitString(miss.getName()), false);
						adapter.push(miss.getType());
						if (ArrayUtil.isEmpty(miss.getAlias())) adapter.invokeStatic(MISSING_ATTRIBUTE, NEW_INSTANCE_MAX2);
						else {
							new LiteralStringArray(bc.getFactory(), miss.getAlias()).writeOut(bc, Expression.MODE_REF);
							adapter.invokeStatic(MISSING_ATTRIBUTE, NEW_INSTANCE_MAX3);
						}
						av.visitEndItem(bc.getAdapter());
					}
					av.visitEnd();
				}
				else {
					ASMConstants.NULL(adapter);
				}
				///
				attrColl.getValue().writeOut(bc, Expression.MODE_REF);

				adapter.push(attrType);
				adapter.invokeStatic(TAG_UTIL, SET_ATTRIBUTE_COLLECTION);
			}
		}

		// metadata
		Attribute attr;
		Map<String, Attribute> metadata = tag.getMetaData();
		if (metadata != null) {
			Iterator<Attribute> it = metadata.values().iterator();
			while (it.hasNext()) {
				attr = it.next();
				adapter.loadLocal(currLocal);
				adapter.push(attr.getName());
				attr.getValue().writeOut(bc, Expression.MODE_REF);

				if (fromBundle) ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, SET_META_DATA3);
				else ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, SET_META_DATA2);
			}
		}

		// set attributes
		setAttributes(bc, tag, currLocal, currType, false, fromBundle);
		// Body
		if (hasBody) {
			final int state = adapter.newLocal(Types.INT_VALUE);

			// int state=tag.doStartTag();
			adapter.loadLocal(currLocal);
			ASMUtil.invoke(fromBundle ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currType, DO_START_TAG);
			// adapter.invokeVirtual(currType, DO_START_TAG);
			adapter.storeLocal(state);

			// if (state!=Tag.SKIP_BODY)
			Label endBody = new Label();
			adapter.loadLocal(state);
			adapter.push(javax.servlet.jsp.tagext.Tag.SKIP_BODY);
			adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, endBody);
			// pc.initBody(tag, state);
			adapter.loadArg(0);
			adapter.loadLocal(currLocal);
			adapter.loadLocal(state);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, INIT_BODY);

			OnFinally onFinally = new OnFinally() {

				@Override
				public void _writeOut(BytecodeContext bc) {
					Label endIf = new Label();
					/*
					 * if(tlt.handleException() && fcf!=null && fcf.getAfterFinalGOTOLabel()!=null){
					 * ASMUtil.visitLabel(adapter, fcf.getFinalEntryLabel()); }
					 */
					adapter.loadLocal(state);
					adapter.push(javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE);
					adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, endIf);
					// ... pc.popBody();
					adapter.loadArg(0);
					adapter.invokeVirtual(Types.PAGE_CONTEXT, POP_BODY);
					adapter.pop();
					adapter.visitLabel(endIf);

					// tag.doFinally();
					if (tlt.handleException()) {
						adapter.loadLocal(currLocal);
						ASMUtil.invoke(fromBundle ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currDoFinallyType, DO_FINALLY);
						// adapter.invokeVirtual(currType, DO_FINALLY);
					}
					// GOTO after execution body, used when a continue/break was called before
					/*
					 * if(fcf!=null) { Label l = fcf.getAfterFinalGOTOLabel();
					 * if(l!=null)adapter.visitJumpInsn(Opcodes.GOTO, l); }
					 */

				}
			};

			if (tlt.handleException()) {
				TryCatchFinallyVisitor tcfv = new TryCatchFinallyVisitor(onFinally, fcf);
				tcfv.visitTryBegin(bc);
				doTry(bc, adapter, tag, currLocal, currType, fromBundle);
				int t = tcfv.visitTryEndCatchBeging(bc);
				// tag.doCatch(t);
				adapter.loadLocal(currLocal);
				adapter.loadLocal(t);
				// adapter.visitVarInsn(Opcodes.ALOAD,t);
				ASMUtil.invoke(fromBundle ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currDoFinallyType, DO_CATCH);
				// adapter.invokeVirtual(currType, DO_CATCH);
				tcfv.visitCatchEnd(bc);
			}
			else {
				TryFinallyVisitor tfv = new TryFinallyVisitor(onFinally, fcf);
				tfv.visitTryBegin(bc);
				doTry(bc, adapter, tag, currLocal, currType, fromBundle);
				tfv.visitTryEnd(bc);
			}

			adapter.visitLabel(endBody);

		}
		else {
			// tag.doStartTag();
			adapter.loadLocal(currLocal);
			ASMUtil.invoke(fromBundle ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currType, DO_START_TAG);
			// adapter.invokeVirtual(currType, DO_START_TAG);
			adapter.pop();
		}

		// if (tag.doEndTag()==Tag.SKIP_PAGE) throw new Abort(0<!-- SCOPE_PAGE -->);
		Label endDoEndTag = new Label();
		adapter.loadLocal(currLocal);
		ASMUtil.invoke(fromBundle ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currType, DO_END_TAG);
		// adapter.invokeVirtual(currType, DO_END_TAG);
		adapter.push(javax.servlet.jsp.tagext.Tag.SKIP_PAGE);
		adapter.visitJumpInsn(Opcodes.IF_ICMPNE, endDoEndTag);
		adapter.push(Abort.SCOPE_PAGE);
		adapter.invokeStatic(ABORT, NEW_INSTANCE);
		adapter.throwException();
		adapter.visitLabel(endDoEndTag);

		if (doReuse) {
			// } finally{pc.reuse(tag);}
			outerTcfv.visitTryEnd(bc);
		}

		adapter.visitLabel(tagEnd);
		ExpressionUtil.visitLine(bc, tag.getEnd());
	}

	private static void setAttributes(BytecodeContext bc, Tag tag, int currLocal, Type currType, boolean doDefault, boolean interf) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		Map<String, Attribute> attributes = tag.getAttributes();

		String methodName;
		Attribute attr;
		Iterator<Attribute> it = attributes.values().iterator();
		while (it.hasNext()) {
			attr = it.next();
			if (doDefault != attr.isDefaultAttribute()) continue;

			if (attr.isDynamicType()) {
				adapter.loadLocal(currLocal);
				if (interf) adapter.checkCast(Types.DYNAMIC_ATTRIBUTES);
				adapter.visitInsn(Opcodes.ACONST_NULL);
				// adapter.push(attr.getName());
				bc.getFactory().registerKey(bc, bc.getFactory().createLitString(attr.getName()), false);
				attr.getValue().writeOut(bc, Expression.MODE_REF);
				ASMUtil.invoke(interf ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, interf ? Types.DYNAMIC_ATTRIBUTES : currType, SET_DYNAMIC_ATTRIBUTE);
				// adapter.invokeVirtual(currType, SET_DYNAMIC_ATTRIBUTE);
			}
			else {
				// TagUtil.setAttribute(PageContext pc,boolean doDynamic,boolean silently,Tag tag, String
				// name,Object value)
				if (interf) {
					adapter.loadArg(0); // pc
					adapter.loadLocal(currLocal); // tag
					bc.getFactory().createLitString(attr.getName()).writeOut(bc, Expression.MODE_REF);// name
					attr.getValue().writeOut(bc, Expression.MODE_REF); // value
					adapter.invokeStatic(TAG_UTIL, SET_ATTRIBUTE4);

				}
				else {
					Type type = CastOther.getType(attr.getType());
					methodName = tag.getTagLibTag().getSetter(attr, type == null ? null : type.getClassName());
					adapter.loadLocal(currLocal);
					attr.getValue().writeOut(bc, Types.isPrimitiveType(type) ? Expression.MODE_VALUE : Expression.MODE_REF);
					adapter.invokeVirtual(currType, new Method(methodName, Type.VOID_TYPE, new Type[] { type }));
				}
			}
		}
	}

	private static void doTry(BytecodeContext bc, GeneratorAdapter adapter, Tag tag, int currLocal, Type currType, boolean interf) throws TransformerException {
		Label beginDoWhile = new Label();
		adapter.visitLabel(beginDoWhile);
		bc.setCurrentTag(currLocal);
		tag.getBody().writeOut(bc);

		// while (tag.doAfterBody()==BodyTag.EVAL_BODY_AGAIN);
		adapter.loadLocal(currLocal);
		if (interf) adapter.checkCast(Types.BODY_TAG);
		ASMUtil.invoke(interf ? ASMUtil.INTERFACE : ASMUtil.VIRTUAL, adapter, currType, DO_AFTER_BODY);
		// adapter.invokeVirtual(currType, DO_AFTER_BODY);
		adapter.push(IterationTag.EVAL_BODY_AGAIN);
		adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, beginDoWhile);
	}

	private static Type getTagType(Tag tag) throws TransformerException {
		TagLibTag tlt = tag.getTagLibTag();
		try {
			return Type.getType(tlt.getTagClassDefinition().getClazz());
			// return tlt.getTagType();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new TransformerException(t, tag.getStart());
		}
	}
}