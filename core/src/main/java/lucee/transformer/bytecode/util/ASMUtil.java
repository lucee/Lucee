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
package lucee.transformer.bytecode.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.aprint;
import lucee.commons.digest.MD5;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.component.Property;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.ScriptBody;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.VariableString;
import lucee.transformer.bytecode.literal.Identifier;
import lucee.transformer.bytecode.statement.FlowControl;
import lucee.transformer.bytecode.statement.FlowControlBreak;
import lucee.transformer.bytecode.statement.FlowControlContinue;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.statement.FlowControlRetry;
import lucee.transformer.bytecode.statement.HasBody;
import lucee.transformer.bytecode.statement.PrintOut;
import lucee.transformer.bytecode.statement.Switch;
import lucee.transformer.bytecode.statement.TryCatchFinally;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.statement.tag.TagTry;
import lucee.transformer.bytecode.util.SourceNameClassVisitor.SourceInfo;
import lucee.transformer.cast.Cast;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitNumber;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;

public final class ASMUtil {

	public static final short TYPE_ALL = 0;
	public static final short TYPE_BOOLEAN = 1;
	public static final short TYPE_NUMERIC = 2;
	public static final short TYPE_STRING = 4;

	public static final int INTERFACE = 1;
	public static final int VIRTUAL = 2;
	public static final int STATIC = 3;

	private final static Method CONSTRUCTOR_OBJECT = Method.getMethod("void <init> ()");
	private static final Method _SRC_NAME = new Method("_srcName", Types.STRING, new Type[] {});
	// private static final String VERSION_MESSAGE = "you use an invalid version of the ASM Jar, please
	// update your jar files";
	private static long id = 0;

	/**
	 * Gibt zurueck ob das direkt uebergeordnete Tag mit dem uebergebenen Full-Name (Namespace und Name)
	 * existiert.
	 * 
	 * @param el Startelement, von wo aus gesucht werden soll.
	 * @param fullName Name des gesuchten Tags.
	 * @return Existiert ein solches Tag oder nicht.
	 */
	public static boolean hasAncestorTag(Statement stat, String fullName) {
		return getAncestorTag(stat, fullName) != null;
	}

	/**
	 * Gibt das uebergeordnete CFXD Tag Element zurueck, falls dies nicht existiert wird null
	 * zurueckgegeben.
	 * 
	 * @param el Element von dem das parent Element zurueckgegeben werden soll.
	 * @return uebergeordnete CFXD Tag Element
	 */
	public static Tag getParentTag(Tag tag) {
		Statement p = tag.getParent();
		if (p == null) return null;
		p = p.getParent();
		if (p instanceof Tag) return (Tag) p;
		return null;
	}

	public static boolean isParentTag(Tag tag, String fullName) {
		Tag p = getParentTag(tag);
		if (p == null) return false;
		return p.getFullname().equalsIgnoreCase(fullName);

	}

	public static boolean isParentTag(Tag tag, Class clazz) {
		Tag p = getParentTag(tag);
		if (p == null) return false;
		return p.getClass() == clazz;

	}

	public static boolean hasAncestorRetryFCStatement(Statement stat, String label) {
		return getAncestorRetryFCStatement(stat, null, label) != null;
	}

	public static boolean hasAncestorBreakFCStatement(Statement stat, String label) {
		return getAncestorBreakFCStatement(stat, null, label) != null;
	}

	public static boolean hasAncestorContinueFCStatement(Statement stat, String label) {
		return getAncestorContinueFCStatement(stat, null, label) != null;
	}

	public static FlowControlRetry getAncestorRetryFCStatement(Statement stat, List<FlowControlFinal> finallyLabels, String label) {
		return (FlowControlRetry) getAncestorFCStatement(stat, finallyLabels, FlowControl.RETRY, label);
	}

	public static FlowControlBreak getAncestorBreakFCStatement(Statement stat, List<FlowControlFinal> finallyLabels, String label) {
		return (FlowControlBreak) getAncestorFCStatement(stat, finallyLabels, FlowControl.BREAK, label);
	}

	public static FlowControlContinue getAncestorContinueFCStatement(Statement stat, List<FlowControlFinal> finallyLabels, String label) {
		return (FlowControlContinue) getAncestorFCStatement(stat, finallyLabels, FlowControl.CONTINUE, label);
	}

