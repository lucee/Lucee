package lucee.transformer.cfml.script.java.function;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.SourceCode;
import lucee.runtime.Component;
import lucee.runtime.PageSource;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.transformer.bytecode.statement.Argument;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.literal.LitString;

public class JavaFunctionDef implements FunctionDef {

	private Class<?> javaClass;
	private String javaMethodName;
	protected Class<?>[] args;
	protected Class<?> rtn;
	protected boolean throwException;
	private static long _id = 0;

	public JavaFunctionDef(Class<?> javaClassName, String javaMethodName, Class<?>[] args, Class<?> rtn) {
		this(javaClassName, javaMethodName, args, rtn, false);
	}

	public JavaFunctionDef(Class<?> javaClassName, String javaMethodName, Class<?>[] args, Class<?> rtn, boolean throwException) {
		this.javaClass = javaClassName;
		this.javaMethodName = javaMethodName;
		this.args = args;
		this.rtn = rtn;
		this.throwException = throwException;
	}

	/*
	 * public Class<?> getClazz() { return clazz; }
	 */

	public final Class<?>[] getArgs() {
		return args;
	}

	public final Class<?> getRtn() {
		return rtn;
	}

	private Object toStringShort() {
		StringBuilder sb = new StringBuilder().append(javaMethodName).append('(');
		boolean del = false;
		for (Class<?> arg: args) {
			if (del) sb.append(',');
			sb.append(toString(arg));
			del = true;
		}
		return sb.append(')').toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (javaClass != null) sb.append(toString(javaClass)).append('.');
		return sb.append(toStringShort()).append('+').append(toString(rtn)).toString();
	}