	private static FlowControl getAncestorFCStatement(Statement stat, List<FlowControlFinal> finallyLabels, int flowType, String label) {
		Statement parent = stat;
		FlowControlFinal fcf;
		while (true) {
			parent = parent.getParent();
			if (parent == null) return null;
			if (((flowType == FlowControl.RETRY && parent instanceof FlowControlRetry) || (flowType == FlowControl.CONTINUE && parent instanceof FlowControlContinue)
					|| (flowType == FlowControl.BREAK && parent instanceof FlowControlBreak)) && labelMatch((FlowControl) parent, label)) {
				if (parent instanceof ScriptBody) {
					List<FlowControlFinal> _finallyLabels = finallyLabels == null ? null : new ArrayList<FlowControlFinal>();

					FlowControl scriptBodyParent = getAncestorFCStatement(parent, _finallyLabels, flowType, label);
					if (scriptBodyParent != null) {
						if (finallyLabels != null) {
							Iterator<FlowControlFinal> it = _finallyLabels.iterator();
							while (it.hasNext()) {
								finallyLabels.add(it.next());
							}
						}
						return scriptBodyParent;
					}
					return (FlowControl) parent;
				}
				return (FlowControl) parent;
			}

			// only if not last
			if (finallyLabels != null) {
				fcf = parent.getFlowControlFinal();
				if (fcf != null) {
					finallyLabels.add(fcf);
				}
			}

		}
	}

	private static boolean labelMatch(FlowControl fc, String label) {
		if (StringUtil.isEmpty(label, true)) return true;
		String fcl = fc.getLabel();
		if (StringUtil.isEmpty(fcl, true)) return false;

		return label.trim().equalsIgnoreCase(fcl.trim());
	}