	@Override
	public SourceCode createSourceCode(PageSource ps, String javaCode, final String id, final String functionName, int access, int modifier, String hint, List<Argument> listArgs,
			Boolean output, Boolean bufferOutput, String displayName, String description, int returnFormat, Boolean secureJson, Boolean verifyClient, int localMode) {
		String pack;
		String className;

		String[] argNames = toArgumentNames(listArgs);
		String[] argHints = toArgumentHints(listArgs);

		String parent = ps.getClassName();
		int index = parent.lastIndexOf('.');
		if (index == -1) {
			pack = "";
			className = parent + "$" + id + id(); // TODO id should not be necessary, but rename the class does not work, we first have to update ASM.
		}
		else {
			pack = parent.substring(0, index);
			className = parent.substring(index + 1) + "$" + id + CreateUniqueId.invoke();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("	public ").append(rtn.getName()).append(' ').append(this.javaMethodName).append('(');

		// args
		boolean del = false;
		String n;
		for (int i = 0; i < args.length; i++) {
			if (del) sb.append(',');
			sb.append(toString(args[i])).append(' ').append(argNames[i]);
			del = true;
		}

		sb.append(") ");
		if (throwException) sb.append(" throws Exception ");

		sb.append(StringUtil.replace(javaCode, "{", "{\n" + (throwException ? "" : "\ntry {") + "\nlucee.loader.engine.CFMLEngine engine = CFMLEngineFactory.getInstance();\n"
				+ "PageContext pc = engine.getThreadPageContext();\n", true));

		if (!throwException) sb.append(" catch(Exception eeeee){throw new RuntimeException(eeeee);}} ");
		sb.append(createConstructor(className, id, functionName, access, modifier, hint, output, bufferOutput, displayName, description, returnFormat, secureJson, verifyClient,
				localMode));
		sb.append(createCallFunction(javaClass));
		sb.append(createGetFunctionArguments(argNames, argHints));

		return outerShell(ps, pack, className, id, functionName, javaClass, sb.toString());
	}

	private SourceCode outerShell(final PageSource ps, final String pack, final String className, final String id, final String functionName, Class<?> javaFunctionClass,
			String javaFunctionCode) {

		StringBuilder sb = new StringBuilder();
		if (!pack.isEmpty()) sb.append("package " + pack + ";\n");

		sb.append("import lucee.runtime.PageContext;\n");
		sb.append("import lucee.runtime.config.Config;\n");
		sb.append("import lucee.runtime.type.Query;\n");
		sb.append("import lucee.runtime.ext.function.BIF;\n");
		sb.append("import lucee.loader.engine.CFMLEngineFactory;\n");
		sb.append("import lucee.runtime.Component;\n");
		sb.append("import lucee.runtime.PageSource;\n");
		sb.append("import lucee.runtime.dump.DumpData;\n");
		sb.append("import lucee.runtime.type.FunctionArgument;\n");
		sb.append("import lucee.loader.engine.CFMLEngine;\n");
		sb.append("import lucee.runtime.exp.PageException;\n");
		sb.append("import lucee.runtime.type.Struct;\n");
		sb.append("import lucee.runtime.dump.DumpProperties;\n");
		sb.append("import lucee.runtime.type.UDF;\n");
		sb.append("import lucee.runtime.type.Collection.Key;\n");
		sb.append("import lucee.runtime.op.Caster;\n");
		sb.append("import lucee.runtime.type.FunctionArgumentImpl;\n");

		sb.append("public class " + className);
		sb.append(" extends lucee.runtime.JF ");
		sb.append(" implements UDF");
		if (javaFunctionClass != null) sb.append("," + javaFunctionClass.getName());
		sb.append(" {\n");

		if (!StringUtil.isEmpty(javaFunctionCode)) sb.append(javaFunctionCode);

		sb.append("}");
		return new SourceCode(functionName, pack.isEmpty() ? className : pack + "." + className, sb.toString());
	}

	private String createConstructor(String className, String id, String functionName, int access, int modifier, String hint, Boolean output, Boolean bufferOutput,
			String displayName, String description, int returnFormat, Boolean secureJson, Boolean verifyClient, int localMode) {
		StringBuilder sb = new StringBuilder();
		sb.append("public " + className + "() {\n");

		// super(functionName, type, strType, description)
		sb.append("super(");
		sb.append(esc(functionName)).append(',');
		sb.append(access).append(',');
		sb.append(modifier).append(',');
		sb.append("(short)").append(CFTypes.toShortStrict(Caster.toTypeName(rtn), (short) 0)).append(',');
		sb.append(esc(Caster.toTypeName(rtn))).append(',');
		sb.append(esc(hint)).append(',');
		sb.append(b(output)).append(',');
		sb.append(b(bufferOutput)).append(',');
		sb.append(esc(displayName)).append(',');
		sb.append(esc(description)).append(',');
		sb.append(returnFormat).append(',');
		sb.append(b(secureJson)).append(',');
		sb.append(b(verifyClient)).append(',');
		sb.append(localMode);
		sb.append(");\n");

		sb.append("}\n");

		return sb.toString();
	}

	private String b(Boolean b) {
		if (b == null) return "null";
		return b ? "Booleam.TRUE" : "Boolean.FALSE";
	}

	private String createCallFunction(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("public Object call(PageContext pc, Object[] args, boolean bbbbbbbbbbbb) throws PageException {\n");
		sb.append("CFMLEngine engine = CFMLEngineFactory.getInstance();\n");

		sb.append("	if (args.length == " + args.length + ") {\n");
		sb.append("	try {\n");
		if (void.class != rtn) sb.append("		return ");
		sb.append(javaMethodName).append('(');
		StringBuilder types = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (types.length() > 0) types.append(", ");
			types.append(args[i].getName());

			if (i > 0) sb.append(", ");
			if (Object.class != args[i]) caster(sb, args[i]);
			sb.append("args[").append(i).append(']');
			if (Object.class != args[i]) sb.append(')');
		}

		sb.append(");\n");
		if (void.class == rtn) sb.append("		return null;\n");

		sb.append("	}\n");
		sb.append("	catch (Exception e) {\n");
		sb.append("		throw Caster.toPageException(e);\n");
		sb.append("	}\n");

		sb.append("	}\n");

		sb.append("	throw engine.getExceptionUtil().createApplicationException(\"invalid argument count (\" + args.length + \"), java function ["
				+ (clazz == null ? "" : (clazz.getName() + ".")) + javaMethodName + "(" + types + ")] takes " + args.length + " argument" + (args.length == 1 ? "" : "s")
				+ "\");\n");
		sb.append("}\n");

		return sb.toString();
	}

	private String createGetFunctionArguments(String[] argNames, String[] argHints) {
		StringBuilder sb = new StringBuilder();
		sb.append("public FunctionArgument[] getFunctionArguments() {\n");
		sb.append("	return new FunctionArgument[] {");
		for (int i = 0; i < argNames.length; i++) {
			if (i > 0) sb.append(',');
			sb.append("new FunctionArgumentImpl(lucee.runtime.type.KeyImpl.init(").append(esc(argNames[i])).append("),").append(esc(Caster.toClassName(args[i]))).append(",")
					.append("(short)").append(CFTypes.toShortStrict(Caster.toTypeName(args[i]), (short) 0)).append(',')
					.append("false,FunctionArgument.DEFAULT_TYPE_NULL, true,\"\",").append(esc(argHints[i])).append(")\n");
		}
		sb.append("};\n");
		sb.append("}\n");

		return sb.toString();
	}

	private String esc(String str) {
		if (str == null) return "\"\"";
		return '"' + StringUtil.replace(StringUtil.replace(str, "\n", "\\n", false), "\"", "\\\"", false) + '"';
	}

	protected static final Object toString(Class<?> clazz) {
		return StringUtil.replace(Caster.toClassName(clazz), "java.lang.", "", true);
	}

	protected void caster(StringBuilder sb, Class<?> clazz) {
		if (long.class == clazz) sb.append("engine.getCastUtil().toLongValue(");
		else if (Long.class == clazz) sb.append("engine.getCastUtil().toLong(");
		else if (double.class == clazz) sb.append("engine.getCastUtil().toDoubleValue(");
		else if (Double.class == clazz) sb.append("engine.getCastUtil().toDouble(");
		else if (int.class == clazz) sb.append("engine.getCastUtil().toIntValue(");
		else if (Integer.class == clazz) sb.append("engine.getCastUtil().toInteger(");
		else if (boolean.class == clazz) sb.append("engine.getCastUtil().toBooleanValue(");
		else if (Boolean.class == clazz) sb.append("engine.getCastUtil().toBoolean(");
		else if (char.class == clazz) sb.append("engine.getCastUtil().toCharValue(");
		else if (Character.class == clazz) sb.append("engine.getCastUtil().toCharacter(");
		else if (short.class == clazz) sb.append("engine.getCastUtil().toShortValue(");
		else if (Short.class == clazz) sb.append("engine.getCastUtil().toShort(");
		else if (byte.class == clazz) sb.append("engine.getCastUtil().toByteValue(");
		else if (Byte.class == clazz) sb.append("engine.getCastUtil().toByte(");
		else if (float.class == clazz) sb.append("engine.getCastUtil().toFloatValue(");
		else if (Float.class == clazz) sb.append("engine.getCastUtil().toFloat(");

		else if (Array.class == clazz) sb.append("engine.getCastUtil().toArray(");
		else if (BigDecimal.class == clazz) sb.append("engine.getCastUtil().toBigDecimal(");
		else if (BigInteger.class == clazz) sb.append("engine.getCastUtil().toBigInteger(");
		else if (byte[].class == clazz) sb.append("engine.getCastUtil().toBinary(");
		else if (CharSequence.class == clazz) sb.append("engine.getCastUtil().toCharSequence(");
		else if (Collection.class == clazz) sb.append("engine.getCastUtil().toCollection(");
		else if (Component.class == clazz) sb.append("engine.getCastUtil().toComponent(");
		else if (File.class == clazz) sb.append("engine.getCastUtil().toFile(");
		else if (UDF.class == clazz) sb.append("engine.getCastUtil().toFunction(");
		else if (Iterator.class == clazz) sb.append("engine.getCastUtil().toIterator(");
		else if (java.util.Collection.class == clazz) sb.append("engine.getCastUtil().toJavaCollection(");
		else if (List.class == clazz) sb.append("engine.getCastUtil().toList(");
		else if (Locale.class == clazz) sb.append("engine.getCastUtil().toLocale(");
		else if (Map.class == clazz) sb.append("engine.getCastUtil().toMap(");
		else if (Object[].class == clazz) sb.append("engine.getCastUtil().toNativeArray(");
		else if (Node.class == clazz) sb.append("engine.getCastUtil().toNode(");
		else if (NodeList.class == clazz) sb.append("engine.getCastUtil().toNodeList(");
		else if (Query.class == clazz) sb.append("engine.getCastUtil().toQuery(");
		else if (String.class == clazz) sb.append("engine.getCastUtil().toString(");
		else if (Struct.class == clazz) sb.append("engine.getCastUtil().toStruct(");
		else if (TimeSpan.class == clazz) sb.append("engine.getCastUtil().toTimeSpan(");
		else if (TimeZone.class == clazz) sb.append("engine.getCastUtil().toTimeZone(");
		else if (Object.class == clazz) sb.append("(");
		else sb.append("(" + Caster.toClassName(clazz) + ")(");
	}

	private static String[] toArgumentNames(List<Argument> args) {
		if (args == null) return new String[0];
		Iterator<Argument> it = args.iterator();
		String[] arr = new String[args.size()];
		ExprString es;
		int i = 0;
		while (it.hasNext()) {
			es = it.next().getName();
			if (es instanceof LitString) arr[i] = ((LitString) es).getString();
			if (StringUtil.isEmpty(arr[i])) arr[i] = "arg" + (i + 1);
			i++;
		}
		return arr;
	}

	private static String[] toArgumentHints(List<Argument> args) {
		if (args == null) return new String[0];
		Iterator<Argument> it = args.iterator();
		String[] arr = new String[args.size()];
		ExprString es;
		int i = 0;
		while (it.hasNext()) {
			es = it.next().getHint();
			if (es instanceof LitString) arr[i] = ((LitString) es).getString();
			if (StringUtil.isEmpty(arr[i])) arr[i] = "";
			i++;
		}
		return arr;
	}

	public static synchronized String id() {
		_id++;
		if (_id < 0) _id = 1;
		return Long.toString(_id, Character.MAX_RADIX);
	}
}