	public static void leadFlow(BytecodeContext bc, Statement stat, int flowType, String label) throws TransformerException {
		List<FlowControlFinal> finallyLabels = new ArrayList<FlowControlFinal>();

		FlowControl fc;
		String name;
		if (FlowControl.BREAK == flowType) {
			fc = ASMUtil.getAncestorBreakFCStatement(stat, finallyLabels, label);
			name = "break";
		}
		else if (FlowControl.CONTINUE == flowType) {
			fc = ASMUtil.getAncestorContinueFCStatement(stat, finallyLabels, label);
			name = "continue";
		}
		else {
			fc = ASMUtil.getAncestorRetryFCStatement(stat, finallyLabels, label);
			name = "retry";
		}

		if (fc == null) throw new TransformerException(name + " must be inside a loop (for,while,do-while,<cfloop>,<cfwhile> ...)", stat.getStart());

		GeneratorAdapter adapter = bc.getAdapter();

		Label end;
		if (FlowControl.BREAK == flowType) end = ((FlowControlBreak) fc).getBreakLabel();
		else if (FlowControl.CONTINUE == flowType) end = ((FlowControlContinue) fc).getContinueLabel();
		else end = ((FlowControlRetry) fc).getRetryLabel();

		// first jump to all final labels
		FlowControlFinal[] arr = finallyLabels.toArray(new FlowControlFinal[finallyLabels.size()]);
		if (arr.length > 0) {
			FlowControlFinal fcf;
			for (int i = 0; i < arr.length; i++) {
				fcf = arr[i];

				// first
				if (i == 0) {
					adapter.visitJumpInsn(Opcodes.GOTO, fcf.getFinalEntryLabel());
				}

				// last
				if (arr.length == i + 1) fcf.setAfterFinalGOTOLabel(end);
				else fcf.setAfterFinalGOTOLabel(arr[i + 1].getFinalEntryLabel());
			}

		}
		else bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end);
	}

	public static boolean hasAncestorTryStatement(Statement stat) {
		return getAncestorTryStatement(stat) != null;
	}

	public static Statement getAncestorTryStatement(Statement stat) {
		Statement parent = stat;
		while (true) {
			parent = parent.getParent();
			if (parent == null) return null;

			if (parent instanceof TagTry) {
				return parent;
			}
			else if (parent instanceof TryCatchFinally) {
				return parent;
			}
		}
	}

	/**
	 * Gibt ein uebergeordnetes Tag mit dem uebergebenen Full-Name (Namespace und Name) zurueck, falls
	 * ein solches existiert, andernfalls wird null zurueckgegeben.
	 * 
	 * @param el Startelement, von wo aus gesucht werden soll.
	 * @param fullName Name des gesuchten Tags.
	 * @return uebergeornetes Element oder null.
	 */
	public static Tag getAncestorTag(Statement stat, String fullName) {
		Statement parent = stat;
		Tag tag;
		while (true) {
			parent = parent.getParent();
			if (parent == null) return null;
			if (parent instanceof Tag) {
				tag = (Tag) parent;
				if (tag.getFullname().equalsIgnoreCase(fullName)) return tag;
			}
		}
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static Boolean getAttributeBoolean(Tag tag, String attrName) throws EvaluatorException {
		Boolean b = getAttributeLiteral(tag, attrName).getBoolean(null);
		if (b == null) throw new EvaluatorException("attribute [" + attrName + "] must be a constant boolean value");
		return b;
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static Boolean getAttributeBoolean(Tag tag, String attrName, Boolean defaultValue) {
		Literal lit = getAttributeLiteral(tag, attrName, null);
		if (lit == null) return defaultValue;
		return lit.getBoolean(defaultValue);
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static String getAttributeString(Tag tag, String attrName) throws EvaluatorException {
		return getAttributeLiteral(tag, attrName).getString();
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static String getAttributeString(Tag tag, String attrName, String defaultValue) {
		Literal lit = getAttributeLiteral(tag, attrName, null);
		if (lit == null) return defaultValue;
		return lit.getString();
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static Literal getAttributeLiteral(Tag tag, String attrName) throws EvaluatorException {
		Attribute attr = tag.getAttribute(attrName);
		if (attr != null && attr.getValue() instanceof Literal) return ((Literal) attr.getValue());
		throw new EvaluatorException("attribute [" + attrName + "] must be a constant value");
	}

	/**
	 * extract the content of an attribute
	 * 
	 * @param cfxdTag
	 * @param attrName
	 * @return attribute value
	 * @throws EvaluatorException
	 */
	public static Literal getAttributeLiteral(Tag tag, String attrName, Literal defaultValue) {
		Attribute attr = tag.getAttribute(attrName);
		if (attr != null && attr.getValue() instanceof Literal) return ((Literal) attr.getValue());
		return defaultValue;
	}

	/**
	 * Prueft ob das das angegebene Tag in der gleichen Ebene nach dem angegebenen Tag vorkommt.
	 * 
	 * @param tag Ausgangspunkt, nach diesem tag darf das angegebene nicht vorkommen.
	 * @param nameToFind Tag Name der nicht vorkommen darf
	 * @return kommt das Tag vor.
	 */
	public static boolean hasSisterTagAfter(Tag tag, String nameToFind) {
		Body body = (Body) tag.getParent();
		List<Statement> stats = body.getStatements();
		Iterator<Statement> it = stats.iterator();
		Statement other;

		boolean isAfter = false;
		while (it.hasNext()) {
			other = it.next();

			if (other instanceof Tag) {
				if (isAfter) {
					if (((Tag) other).getTagLibTag().getName().equals(nameToFind)) return true;
				}
				else if (other == tag) isAfter = true;
			}
		}
		return false;
	}

	/**
	 * Prueft ob das angegebene Tag innerhalb seiner Ebene einmalig ist oder nicht.
	 * 
	 * @param tag Ausgangspunkt, nach diesem tag darf das angegebene nicht vorkommen.
	 * @return kommt das Tag vor.
	 */
	public static boolean hasSisterTagWithSameName(Tag tag) {

		Body body = (Body) tag.getParent();
		List<Statement> stats = body.getStatements();
		Iterator<Statement> it = stats.iterator();
		Statement other;
		String name = tag.getTagLibTag().getName();

		while (it.hasNext()) {
			other = it.next();
			if (other != tag && other instanceof Tag && ((Tag) other).getTagLibTag().getName().equals(name)) return true;

		}
		return false;
	}

	/**
	 * remove this tag from his parent body
	 * 
	 * @param tag
	 */
	public static void remove(Tag tag) {
		Body body = (Body) tag.getParent();
		body.getStatements().remove(tag);
	}

	public static void move(Tag src, Body dest) {
		// switch children
		Body srcBody = (Body) src.getParent();

		Iterator<Statement> it = srcBody.getStatements().iterator();
		Statement stat;
		while (it.hasNext()) {
			stat = it.next();
			if (stat == src) {
				it.remove();
				dest.addStatement(stat);
			}
		}

		// switch parent
		src.setParent(dest);

	}

	/**
	 * replace src with trg
	 * 
	 * @param src
	 * @param trg
	 */
	public static void replace(Tag src, Tag trg, boolean moveBody) {
		trg.setParent(src.getParent());

		Body p = (Body) src.getParent();
		List<Statement> stats = p.getStatements();
		Iterator<Statement> it = stats.iterator();
		Statement stat;
		int count = 0;

		while (it.hasNext()) {
			stat = it.next();
			if (stat == src) {
				if (moveBody && src.getBody() != null) src.getBody().setParent(trg);
				stats.set(count, trg);
				break;
			}
			count++;
		}
	}

	public static Page getAncestorPage(Statement stat) throws TransformerException {
		Statement parent = stat;
		while (true) {
			parent = parent.getParent();
			if (parent == null) {
				throw new TransformerException("missing parent Statement of Statement", stat.getStart());
				// return null;
			}
			if (parent instanceof Page) return (Page) parent;
		}
	}

	public static Page getAncestorPage(Statement stat, Page defaultValue) {
		Statement parent = stat;
		while (true) {
			parent = parent.getParent();
			if (parent == null) {
				return defaultValue;
			}
			if (parent instanceof Page) return (Page) parent;
		}
	}

	public static void invokeMethod(GeneratorAdapter adapter, Type type, Method method) {
		if (type.getClass().isInterface()) adapter.invokeInterface(type, method);
		else adapter.invokeVirtual(type, method);
	}

	public static byte[] createPojo(String className, ASMProperty[] properties, Class parent, Class[] interfaces, String srcName) throws PageException {
		className = className.replace('.', '/');
		className = className.replace('\\', '/');
		className = ListUtil.trim(className, "/");
		String[] inter = null;
		if (interfaces != null) {
			inter = new String[interfaces.length];
			for (int i = 0; i < inter.length; i++) {
				inter[i] = interfaces[i].getName().replace('.', '/');
			}
		}
		// CREATE CLASS
		ClassWriter cw = ASMUtil.getClassWriter();
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, parent.getName().replace('.', '/'), inter);
		String md5;
		try {
			md5 = createMD5(properties);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			md5 = "";
		}

		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "_md5_", "Ljava/lang/String;", null, md5);
		fv.visitEnd();

		// Constructor
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_OBJECT, null, null, cw);
		adapter.loadThis();
		adapter.invokeConstructor(toType(parent, true), CONSTRUCTOR_OBJECT);
		adapter.returnValue();
		adapter.endMethod();

		// properties
		for (int i = 0; i < properties.length; i++) {
			createProperty(cw, className, properties[i]);
		}

		// complexType src
		if (!StringUtil.isEmpty(srcName)) {
			GeneratorAdapter _adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, _SRC_NAME, null, null, cw);
			_adapter.push(srcName);
			_adapter.returnValue();
			_adapter.endMethod();
		}

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static void createProperty(ClassWriter cw, String classType, ASMProperty property) throws PageException {
		String name = property.getName();
		Type type = property.getASMType();
		Class clazz = property.getClazz();

		cw.visitField(Opcodes.ACC_PRIVATE, name, type.toString(), null, null).visitEnd();

		int load = loadFor(type);
		// int sizeOf=sizeOf(type);

		// get<PropertyName>():<type>
		Type[] types = new Type[0];
		Method method = new Method((clazz == boolean.class ? "get" : "get") + StringUtil.ucFirst(name), type, types);
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, cw);

		Label start = new Label();
		adapter.visitLabel(start);

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, classType, name, type.toString());
		adapter.returnValue();

		Label end = new Label();
		adapter.visitLabel(end);
		adapter.visitLocalVariable("this", "L" + classType + ";", null, start, end, 0);
		adapter.visitEnd();

		adapter.endMethod();

		// set<PropertyName>(object):void
		types = new Type[] { type };
		method = new Method("set" + StringUtil.ucFirst(name), Types.VOID, types);
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, cw);

		start = new Label();
		adapter.visitLabel(start);
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitVarInsn(load, 1);
		adapter.visitFieldInsn(Opcodes.PUTFIELD, classType, name, type.toString());

		adapter.visitInsn(Opcodes.RETURN);
		end = new Label();
		adapter.visitLabel(end);
		adapter.visitLocalVariable("this", "L" + classType + ";", null, start, end, 0);
		adapter.visitLocalVariable(name, type.toString(), null, start, end, 1);
		// adapter.visitMaxs(0, 0);//.visitMaxs(sizeOf+1, sizeOf+1);// hansx
		adapter.visitEnd();

		adapter.endMethod();

	}

	public static int loadFor(Type type) {
		if (type.equals(Types.BOOLEAN_VALUE) || type.equals(Types.INT_VALUE) || type.equals(Types.CHAR) || type.equals(Types.SHORT_VALUE)) return Opcodes.ILOAD;
		if (type.equals(Types.FLOAT_VALUE)) return Opcodes.FLOAD;
		if (type.equals(Types.LONG_VALUE)) return Opcodes.LLOAD;
		if (type.equals(Types.DOUBLE_VALUE)) return Opcodes.DLOAD;
		return Opcodes.ALOAD;
	}

	public static int sizeOf(Type type) {
		if (type.equals(Types.LONG_VALUE) || type.equals(Types.DOUBLE_VALUE)) return 2;
		return 1;
	}

	/**
	 * translate a string cfml type definition to a Type Object
	 * 
	 * @param cfType
	 * @param axistype
	 * @return
	 * @throws PageException
	 */
	public static Type toType(String cfType, boolean axistype) throws PageException {
		return toType(Caster.cfTypeToClass(cfType), axistype);
	}

	/**
	 * translate a string cfml type definition to a Type Object
	 * 
	 * @param cfType
	 * @param axistype
	 * @return
	 * @throws PageException
	 */
	public static Type toType(Class type, boolean axistype) throws PageException {
		if (axistype) type = ((ConfigWebPro) ThreadLocalPageContext.getConfig()).getWSHandler().toWSTypeClass(type);
		return Type.getType(type);
	}

	public static String createMD5(ASMProperty[] props) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < props.length; i++) {
			sb.append("name:" + props[i].getName() + ";");
			if (props[i] instanceof Property) {
				sb.append("type:" + ((Property) props[i]).getType() + ";");
			}
			else {
				try {
					sb.append("type:" + props[i].getASMType() + ";");

				}
				catch (PageException e) {
				}
			}
		}
		try {
			return MD5.getDigestAsString(sb.toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public static void removeLiterlChildren(Tag tag, boolean recursive) {
		Body body = tag.getBody();
		if (body != null) {
			List<Statement> list = body.getStatements();
			Statement[] stats = list.toArray(new Statement[list.size()]);
			PrintOut po;
			Tag t;
			for (int i = 0; i < stats.length; i++) {
				if (stats[i] instanceof PrintOut) {
					po = (PrintOut) stats[i];
					if (po.getExpr() instanceof Literal) {
						body.getStatements().remove(po);
					}
				}
				else if (recursive && stats[i] instanceof Tag) {
					t = (Tag) stats[i];
					if (t.getTagLibTag().isAllowRemovingLiteral()) {
						removeLiterlChildren(t, recursive);
					}
				}
			}
		}
	}

	public synchronized static String getId() {
		if (id < 0) id = 0;
		return StringUtil.addZeros(++id, 6);
	}

	public static boolean isEmpty(Body body) {
		return body == null || body.isEmpty();
	}

	/**
	 * @param adapter
	 * @param expr
	 * @param mode
	 */
	public static void pop(GeneratorAdapter adapter, Expression expr, int mode) {
		if (mode == Expression.MODE_VALUE && (expr instanceof ExprDouble)) adapter.pop2();
		else adapter.pop();
	}

	public static void pop(GeneratorAdapter adapter, Type type) {
		if (type.equals(Types.DOUBLE_VALUE)) adapter.pop2();
		else if (type.equals(Types.VOID)) {
		}
		else adapter.pop();
	}

	public static ClassWriter getClassWriter() {
		return new ClassWriter(ClassWriter.COMPUTE_MAXS);// |ClassWriter.COMPUTE_FRAMES);
	}

	public static String createOverfowMethod(String prefix, int id) { // pattern is used in function callstackget
		if (StringUtil.isEmpty(prefix)) prefix = "call";
		return prefix + "_" + StringUtil.addZeros(id, 6);
	}

	public static boolean isOverfowMethod(String name) {
		return name.length() > 6 && Decision.isNumber(name.substring(name.length() - 6, name.length()));
		// return name.startsWith("_call") && name.length()>=11;
	}

	public static boolean isDotKey(ExprString expr) {
		return expr instanceof LitString && !((LitString) expr).fromBracket();
	}

	public static String toString(Expression exp, String defaultValue) {
		try {
			return toString(exp);
		}
		catch (TransformerException e) {
			return defaultValue;
		}
	}

	public static String toString(Expression exp) throws TransformerException {
		if (exp instanceof Variable) {
			return toString(VariableString.toExprString(exp));
		}
		else if (exp instanceof VariableString) {
			return ((VariableString) exp).castToString();
		}
		else if (exp instanceof Literal) {
			return ((Literal) exp).toString();
		}
		return null;
	}

	public static Boolean toBoolean(Attribute attr, Position start) throws TransformerException {
		if (attr == null) throw new TransformerException("attribute does not exist", start);

		if (attr.getValue() instanceof Literal) {
			Boolean b = ((Literal) attr.getValue()).getBoolean(null);
			if (b != null) return b;
		}
		throw new TransformerException("attribute [" + attr.getName() + "] must be a constant boolean value", start);

	}

	public static Boolean toBoolean(Attribute attr, int line, Boolean defaultValue) {
		if (attr == null) return defaultValue;

		if (attr.getValue() instanceof Literal) {
			Boolean b = ((Literal) attr.getValue()).getBoolean(null);
			if (b != null) return b;
		}
		return defaultValue;
	}

	public static boolean isCFC(Statement s) {
		Statement p;
		while ((p = s.getParent()) != null) {
			s = p;
		}

		return true;
	}

	public static boolean isLiteralAttribute(Tag tag, String attrName, short type, boolean required, boolean throwWhenNot) throws EvaluatorException {
		return isLiteralAttribute(tag, tag.getAttribute(attrName), type, required, throwWhenNot);
	}

	public static boolean isLiteralAttribute(Tag tag, Attribute attr, short type, boolean required, boolean throwWhenNot) throws EvaluatorException {
		String strType = "/constant";
		if (attr != null && !isNull(attr.getValue())) {

			switch (type) {
			case TYPE_ALL:
				if (attr.getValue() instanceof Literal) return true;
				break;
			case TYPE_BOOLEAN:
				if (tag.getFactory().toExprBoolean(attr.getValue()) instanceof LitBoolean) return true;
				strType = " boolean";
				break;
			case TYPE_NUMERIC:
				if (tag.getFactory().toExprNumber(attr.getValue()) instanceof LitNumber) return true;
				strType = " numeric";
				break;
			case TYPE_STRING:
				if (tag.getFactory().toExprString(attr.getValue()) instanceof LitString) return true;
				strType = " string";
				break;
			}
			if (!throwWhenNot) return false;
			throw new EvaluatorException("Attribute [" + attr.getName() + "] of the Tag [" + tag.getFullname() + "] must be a literal" + strType + " value. "
					+ "attributes java class type " + attr.getValue().getClass().getName());
		}
		if (required) {
			if (!throwWhenNot) return false;
			throw new EvaluatorException("Attribute [" + attr.getName() + "] of the Tag [" + tag.getFullname() + "] is required");
		}
		return false;
	}

	public static boolean isNull(Expression expr) {
		if (expr instanceof Cast) {
			return isNull(((Cast) expr).getExpr());
		}
		return expr.getFactory().isNull(expr);
	}

	public static boolean isRefType(Type type) {
		return !(type == Types.BYTE_VALUE || type == Types.BOOLEAN_VALUE || type == Types.CHAR || type == Types.DOUBLE_VALUE || type == Types.FLOAT_VALUE || type == Types.INT_VALUE
				|| type == Types.LONG_VALUE || type == Types.SHORT_VALUE);
	}

	public static Type toRefType(Type type) {
		if (type == Types.BYTE_VALUE) return Types.BYTE;
		if (type == Types.BOOLEAN_VALUE) return Types.BOOLEAN;
		if (type == Types.CHAR) return Types.CHARACTER;
		if (type == Types.DOUBLE_VALUE) return Types.DOUBLE;
		if (type == Types.FLOAT_VALUE) return Types.FLOAT;
		if (type == Types.INT_VALUE) return Types.INTEGER;
		if (type == Types.LONG_VALUE) return Types.LONG;
		if (type == Types.SHORT_VALUE) return Types.SHORT;

		return type;
	}

	/**
	 * return value type only when there is one
	 * 
	 * @param type
	 * @return
	 */
	public static Type toValueType(Type type) {
		if (type == Types.BYTE) return Types.BYTE_VALUE;
		if (type == Types.BOOLEAN) return Types.BOOLEAN_VALUE;
		if (type == Types.CHARACTER) return Types.CHAR;
		if (type == Types.DOUBLE) return Types.DOUBLE_VALUE;
		if (type == Types.FLOAT) return Types.FLOAT_VALUE;
		if (type == Types.INTEGER) return Types.INT_VALUE;
		if (type == Types.LONG) return Types.LONG_VALUE;
		if (type == Types.SHORT) return Types.SHORT_VALUE;

		return type;
	}

	public static Class getValueTypeClass(Type type, Class defaultValue) {

		if (type == Types.BYTE_VALUE) return byte.class;
		if (type == Types.BOOLEAN_VALUE) return boolean.class;
		if (type == Types.CHAR) return char.class;
		if (type == Types.DOUBLE_VALUE) return double.class;
		if (type == Types.FLOAT_VALUE) return float.class;
		if (type == Types.INT_VALUE) return int.class;
		if (type == Types.LONG_VALUE) return long.class;
		if (type == Types.SHORT_VALUE) return short.class;

		return defaultValue;
	}

	public static ASMProperty[] toASMProperties(Property[] properties) {
		ASMProperty[] asmp = new ASMProperty[properties.length];
		for (int i = 0; i < asmp.length; i++) {
			asmp[i] = (ASMProperty) properties[i];
		}
		return asmp;
	}

	public static boolean containsComponent(Body body) {
		if (body == null) return false;

		Iterator<Statement> it = body.getStatements().iterator();
		while (it.hasNext()) {
			if (it.next() instanceof TagComponent) return true;
		}
		return false;
	}

	public static void dummy1(BytecodeContext bc) {
		bc.getAdapter().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
		bc.getAdapter().visitInsn(Opcodes.POP2);
	}

	public static void dummy2(BytecodeContext bc) {
		bc.getAdapter().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J");
		bc.getAdapter().visitInsn(Opcodes.POP2);
	}

	/**
	 * convert a clas array to a type array
	 * 
	 * @param classes
	 * @return
	 */
	public static Type[] toTypes(Class<?>[] classes) {
		if (classes == null || classes.length == 0) return new Type[0];

		Type[] types = new Type[classes.length];
		for (int i = 0; i < classes.length; i++) {
			types[i] = Type.getType(classes[i]);
		}
		return types;
	}

	public static String display(ExprString name) {
		if (name instanceof Literal) {
			if (name instanceof Identifier) return ((Identifier) name).getRaw();
			return ((Literal) name).getString();

		}
		return name.toString();
	}

	public static Literal cachedWithinValue(Expression val) throws EvaluatorException {
		if (val instanceof Literal) {
			Literal l = (Literal) val;

			// double == days
			Number n = l.getNumber(null);
			if (n != null) {
				return val.getFactory().createLitLong(TimeSpanImpl.fromDays(n.doubleValue()).getMillis(), null, null);
			}
			return l;
		}
		// createTimespan
		else if (val instanceof Variable) {
			Variable var = (Variable) val;
			if (var.getMembers().size() == 1) {
				Member first = var.getFirstMember();
				if (first instanceof BIF) {
					BIF bif = (BIF) first;
					if ("createTimeSpan".equalsIgnoreCase(bif.getFlf().getName())) {
						Argument[] args = bif.getArguments();
						int len = ArrayUtil.size(args);
						if (len >= 4 && len <= 5) {
							double days = toDouble(args[0].getValue());
							double hours = toDouble(args[1].getValue());
							double minutes = toDouble(args[2].getValue());
							double seconds = toDouble(args[3].getValue());
							double millis = len == 5 ? toDouble(args[4].getValue()) : 0;
							return val.getFactory().createLitLong(new TimeSpanImpl((int) days, (int) hours, (int) minutes, (int) seconds, (int) millis).getMillis(), null, null);
						}
					}
				}
			}
		}
		throw cacheWithinException();
	}

	private static EvaluatorException cacheWithinException() {
		return new EvaluatorException(
				"value of cachedWithin must be a literal value (string,boolean,number), a timespan can be defined as follows: 0.1 or createTimespan(1,2,3,4)");
	}

	private static double toDouble(Expression e) throws EvaluatorException {
		if (!(e instanceof Literal)) throw new EvaluatorException("Paremeters of the function createTimeSpan have to be literal numeric values in this context");
		Number n = ((Literal) e).getNumber(null);
		if (n == null) throw new EvaluatorException("Paremeters of the function createTimeSpan have to be literal numeric values in this context");

		return n.doubleValue();
	}

	public static void visitLabel(GeneratorAdapter ga, Label label) {
		if (label != null) ga.visitLabel(label);
	}

	public static String getClassName(Resource res) throws IOException {
		byte[] src = IOUtil.toBytes(res);
		ClassReader cr = new ClassReader(src);
		return cr.getClassName();
	}

	public static String getClassName(byte[] barr) {
		return new ClassReader(barr).getClassName();
	}

	public static SourceInfo getSourceInfo(Config config, Class clazz, boolean onlyCFC) throws IOException {
		return SourceNameClassVisitor.getSourceInfo(config, clazz, onlyCFC);
	}

	public static boolean hasOnlyDataMembers(Variable var) {
		Iterator<Member> it = var.getMembers().iterator();
		Member m;
		while (it.hasNext()) {
			m = it.next();
			if (!(m instanceof DataMember)) return false;
		}
		return true;
	}

	public static int count(List<Statement> statements, boolean recursive) {
		if (statements == null) return 0;
		int count = 0;
		Iterator<Statement> it = statements.iterator();
		while (it.hasNext()) {
			count += count(it.next(), recursive);
		}
		return count;
	}

	public static int count(Statement s, boolean recursive) {
		int count = 1;
		if (recursive && s instanceof HasBody) {
			Body b = ((HasBody) s).getBody();
			if (b != null) count += count(b.getStatements(), recursive);
		}
		return count;
	}

	public static void dump(Statement s, int level) {

		for (int i = 0; i < level; i++)
			System.err.print("-");
		aprint.e(s.getClass().getName());

		if (s instanceof HasBody) {
			Body b = ((HasBody) s).getBody();
			if (b != null) {
				Iterator<Statement> it = b.getStatements().iterator();
				while (it.hasNext()) {
					dump(it.next(), level + 1);
				}
			}
		}
	}

	public static void size(ClassWriter cw) {
		try {
			MethodVisitor mw = null;
			Field[] fields = cw.getClass().getDeclaredFields();
			Field f;
			for (int i = 0; i < fields.length; i++) {
				f = fields[i];
				if (f.getType().getName().equals("org.objectweb.asm.MethodWriter")) {
					f.setAccessible(true);
					mw = (MethodVisitor) f.get(cw);
					break;
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void invoke(int mode, GeneratorAdapter adapter, Type type, Method method) {
		if (mode == INTERFACE) adapter.invokeInterface(type, method);
		else if (mode == VIRTUAL) adapter.invokeVirtual(type, method);
		else if (mode == STATIC) adapter.invokeStatic(type, method);
	}

	public static BIF createBif(Data data, FunctionLibFunction flf) {
		BIF bif = new BIF(data.factory, data.settings, flf);
		data.ep.add(flf, bif, data.srcCode);
		bif.setArgType(flf.getArgType());
		try {
			bif.setClassDefinition(flf.getFunctionClassDefinition());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new PageRuntimeException(t);
		}
		bif.setReturnType(flf.getReturnTypeAsString());
		return bif;
	}

	public static boolean isOnlyDataMember(Variable v) {
		Iterator<Member> it = v.getMembers().iterator();

		while (it.hasNext()) {
			if (!(it.next() instanceof DataMember)) return false;
		}
		return true;
	}

	public static void addStatements(Body body, List<Statement> statements) {
		Iterator<Statement> it = statements.iterator();
		while (it.hasNext()) {
			body.addStatement(it.next());
		}
	}

	public static void createEmptyStruct(GeneratorAdapter adapter) {
		adapter.newInstance(Types.STRUCT_IMPL);
		adapter.dup();
		adapter.invokeConstructor(Types.STRUCT_IMPL, Page.INIT_STRUCT_IMPL);
	}

	public static void createEmptyArray(GeneratorAdapter adapter) {
		adapter.newInstance(Types.ARRAY_IMPL);
		adapter.dup();
		adapter.invokeConstructor(Types.ARRAY_IMPL, Switch.INIT);
	}

}