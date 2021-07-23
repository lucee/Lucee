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
package lucee.transformer.bytecode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.InterfacePageImpl;
import lucee.runtime.Mapping;
import lucee.runtime.PageImpl;
import lucee.runtime.PageSource;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.Property;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Factory;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.ConstrBytecodeContext.Data;
import lucee.transformer.bytecode.statement.Argument;
import lucee.transformer.bytecode.statement.HasBodies;
import lucee.transformer.bytecode.statement.HasBody;
import lucee.transformer.bytecode.statement.IFunction;
import lucee.transformer.bytecode.statement.NativeSwitch;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagCIObject;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.statement.tag.TagImport;
import lucee.transformer.bytecode.statement.tag.TagInterface;
import lucee.transformer.bytecode.statement.tag.TagOther;
import lucee.transformer.bytecode.statement.tag.TagThread;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.statement.udf.FunctionImpl;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.bytecode.visitor.ConditionVisitor;
import lucee.transformer.bytecode.visitor.DecisionIntVisitor;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryCatchFinallyVisitor;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * represent a single Page
 */
public final class Page extends BodyBase implements Root {

	public static final Type NULL = Type.getType(lucee.runtime.type.Null.class);
	public static final Type KEY_IMPL = Type.getType(KeyImpl.class);
	public static final Type KEY_CONSTANTS = Type.getType(KeyConstants.class);

	public static final Method KEY_INIT = new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING });

	public static final Method KEY_INTERN = new Method("intern", Types.COLLECTION_KEY, new Type[] { Types.STRING });

	// public static ImportDefintion getInstance(String fullname,ImportDefintion defaultValue)
	private static final Method ID_GET_INSTANCE = new Method("getInstance", Types.IMPORT_DEFINITIONS, new Type[] { Types.STRING, Types.IMPORT_DEFINITIONS });

	public final static Method STATIC_CONSTRUCTOR = Method.getMethod("void <clinit> ()V");
	// public final static Method CONSTRUCTOR = Method.getMethod("void <init> ()V");

	private static final Method CONSTRUCTOR = new Method("<init>", Types.VOID, new Type[] {});

	/*
	 * private static final Method CONSTRUCTOR_STR = new Method( "<init>", Types.VOID, new
	 * Type[]{Types.STRING}// );
	 */

	private static final Method CONSTRUCTOR_PS = new Method("<init>", Types.VOID, new Type[] { Types.PAGE_SOURCE });

	// public static final Type STRUCT_IMPL = Type.getType(StructImpl.class);
	public static final Method INIT_STRUCT_IMPL = new Method("<init>", Types.VOID, new Type[] {});

	// void call (lucee.runtime.PageContext)
	private final static Method CALL1 = new Method("call", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });

	/*
	 * / void _try () private final static Method TRY = new Method( "_try", Types.VOID, new Type[]{} );
	 */

	// int getVersion()
	private final static Method VERSION = new Method("getVersion", Types.LONG_VALUE, new Type[] {});
	// void _init()
	private final static Method INIT_KEYS = new Method("initKeys", Types.VOID, new Type[] {});

	private final static Method SET_PAGE_SOURCE = new Method("setPageSource", Types.VOID, new Type[] { Types.PAGE_SOURCE });

	// public ImportDefintion[] getImportDefintions()
	private final static Method GET_IMPORT_DEFINITIONS = new Method("getImportDefintions", Types.IMPORT_DEFINITIONS_ARRAY, new Type[] {});

	private final static Method GET_SUB_PAGES = new Method("getSubPages", Types.CI_PAGE_ARRAY, new Type[] {});

	// long getSourceLastModified()
	private final static Method LAST_MOD = new Method("getSourceLastModified", Types.LONG_VALUE, new Type[] {});

	private final static Method COMPILE_TIME = new Method("getCompileTime", Types.LONG_VALUE, new Type[] {});

	private final static Method HASH = new Method("getHash", Types.INT_VALUE, new Type[] {});

	private final static Method LENGTH = new Method("getSourceLength", Types.LONG_VALUE, new Type[] {});
	private final static Method GET_SUBNAME = new Method("getSubname", Types.STRING, new Type[] {});

	private static final Type USER_DEFINED_FUNCTION = Type.getType(UDF.class);
	private static final Method UDF_CALL = new Method("udfCall", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, USER_DEFINED_FUNCTION, Types.INT_VALUE });

	private static final Method THREAD_CALL = new Method("threadCall", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.INT_VALUE });

	private static final Method UDF_DEFAULT_VALUE = new Method("udfDefaultValue", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT });

	private static final Method NEW_COMPONENT_IMPL_INSTANCE = new Method("newInstance", Types.COMPONENT_IMPL,
			new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE });

	private static final Method NEW_INTERFACE_IMPL_INSTANCE = new Method("newInstance", Types.INTERFACE_IMPL, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE });

	private static final Method STATIC_COMPONENT_CONSTR = new Method("staticConstructor", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_IMPL });

	// MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
	private static final Method CINIT = new Method("<clinit>", Types.VOID, new Type[] {});

	// public StaticStruct getStaticStruct()
	private static final Method GET_STATIC_STRUCT = new Method("getStaticStruct", Types.STATIC_STRUCT, new Type[] {});

	// void init(PageContext pc,Component Impl c) throws PageException
	private static final Method INIT_COMPONENT3 = new Method("initComponent", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_IMPL, Types.BOOLEAN_VALUE });
	private static final Method INIT_INTERFACE = new Method("initInterface", Types.VOID, new Type[] { Types.INTERFACE_IMPL });

	// public boolean setMode(int mode) {
	private static final Method SET_MODE = new Method("setMode", Types.INT_VALUE, new Type[] { Types.INT_VALUE });

	private static final Method CONSTR_INTERFACE_IMPL9 = new Method("<init>", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.INTERFACE_PAGE_IMPL, Types.STRING, // extends
			Types.STRING, // hind
			Types.STRING, // display
			Types.STRING, // callpath
			Types.BOOLEAN_VALUE, // realpath
			Types.MAP, // meta
			Types.INT_VALUE // index
	});
	private static final Method CONSTR_STATIC_STRUCT = new Method("<init>", Types.VOID, new Type[] {});

	// void init(PageContext pageContext,ComponentPage componentPage)
	private static final Method INIT_COMPONENT = new Method("init", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN_VALUE });

	private static final Method CHECK_INTERFACE = new Method("checkInterface", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL });

	// boolean getOutput()
	private static final Method GET_OUTPUT = new Method("getOutput", Types.BOOLEAN_VALUE, new Type[] {});

	private static final Method PUSH_BODY = new Method("pushBody", Types.BODY_CONTENT, new Type[] {});

	/*
	 * / boolean setSilent() private static final Method SET_SILENT = new Method( "setSilent",
	 * Types.BOOLEAN_VALUE, new Type[]{} );
	 */
	// Scope beforeCall(PageContext pc)
	private static final Method BEFORE_CALL = new Method("beforeCall", Types.VARIABLES, new Type[] { Types.PAGE_CONTEXT });

	private static final Method TO_PAGE_EXCEPTION = new Method("toPageException", Types.PAGE_EXCEPTION, new Type[] { Types.THROWABLE });

	// void afterCall(PageContext pc, Scope parent)
	private static final Method AFTER_CALL = new Method("afterConstructor", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.VARIABLES });

	private static final Method AFTER_STATIC_CONSTR = new Method("afterStaticConstructor", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.VARIABLES });
	private static final Method GET_INDEX = new Method("getIndex", Types.INT_VALUE, new Type[] {});
	private static final Method BEFORE_STATIC_CONSTR = new Method("beforeStaticConstructor", Types.VARIABLES, new Type[] { Types.PAGE_CONTEXT });

	private static final org.objectweb.asm.commons.Method CONSTRUCTOR_EMPTY = new org.objectweb.asm.commons.Method("<init>", Types.VOID, new Type[] {});

	// Component Impl(ComponentPage,boolean, String, String, String, String) WS==With Style
	private static final Method CONSTR_COMPONENT_IMPL16 = new Method("<init>", Types.VOID,
			new Type[] { Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN_VALUE,
					Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRUCT_IMPL, Types.INT_VALUE });
	private static final Method SET_EL = new Method("setEL", Types.OBJECT, new Type[] { Types.COLLECTION_KEY, Types.OBJECT });
	public static final Method UNDEFINED_SCOPE = new Method("us", Types.UNDEFINED, new Type[] {});
	private static final Method FLUSH_AND_POP = new Method("flushAndPop", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.BODY_CONTENT });
	private static final Method CLEAR_AND_POP = new Method("clearAndPop", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.BODY_CONTENT });
	public static final byte CF = (byte) 207;
	public static final byte _33 = (byte) 51;
	// private static final boolean ADD_C33 = false;
	// private static final String SUB_CALL_UDF = "udfCall";
	private static final String SUB_CALL_UDF = "_";
	private static final int DEFAULT_VALUE = 3;

	private final long version;
	private final long lastModifed;
	private final long length;
	private final boolean _writeLog;
	private final boolean suppressWSbeforeArg;
	private final boolean output;
	private final boolean returnValue;
	public final boolean ignoreScopes;
	// private final PageSource pageSource;

	// private boolean isComponent;
	// private boolean isInterface;

	private ArrayList<IFunction> functions = new ArrayList<IFunction>();
	private ArrayList<TagThread> threads = new ArrayList<TagThread>();
	private Resource staticTextLocation;
	private int off;
	private int methodCount = 0;
	// private final Config config;
	private boolean splitIfNecessary;
	private TagCIObject _comp;
	private String className; // following the pattern "or/susi/Sorglos"
	private Config config;
	private SourceCode sourceCode;
	private int hash;
	private List<JavaFunction> javaFunctions;
	private Set<String> javaFunctionNames;

	/**
	 * @param factory
	 * @param config
	 * @param sc SourceCode for this Page
	 * @param className name of the class produced (pattern: org.whatever.Susi)
	 * @param tc
	 * @param version
	 * @param lastModifed
	 * @param writeLog
	 * @param suppressWSbeforeArg
	 * @param dotNotationUpperCase
	 */
	public Page(Factory factory, Config config, SourceCode sc, TagCIObject tc, long version, long lastModifed, boolean writeLog, boolean suppressWSbeforeArg, boolean output,
			boolean returnValue, boolean ignoreScopes) {
		super(factory);
		this._comp = tc;
		this.version = version;
		this.lastModifed = lastModifed;
		this.length = sc instanceof PageSourceCode ? ((PageSourceCode) sc).getPageSource().getPhyscalFile().length() : 0;

		this._writeLog = writeLog;
		this.suppressWSbeforeArg = suppressWSbeforeArg;
		this.returnValue = returnValue;
		this.ignoreScopes = ignoreScopes;
		this.output = output;
		// this.pageSource=ps;
		this.config = config;
		this.sourceCode = sc;
		this.hash = sc.hashCode();
	}

	/**
	 * convert the Page Object to java bytecode
	 * 
	 * @param cn name of the genrated class (only necessary when Page object has no PageSource
	 *            reference)
	 * @return
	 * @throws TransformerException
	 */
	@Override
	public byte[] execute(String className) throws TransformerException {
		javaFunctions = null;// most likely not necessary
		// not exists in any case, so every usage must have a plan b for not existence
		PageSource optionalPS = sourceCode instanceof PageSourceCode ? ((PageSourceCode) sourceCode).getPageSource() : null;

		List<LitString> keys = new ArrayList<LitString>();
		ClassWriter cw = ASMUtil.getClassWriter();

		ArrayList<String> imports = new ArrayList<String>();
		getImports(imports, this);

		// look for component if necessary
		List<TagCIObject> comps = getTagCFObjects();

		// get class name

		if (!StringUtil.isEmpty(className)) {
			className = className.replace('.', '/');
			this.className = className;
		}
		else {
			className = getClassName();
		}
			if (optionalPS == null) throw new IllegalArgumentException("when Page object has no PageSource, a className is necessary");
			className = optionalPS.getClassName();
		}
		print.e("1->" + className);
		TagCIObject comp = null; // MUST remove
		if (comps != null && comps.size() > 0) {
			comp = comps.get(0);
			print.e("cn1->" + className);
			print.e("cn2->" + comp.getName());
			className = createSubClass(className, comp.getName(), sourceCode.getDialect());
		}

		className = className.replace('.', '/');
		this.className = className;
		print.e("2->" + className);

		// parent
		String parent = PageImpl.class.getName();// "lucee/runtime/Page";
		if (isComponent(comp)) parent = ComponentPageImpl.class.getName();// "lucee/runtime/ComponentPage";
		else if (isInterface(comp)) parent = InterfacePageImpl.class.getName();// "lucee/runtime/InterfacePage";
		parent = parent.replace('.', '/');

		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, className, null, parent, null);
		if (optionalPS != null) {
			// we use full path when FD is enabled
			String path = config.allowRequestTimeout() ? optionalPS.getRealpathWithVirtual() : optionalPS.getPhyscalFile().getAbsolutePath();
			cw.visitSource(path, null); // when adding more use ; as delimiter

			// cw.visitSource(optionalPS.getPhyscalFile().getAbsolutePath(),
			// "rel:"+optionalPS.getRealpathWithVirtual()); // when adding more use ; as delimiter
		}
		else {
			// cw.visitSource("","rel:");
		}

		// static constructor
		// GeneratorAdapter statConstrAdapter = new
		// GeneratorAdapter(Opcodes.ACC_PUBLIC,STATIC_CONSTRUCTOR,null,null,cw);
		// StaticConstrBytecodeContext statConstr = null;//new
		// BytecodeContext(null,null,this,externalizer,keys,cw,name,statConstrAdapter,STATIC_CONSTRUCTOR,writeLog(),suppressWSbeforeArg);

		boolean isSub = comp != null && !comp.isMain();

		// constructor
		GeneratorAdapter constrAdapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_PS, null, null, cw);
		ConstrBytecodeContext constr = new ConstrBytecodeContext(optionalPS, this, keys, cw, className, constrAdapter, CONSTRUCTOR_PS, writeLog(), suppressWSbeforeArg, output,
				returnValue);
		constrAdapter.loadThis();
		Type t;

		if (isComponent(comp)) {
			t = Types.COMPONENT_PAGE_IMPL;

			// extends
			// Attribute attr = comp.getAttribute("extends");
			// if(attr!=null) ExpressionUtil.writeOutSilent(attr.getValue(),constr, Expression.MODE_REF);
			// else constrAdapter.push("");

			constrAdapter.invokeConstructor(t, CONSTRUCTOR);
		}
		else if (isInterface(comp)) {
			t = Types.INTERFACE_PAGE_IMPL;
			constrAdapter.invokeConstructor(t, CONSTRUCTOR);
		}
		else {
			t = Types.PAGE_IMPL;
			constrAdapter.invokeConstructor(t, CONSTRUCTOR);
		}

		// call _init()
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		constrAdapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, constr.getClassName(), "initKeys", "()V");

		// private static ImportDefintion[] test=new ImportDefintion[]{...};
		{
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "imports", "[Llucee/runtime/component/ImportDefintion;", null, null);
			fv.visitEnd();

			constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
			ArrayVisitor av = new ArrayVisitor();
			av.visitBegin(constrAdapter, Types.IMPORT_DEFINITIONS, imports.size());
			int index = 0;
			Iterator<String> it = imports.iterator();
			while (it.hasNext()) {
				av.visitBeginItem(constrAdapter, index++);
				constrAdapter.push(it.next());
				ASMConstants.NULL(constrAdapter);
				constrAdapter.invokeStatic(Types.IMPORT_DEFINITIONS_IMPL, ID_GET_INSTANCE);
				av.visitEndItem(constrAdapter);
			}
			av.visitEnd();
			constrAdapter.visitFieldInsn(Opcodes.PUTFIELD, className, "imports", "[Llucee/runtime/component/ImportDefintion;");

		}

		// getVersion
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, VERSION, null, null, cw);
		adapter.push(version);
		adapter.returnValue();
		adapter.endMethod();

		// public ImportDefintion[] getImportDefintions()
		if (imports.size() > 0) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_IMPORT_DEFINITIONS, null, null, cw);
			adapter.visitVarInsn(Opcodes.ALOAD, 0);
			adapter.visitFieldInsn(Opcodes.GETFIELD, className, "imports", "[Llucee/runtime/component/ImportDefintion;");
			adapter.returnValue();
			adapter.endMethod();
		}
		else {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_IMPORT_DEFINITIONS, null, null, cw);
			adapter.visitInsn(Opcodes.ICONST_0);
			adapter.visitTypeInsn(Opcodes.ANEWARRAY, "lucee/runtime/component/ImportDefintion");
			adapter.returnValue();
			adapter.endMethod();
		}

		// getSourceLastModified
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, LAST_MOD, null, null, cw);
		adapter.push(lastModifed);
		adapter.returnValue();
		adapter.endMethod();

		// getSourceLength
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, LENGTH, null, null, cw);
		adapter.push(length);
		adapter.returnValue();
		adapter.endMethod();

		// getSubname
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_SUBNAME, null, null, cw);

		String subName;
		if (isSub) subName = getName(comp, null);
		else subName = null;

		if (subName != null) adapter.push(subName);
		else ASMConstants.NULL(adapter);
		adapter.returnValue();
		adapter.endMethod();

		// getCompileTime
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, COMPILE_TIME, null, null, cw);
		adapter.push(System.currentTimeMillis());
		adapter.returnValue();
		adapter.endMethod();

		// getHash
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, HASH, null, null, cw);
		adapter.push(hash);
		adapter.returnValue();
		adapter.endMethod();

		// static consructor for component/interface

		if (comps != null && comps.size() > 0) {
			writeOutStaticConstructor(constr, keys, cw, comps, className);
			writeOutGetStaticStruct(constr, keys, cw, comp, className);
			writeOutNewComponent(constr, keys, cw, comp, className);
			writeOutInitComponent(constr, keys, cw, comps, className);

		}
		else {
			funcs = writeOutCall(constr, keys, cw, className);
		}

		// write UDFProperties to constructor
		// writeUDFProperties(bc,funcs,pageType);

		// udfCall
		Function[] functions = getFunctions();// toArray(funcs);

		ConditionVisitor cv;
		DecisionIntVisitor div;
		// less/equal than 10 functions
		if (isInterface()) {
		}
		else if (functions.length <= 10) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, new Type[] { Types.THROWABLE }, cw);
			BytecodeContext bc = new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue);
			if (functions.length == 0) {
			}
			else if (functions.length == 1) {
				ExpressionUtil.visitLine(bc, functions[0].getStart());
				functions[0].getBody().writeOut(bc);
				ExpressionUtil.visitLine(bc, functions[0].getEnd());
			}
			else writeOutUdfCallInner(bc, functions, 0, functions.length);
			adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();
		}
		// more than 10 functions
		else {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, new Type[] { Types.THROWABLE }, cw);
			BytecodeContext bc = new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue);
			cv = new ConditionVisitor();
			cv.visitBefore();
			int count = 0;
			for (int i = 0; i < functions.length; i += 10) {
				cv.visitWhenBeforeExpr();
				div = new DecisionIntVisitor();
				div.visitBegin();
				adapter.loadArg(2);
				div.visitLT();
				adapter.push(i + 10);
				div.visitEnd(bc);
				cv.visitWhenAfterExprBeforeBody(bc);

				adapter.visitVarInsn(Opcodes.ALOAD, 0);
				adapter.visitVarInsn(Opcodes.ALOAD, 1);
				adapter.visitVarInsn(Opcodes.ALOAD, 2);
				adapter.visitVarInsn(Opcodes.ILOAD, 3);
				adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, createFunctionName(++count), "(Llucee/runtime/PageContext;Llucee/runtime/type/UDF;I)Ljava/lang/Object;");
				adapter.visitInsn(Opcodes.ARETURN);// adapter.returnValue();
				cv.visitWhenAfterBody(bc);
			}
			cv.visitAfter(bc);

			adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();

			count = 0;
			Method innerCall;
			for (int i = 0; i < functions.length; i += 10) {
				innerCall = new Method(createFunctionName(++count), Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, USER_DEFINED_FUNCTION, Types.INT_VALUE });

				adapter = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, innerCall, null, new Type[] { Types.THROWABLE }, cw);
				writeOutUdfCallInner(new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, innerCall, writeLog(), suppressWSbeforeArg, output, returnValue),
						functions, i, i + 10 > functions.length ? functions.length : i + 10);

				adapter.visitInsn(Opcodes.ACONST_NULL);
				adapter.returnValue();
				adapter.endMethod();
			}
		}

		// threadCall
		TagThread[] threads = getThreads();
		if (true) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, THREAD_CALL, null, new Type[] { Types.THROWABLE }, cw);
			if (threads.length > 0) writeOutThreadCallInner(
					new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, THREAD_CALL, writeLog(), suppressWSbeforeArg, output, returnValue), threads, 0,
					threads.length);
			// adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();
		}

		// udfDefaultValue
		// less/equal than 10 functions
		if (isInterface()) {
		}
		else if (functions.length <= 10) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
			if (functions.length > 0) writeUdfDefaultValueInner(
					new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(), suppressWSbeforeArg, output, returnValue), functions,
					0, functions.length);

			adapter.loadArg(DEFAULT_VALUE);
			adapter.returnValue();
			adapter.endMethod();
		}
		else {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
			BytecodeContext bc = new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(), suppressWSbeforeArg, output,
					returnValue);
			cv = new ConditionVisitor();
			cv.visitBefore();
			int count = 0;
			for (int i = 0; i < functions.length; i += 10) {
				cv.visitWhenBeforeExpr();
				div = new DecisionIntVisitor();
				div.visitBegin();
				adapter.loadArg(1);
				div.visitLT();
				adapter.push(i + 10);
				div.visitEnd(bc);
				cv.visitWhenAfterExprBeforeBody(bc);

				adapter.visitVarInsn(Opcodes.ALOAD, 0);
				adapter.visitVarInsn(Opcodes.ALOAD, 1);
				adapter.visitVarInsn(Opcodes.ILOAD, 2);
				adapter.visitVarInsn(Opcodes.ILOAD, 3);
				adapter.visitVarInsn(Opcodes.ALOAD, 4);

				adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "udfDefaultValue" + (++count), "(Llucee/runtime/PageContext;IILjava/lang/Object;)Ljava/lang/Object;");
				adapter.visitInsn(Opcodes.ARETURN);// adapter.returnValue();

				cv.visitWhenAfterBody(bc);
			}
			cv.visitAfter(bc);

			adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();

			count = 0;
			Method innerDefaultValue;
			for (int i = 0; i < functions.length; i += 10) {
				innerDefaultValue = new Method("udfDefaultValue" + (++count), Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT });
				adapter = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, innerDefaultValue, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
				writeUdfDefaultValueInner(
						new BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, innerDefaultValue, writeLog(), suppressWSbeforeArg, output, returnValue),
						functions, i, i + 10 > functions.length ? functions.length : i + 10);

				adapter.loadArg(DEFAULT_VALUE);
				// adapter.visitInsn(Opcodes.ACONST_NULL);
				adapter.returnValue();
				adapter.endMethod();
			}

		}

		// CONSTRUCTOR

		List<Data> udfProperties = constr.getUDFProperties();
		Iterator<Data> it = udfProperties.iterator();

		String udfpropsClassName = Types.UDF_PROPERTIES_ARRAY.toString();

		// new UDFProperties Array
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		constrAdapter.push(udfProperties.size());
		constrAdapter.newArray(Types.UDF_PROPERTIES);
		constrAdapter.visitFieldInsn(Opcodes.PUTFIELD, getClassName(), "udfs", udfpropsClassName);

		// set item
		Data data;
		int index = 0;
		while (it.hasNext()) {
			data = it.next();
			constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
			constrAdapter.visitFieldInsn(Opcodes.GETFIELD, constr.getClassName(), "udfs", Types.UDF_PROPERTIES_ARRAY.toString());
			constrAdapter.push(index++);
			data.function.createUDFProperties(constr, data.valueIndex, data.type);
			constrAdapter.visitInsn(Opcodes.AASTORE);
		}

		// setPageSource(pageSource);
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 1);
		constrAdapter.invokeVirtual(t, SET_PAGE_SOURCE);

		constrAdapter.returnValue();
		constrAdapter.endMethod();

		// INIT KEYS
		{
			GeneratorAdapter aInit = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, INIT_KEYS, null, null, cw);
			BytecodeContext bcInit = new BytecodeContext(optionalPS, constr, this, keys, cw, className, aInit, INIT_KEYS, writeLog(), suppressWSbeforeArg, output, returnValue);
			registerFields(bcInit, keys);
			aInit.returnValue();
			aInit.endMethod();
		}

		// set field subs
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "subs", "[Llucee/runtime/CIPage;", null, null);
		fv.visitEnd();

		// create sub components/interfaces
		/*
		 * if (comp != null && comp.getIndex() > 0) { List<TagCIObject> subs = getSubs(null); if
		 * (!ArrayUtil.isEmpty(subs)) { Iterator<TagCIObject> _it = subs.iterator(); TagCIObject tc; while
		 * (_it.hasNext()) { tc = _it.next(); tc.writeOut(null, this);// MUST do not pass null }
		 * writeGetSubPages(cw, className, subs, sourceCode.getDialect()); } }
		 */

		return cw.toByteArray();
	}

	public static String createSubClass(String name, int dialect) {
		String suffix = (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_CLASS_SUFFIX : Constants.LUCEE_CLASS_SUFFIX);
		name = name.toLowerCase();
		if (name.endsWith(suffix)) return name;
		return name + suffix;
	}

	public static String createSubClass(String name, String subName, int dialect) {
		// TODO handle special characters
		if (!StringUtil.isEmpty(subName)) {
			String suffix = (dialect == CFMLEngine.DIALECT_CFML ? Constants.CFML_CLASS_SUFFIX : Constants.LUCEE_CLASS_SUFFIX);
			subName = subName.toLowerCase();
			if (name.endsWith(suffix)) name = name.substring(0, name.length() - 3) + "$" + subName + suffix;
			else name += "$" + subName;
		}
		return name;
	}

	private void writeGetSubPages(ClassWriter cw, String name, List<TagCIObject> subs, int dialect) {
		// pageSource.getFullClassName().replace('.', '/');
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_SUB_PAGES, null, null, cw);
		Label endIF = new Label();

		// adapter.visitVarInsn(Opcodes.ALOAD, 0);
		// adapter.visitFieldInsn(Opcodes.GETFIELD, name, "subs", "[Llucee/runtime/CIPage;");
		// adapter.visitJumpInsn(Opcodes.IFNONNULL, endIF);
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.CI_PAGE, subs.size());
		Iterator<TagCIObject> it = subs.iterator();
		String className;
		int index = 0;
		while (it.hasNext()) {
			TagCIObject ci = it.next();
			av.visitBeginItem(adapter, index++);
			className = createSubClass(name, ci.getName(), dialect);
			// ASMConstants.NULL(adapter);
			adapter.visitTypeInsn(Opcodes.NEW, className);
			adapter.visitInsn(Opcodes.DUP);

			adapter.visitVarInsn(Opcodes.ALOAD, 0);
			adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name, "getPageSource", "()Llucee/runtime/PageSource;");

			adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "(Llucee/runtime/PageSource;)V");
			av.visitEndItem(adapter);
		}
		av.visitEnd();

		adapter.visitFieldInsn(Opcodes.PUTFIELD, name, "subs", "[Llucee/runtime/CIPage;");

		// adapter.visitLabel(endIF);

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, name, "subs", "[Llucee/runtime/CIPage;");

		adapter.returnValue();
		adapter.endMethod();

	}

	private String getName(TagCIObject ci, String defaultValue) {
		Attribute attr = ci.getAttribute("name");
		if (attr == null) return defaultValue;
		Expression val = attr.getValue();
		if (!(val instanceof LitString)) return defaultValue;

		return ((LitString) val).getString();
	}

	public String getClassName() {
		if (className == null) {
			// only main components have a pageSource
			PageSource optionalPS = sourceCode instanceof PageSourceCode ? ((PageSourceCode) sourceCode).getPageSource() : null;
			if (optionalPS != null) className = optionalPS.getClassName();
			else {
				TagCIObject comp = getTagCFObject(null);
				if (comp != null) {
					className = createSubClass(className, comp.getName(), sourceCode.getDialect());
				}
			}
			if (className != null) className = className.replace('.', '/');
			else {
				throw new IllegalArgumentException("You always need to defined a name for a sub component");
			}
			// in case we have a sub component

		}
		return className;
	}

	/**
	 * get the main component/interface from the Page
	 * 
	 * @return
	 * @throws TransformerException
	 */

	private TagCIObject getMainTagCFObject(TagCIObject defaultValue) {
		print.e("-------------- getMainTagCFObject --------------");
		List<TagCIObject> objs = getTagCFObjects();
		if (objs != null && objs.size() > 0) return objs.get(0);
		return defaultValue;
	}

	private List<TagCIObject> getTagCFObjects() {
		List<TagCIObject> objects = new ArrayList<>();
		// if (_comp != null) return _comp;

		// look for main
		Iterator<Statement> it = getStatements().iterator();
		Statement s;
		TagCIObject t;

		while (it.hasNext()) {
			s = it.next();
			if (s instanceof TagCIObject) {
				t = (TagCIObject) s;
				if (t.getIndex() > 0) _comp = t;
				objects.add(t);
			}
		}
		return objects;
	}

	private List<TagCIObject> getSubs(TagCIObject[] defaultValue) {
		Iterator<Statement> it = getStatements().iterator();
		Statement s;
		TagCIObject t;
		List<TagCIObject> subs = null;
		while (it.hasNext()) {
			s = it.next();
			if (s instanceof TagCIObject) {
				t = (TagCIObject) s;
				if (t.getIndex() <= 0) {
					if (subs == null) subs = new ArrayList<TagCIObject>();
					subs.add(t);
				}

			}
		}
		return subs;
	}

	private String createFunctionName(int i) {
		return "udfCall" + Integer.toString(i, Character.MAX_RADIX);
	}

	public boolean writeLog() {
		return _writeLog && !isInterface();
	}

	public static void registerFields(BytecodeContext bc, List<LitString> keys) throws TransformerException {
		// if(keys.size()==0) return;
		GeneratorAdapter ga = bc.getAdapter();

		FieldVisitor fv = bc.getClassWriter().visitField(Opcodes.ACC_PRIVATE, "keys", Types.COLLECTION_KEY_ARRAY.toString(), null, null);
		fv.visitEnd();

		int index = 0;
		LitString value;
		Iterator<LitString> it = keys.iterator();
		ga.visitVarInsn(Opcodes.ALOAD, 0);
		ga.push(keys.size());
		ga.newArray(Types.COLLECTION_KEY);
		while (it.hasNext()) {
			value = it.next();
			ga.dup();
			ga.push(index++);
			// value.setExternalize(false);
			ExpressionUtil.writeOutSilent(value, bc, Expression.MODE_REF);
			ga.invokeStatic(KEY_IMPL, KEY_INTERN);
			ga.visitInsn(Opcodes.AASTORE);
		}
		ga.visitFieldInsn(Opcodes.PUTFIELD, bc.getClassName(), "keys", Types.COLLECTION_KEY_ARRAY.toString());
	}

	private void writeUdfDefaultValueInner(BytecodeContext bc, Function[] functions, int offset, int length) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		ConditionVisitor cv = new ConditionVisitor();
		DecisionIntVisitor div;
		cv.visitBefore();
		for (int i = offset; i < length; i++) {
			cv.visitWhenBeforeExpr();
			div = new DecisionIntVisitor();
			div.visitBegin();
			adapter.loadArg(1);
			div.visitEQ();
			adapter.push(i);
			div.visitEnd(bc);
			cv.visitWhenAfterExprBeforeBody(bc);
			writeOutFunctionDefaultValueInnerInner(bc, functions[i]);
			cv.visitWhenAfterBody(bc);
		}
		cv.visitAfter(bc);
	}

	private void writeOutUdfCallInnerIf(BytecodeContext bc, Function[] functions, int offset, int length) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		ConditionVisitor cv = new ConditionVisitor();
		DecisionIntVisitor div;
		cv.visitBefore();
		for (int i = offset; i < length; i++) {
			cv.visitWhenBeforeExpr();
			div = new DecisionIntVisitor();
			div.visitBegin();
			adapter.loadArg(2);
			div.visitEQ();
			adapter.push(i);
			div.visitEnd(bc);
			cv.visitWhenAfterExprBeforeBody(bc);
			ExpressionUtil.visitLine(bc, functions[i].getStart());
			functions[i].getBody().writeOut(bc);
			ExpressionUtil.visitLine(bc, functions[i].getEnd());
			cv.visitWhenAfterBody(bc);
		}
		cv.visitAfter(bc);
	}

	private void writeOutUdfCallInner(BytecodeContext bc, Function[] functions, int offset, int length) throws TransformerException {
		NativeSwitch ns = new NativeSwitch(bc.getFactory(), 2, NativeSwitch.ARG_REF, null, null);

		for (int i = offset; i < length; i++) {
			ns.addCase(i, functions[i].getBody(), functions[i].getStart(), functions[i].getEnd(), true);
		}
		ns._writeOut(bc);
	}

	private void writeOutThreadCallInner(BytecodeContext bc, TagThread[] threads, int offset, int length) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		ConditionVisitor cv = new ConditionVisitor();
		DecisionIntVisitor div;
		cv.visitBefore();
		for (int i = offset; i < length; i++) {
			cv.visitWhenBeforeExpr();
			div = new DecisionIntVisitor();
			div.visitBegin();
			adapter.loadArg(1);
			div.visitEQ();
			adapter.push(i);
			div.visitEnd(bc);
			cv.visitWhenAfterExprBeforeBody(bc);
			Body body = threads[i].getRealBody();
			if (body != null) body.writeOut(bc);
			cv.visitWhenAfterBody(bc);
		}
		cv.visitAfter(bc);
	}

	private void writeOutGetStaticStruct(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, TagCIObject component, String name) throws TransformerException {
		// public final static StaticStruct _static = new StaticStruct();
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "staticStruct", "Llucee/runtime/component/StaticStruct;", null, null);
		fv.visitEnd();

		{
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_STATIC, CINIT, null, null, cw);
			ga.newInstance(Types.STATIC_STRUCT);
			ga.dup();
			ga.invokeConstructor(Types.STATIC_STRUCT, CONSTR_STATIC_STRUCT);
			ga.putStatic(Type.getType(name), "staticStruct", Types.STATIC_STRUCT);
			ga.returnValue();
			ga.endMethod();
		}

		// public StaticStruct getStaticStruct() {return _static;}
		{
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_STATIC_STRUCT, null, null, cw);
			ga.getStatic(Type.getType(name), "staticStruct", Types.STATIC_STRUCT);
			ga.returnValue();
			ga.endMethod();
		}

	}

	private void writeOutStaticConstructor(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, List<TagCIObject> components, String name)
			throws TransformerException {

		final GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, STATIC_COMPONENT_CONSTR, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(null, constr, this, keys, cw, name, adapter, STATIC_COMPONENT_CONSTR, writeLog(), suppressWSbeforeArg, output, returnValue);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		// Scope oldData=null;
		final int oldData = adapter.newLocal(Types.VARIABLES);
		ASMConstants.NULL(adapter);
		adapter.storeLocal(oldData);

		// push body
		int localBC = adapter.newLocal(Types.BODY_CONTENT);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, PUSH_BODY);
		adapter.storeLocal(localBC);

		// int oldCheckArgs= pc.undefinedScope().setMode(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
		final int oldCheckArgs = adapter.newLocal(Types.INT_VALUE);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE);
		adapter.push(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
		adapter.invokeInterface(Types.UNDEFINED, SET_MODE);
		adapter.storeLocal(oldCheckArgs);

		TryCatchFinallyVisitor tcf = new TryCatchFinallyVisitor(new OnFinally() {

			@Override
			public void _writeOut(BytecodeContext bc) {

				// undefined.setMode(oldMode);
				adapter.loadArg(0);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE);
				adapter.loadLocal(oldCheckArgs, Types.INT_VALUE);
				adapter.invokeInterface(Types.UNDEFINED, SET_MODE);
				adapter.pop();

				// c.afterCall(pc,_oldData);
				// adapter.loadThis();
				adapter.loadArg(1);
				adapter.loadArg(0);
				adapter.loadLocal(oldData); // old variables scope
				adapter.invokeVirtual(Types.COMPONENT_IMPL, AFTER_STATIC_CONSTR);

			}
		}, null);
		tcf.visitTryBegin(bc);
		// oldData=c.beforeCall(pc);
		adapter.loadArg(1);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.COMPONENT_IMPL, BEFORE_STATIC_CONSTR);
		adapter.storeLocal(oldData);

		if (components.size() == 1) {
			TagCIObject component = components.get(0);
			// body before
			List<StaticBody> list = component.getStaticBodies();
			if (list != null) {
				writeOutConstrBody(bc, list, IFunction.PAGE_TYPE_COMPONENT);
			}
			// body after
		}
		else {
			ConditionVisitor cv = new ConditionVisitor();
			DecisionIntVisitor div;
			cv.visitBefore();
			TagCIObject component;
			Iterator<TagCIObject> it = components.iterator();
			while (it.hasNext()) {
				component = it.next();

				cv.visitWhenBeforeExpr();
				div = new DecisionIntVisitor();
				div.visitBegin();
				adapter.push(component.getIndex());
				div.visitEQ();
				// cfc.getIndex();
				adapter.loadArg(1);
				adapter.invokeVirtual(Types.COMPONENT_IMPL, GET_INDEX);

				div.visitEnd(bc);
				cv.visitWhenAfterExprBeforeBody(bc);

				// body before
				List<StaticBody> list = component.getStaticBodies();
				if (list != null) {
					writeOutConstrBody(bc, list, IFunction.PAGE_TYPE_COMPONENT);
				}
				// body after

				cv.visitWhenAfterBody(bc);
			}
			cv.visitAfter(bc);
		}

		// ExpressionUtil.visitLine(bc, component.getEnd());
		int t = tcf.visitTryEndCatchBeging(bc);
		// BodyContentUtil.flushAndPop(pc,bc);
		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP);

		// throw Caster.toPageException(t);
		adapter.loadLocal(t);
		adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);
		adapter.throwException();
		tcf.visitCatchEnd(bc);

		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP);// TODO why does the body constuctor call clear and it works?

		adapter.returnValue();
		adapter.visitLabel(methodEnd);

		adapter.endMethod();
	}

	private void writeOutConstrBody(BytecodeContext bc, List<StaticBody> bodies, int pageType) throws TransformerException {
		// get and remove all functions from body
		List<IFunction> funcs = new ArrayList<IFunction>();

		Iterator<StaticBody> it = bodies.iterator();
		while (it.hasNext()) {
			extractFunctions(bc, it.next(), funcs, pageType);
		}
		writeUDFProperties(bc, funcs, pageType);

		it = bodies.iterator();
		while (it.hasNext()) {
			BodyBase.writeOut(bc, it.next());
		}
	}

	private void writeOutInitComponent(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, List<TagCIObject> components, String name) throws TransformerException {
		final GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_COMPONENT3, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(null, constr, this, keys, cw, name, adapter, INIT_COMPONENT3, writeLog(), suppressWSbeforeArg, output, returnValue);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		// Scope oldData=null;
		final int oldData = adapter.newLocal(Types.VARIABLES);
		ASMConstants.NULL(adapter);
		adapter.storeLocal(oldData);

		int localBC = adapter.newLocal(Types.BODY_CONTENT);
		ConditionVisitor cv = new ConditionVisitor();
		cv.visitBefore();
		cv.visitWhenBeforeExpr();
		adapter.loadArg(1);
		adapter.invokeVirtual(Types.COMPONENT_IMPL, GET_OUTPUT);
		cv.visitWhenAfterExprBeforeBody(bc);
		ASMConstants.NULL(adapter);
		cv.visitWhenAfterBody(bc);

		cv.visitOtherviseBeforeBody();
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, PUSH_BODY);
		cv.visitOtherviseAfterBody();
		cv.visitAfter(bc);
		adapter.storeLocal(localBC);

		// c.init(pc,this);
		adapter.loadArg(1);
		adapter.loadArg(0);
		adapter.loadThis();
		adapter.loadArg(2);
		// adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.invokeVirtual(Types.COMPONENT_IMPL, INIT_COMPONENT);

		// return when executeConstr is false
		adapter.loadArg(2);
		Label afterIf = new Label();
		adapter.visitJumpInsn(Opcodes.IFNE, afterIf);

		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BODY_CONTENT_UTIL, CLEAR_AND_POP);

		adapter.visitInsn(Opcodes.RETURN);
		adapter.visitLabel(afterIf);

		// int oldCheckArgs= pc.undefinedScope().setMode(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
		final int oldCheckArgs = adapter.newLocal(Types.INT_VALUE);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE);
		adapter.push(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
		adapter.invokeInterface(Types.UNDEFINED, SET_MODE);
		adapter.storeLocal(oldCheckArgs);

		TryCatchFinallyVisitor tcf = new TryCatchFinallyVisitor(new OnFinally() {

			@Override
			public void _writeOut(BytecodeContext bc) {

				// undefined.setMode(oldMode);
				adapter.loadArg(0);
				adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE);
				adapter.loadLocal(oldCheckArgs, Types.INT_VALUE);
				adapter.invokeInterface(Types.UNDEFINED, SET_MODE);
				adapter.pop();

				// c.afterCall(pc,_oldData);
				adapter.loadArg(1);
				adapter.loadArg(0);
				adapter.loadLocal(oldData);
				adapter.invokeVirtual(Types.COMPONENT_IMPL, AFTER_CALL);

			}
		}, null);
		tcf.visitTryBegin(bc);
		// oldData=c.beforeCall(pc);
		adapter.loadArg(1);
		adapter.loadArg(0);
		adapter.invokeVirtual(Types.COMPONENT_IMPL, BEFORE_CALL);
		adapter.storeLocal(oldData);

		if (components.size() == 1 || true) {
			TagCIObject component = components.get(0);
			// body before
			ExpressionUtil.visitLine(bc, component.getStart());
			writeOutCallBody(bc, component.getBody(), IFunction.PAGE_TYPE_COMPONENT);
			ExpressionUtil.visitLine(bc, component.getEnd());
			// body after
		}
		else {
			ConditionVisitor _cv = new ConditionVisitor();
			DecisionIntVisitor div;
			_cv.visitBefore();
			TagCIObject component;
			Iterator<TagCIObject> it = components.iterator();
			while (it.hasNext()) {
				component = it.next();

				_cv.visitWhenBeforeExpr();
				div = new DecisionIntVisitor();
				div.visitBegin();
				adapter.push(component.getIndex());
				div.visitEQ();
				// cfc.getIndex();
				adapter.loadArg(1);
				adapter.invokeVirtual(Types.COMPONENT_IMPL, GET_INDEX);

				div.visitEnd(bc);
				_cv.visitWhenAfterExprBeforeBody(bc);

				// body before
				ExpressionUtil.visitLine(bc, component.getStart());
				writeOutCallBody(bc, component.getBody(), IFunction.PAGE_TYPE_COMPONENT);
				ExpressionUtil.visitLine(bc, component.getEnd());
				// body after

				_cv.visitWhenAfterBody(bc);
			}
			_cv.visitAfter(bc);
		}

		int t = tcf.visitTryEndCatchBeging(bc);
		// BodyContentUtil.flushAndPop(pc,bc);
		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP);

		// throw Caster.toPageException(t);
		adapter.loadLocal(t);
		adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);
		adapter.throwException();
		tcf.visitCatchEnd(bc);

		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BODY_CONTENT_UTIL, CLEAR_AND_POP);

		adapter.returnValue();
		adapter.visitLabel(methodEnd);

		adapter.endMethod();
		return funcs;
	}

	private List<IFunction> writeOutInitInterface(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, Tag interf, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_INTERFACE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(null, constr, this, keys, cw, name, adapter, INIT_INTERFACE, writeLog(), suppressWSbeforeArg, output, returnValue);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		ExpressionUtil.visitLine(bc, interf.getStart());
		List<IFunction> funcs = writeOutCallBody(bc, interf.getBody(), IFunction.PAGE_TYPE_INTERFACE);
		ExpressionUtil.visitLine(bc, interf.getEnd());

		adapter.returnValue();
		adapter.visitLabel(methodEnd);

		adapter.endMethod();
		return funcs;
	}

	private void writeOutFunctionDefaultValueInnerInner(BytecodeContext bc, Function function) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		List<Argument> args = function.getArguments();

		if (args.size() == 0) {
			adapter.loadArg(DEFAULT_VALUE);
			adapter.returnValue();
			return;
		}

		Iterator<Argument> it = args.iterator();
		Argument arg;
		ConditionVisitor cv = new ConditionVisitor();
		DecisionIntVisitor div;
		cv.visitBefore();
		int count = 0;
		while (it.hasNext()) {
			arg = it.next();
			cv.visitWhenBeforeExpr();
			div = new DecisionIntVisitor();
			div.visitBegin();
			adapter.loadArg(2);
			div.visitEQ();
			adapter.push(count++);
			div.visitEnd(bc);
			cv.visitWhenAfterExprBeforeBody(bc);
			Expression defaultValue = arg.getDefaultValue();
			if (defaultValue != null) {
				/*
				 * if(defaultValue instanceof Null) { adapter.invokeStatic(NULL, GET_INSTANCE); } else
				 */
				defaultValue.writeOut(bc, Expression.MODE_REF);
			}
			else adapter.loadArg(DEFAULT_VALUE);
			// adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			cv.visitWhenAfterBody(bc);
		}
		cv.visitOtherviseBeforeBody();
		// adapter.visitInsn(ACONST_NULL);
		// adapter.returnValue();
		cv.visitOtherviseAfterBody();
		cv.visitAfter(bc);
	}

	private Function[] getFunctions() {
		Function[] funcs = new Function[functions.size()];
		Iterator it = functions.iterator();
		int count = 0;
		while (it.hasNext()) {
			funcs[count++] = (Function) it.next();
		}
		return funcs;
	}

	private TagThread[] getThreads() {
		TagThread[] threads = new TagThread[this.threads.size()];
		Iterator it = this.threads.iterator();
		int count = 0;
		while (it.hasNext()) {
			threads[count++] = (TagThread) it.next();
		}
		return threads;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {

	}

	private void writeOutNewComponent(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, Tag component, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_COMPONENT_IMPL_INSTANCE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(null, constr, this, keys, cw, name, adapter, NEW_COMPONENT_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output, returnValue);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		ExpressionUtil.visitLine(bc, component.getStart());
		adapter.visitLabel(methodBegin);

		int comp = adapter.newLocal(Types.COMPONENT_IMPL);
		adapter.newInstance(Types.COMPONENT_IMPL);
		adapter.dup();

		Attribute attr;
		// ComponentPage
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.checkCast(Types.COMPONENT_PAGE_IMPL);

		// !!! also check CFMLScriptTransformer.addMetaData if you do any change here !!!

		// Output
		attr = component.removeAttribute("output");
		if (attr != null) {
			ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		}
		else ASMConstants.NULL(adapter);

		// synchronized
		attr = component.removeAttribute("synchronized");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_VALUE);
		else adapter.push(false);

		// extends
		attr = component.removeAttribute("extends");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// implements
		attr = component.removeAttribute("implements");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// hint
		attr = component.removeAttribute("hint");
		if (attr != null) {
			Expression value = attr.getValue();
			if (!(value instanceof Literal)) {
				value = bc.getFactory().createLitString("[runtime expression]");
			}
			ExpressionUtil.writeOutSilent(value, bc, Expression.MODE_REF);
		}
		else adapter.push("");

		// dspName
		attr = component.removeAttribute("displayname");
		if (attr == null) attr = component.getAttribute("display");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// callpath
		adapter.visitVarInsn(Opcodes.ALOAD, 2);
		// realpath
		adapter.visitVarInsn(Opcodes.ILOAD, 3);

		// style
		attr = component.removeAttribute("style");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// persistent
		attr = component.removeAttribute("persistent");
		boolean persistent = false;
		if (attr != null) {
			persistent = ASMUtil.toBoolean(attr, component.getStart()).booleanValue();
		}

		// accessors
		attr = component.removeAttribute("accessors");
		boolean accessors = false;
		if (attr != null) {
			accessors = ASMUtil.toBoolean(attr, component.getStart()).booleanValue();
		}

		// modifier
		attr = component.removeAttribute("modifier");
		int modifiers = Component.MODIFIER_NONE;
		if (attr != null) {
			// type already evaluated in evaluator
			LitString ls = (LitString) component.getFactory().toExprString(attr.getValue());
			modifiers = ComponentUtil.toModifier(ls.getString(), lucee.runtime.Component.MODIFIER_NONE, lucee.runtime.Component.MODIFIER_NONE);
		}

		adapter.push(persistent);
		adapter.push(accessors);
		adapter.push(modifiers);
		adapter.visitVarInsn(Opcodes.ILOAD, 4);

		// adapter.visitVarInsn(Opcodes.ALOAD, 4);
		createMetaDataStruct(bc, component.getAttributes(), component.getMetaData());

		TagCIObject tc = (TagCIObject) component;
		adapter.push(tc.getIndex());

		print.e("iiiii:" + tc.getIndex());

		adapter.invokeConstructor(Types.COMPONENT_IMPL, CONSTR_COMPONENT_IMPL16);
		// INTERFACE_IMPL
		adapter.storeLocal(comp);

		// Component Impl(ComponentPage componentPage,boolean output, String extend, String hint, String
		// dspName)

		// initComponent(pc,c);
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.loadArg(0);
		adapter.loadLocal(comp);
		adapter.loadArg(4);
		adapter.invokeVirtual(Types.COMPONENT_PAGE_IMPL, INIT_COMPONENT3);

		adapter.visitLabel(methodEnd);

		// return component;
		adapter.loadLocal(comp);

		adapter.returnValue();
		// ExpressionUtil.visitLine(adapter, component.getEndLine());
		adapter.endMethod();

	}

	private void writeOutNewInterface(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, Tag interf, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_INTERFACE_IMPL_INSTANCE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(null, constr, this, keys, cw, name, adapter, NEW_INTERFACE_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output, returnValue);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		ExpressionUtil.visitLine(bc, interf.getStart());
		adapter.visitLabel(methodBegin);

		// ExpressionUtil.visitLine(adapter, interf.getStartLine());

		int comp = adapter.newLocal(Types.INTERFACE_IMPL);

		adapter.newInstance(Types.INTERFACE_IMPL);
		adapter.dup();

		// PageContext
		adapter.visitVarInsn(Opcodes.ALOAD, 1);

		// Interface Page
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.checkCast(Types.INTERFACE_PAGE_IMPL);

		// extened
		Attribute attr = interf.removeAttribute("extends");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// hint
		attr = interf.removeAttribute("hint");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// dspName
		attr = interf.removeAttribute("displayname");
		if (attr == null) attr = interf.getAttribute("display");
		if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
		else adapter.push("");

		// callpath
		adapter.visitVarInsn(Opcodes.ALOAD, 2);
		// relpath
		adapter.visitVarInsn(Opcodes.ILOAD, 3);

		// interface udfs
		// adapter.visitVarInsn(Opcodes.ALOAD, 3);

		createMetaDataStruct(bc, interf.getAttributes(), interf.getMetaData());

		TagCIObject tc = (TagCIObject) interf;

		adapter.push(tc.getIndex());

		adapter.invokeConstructor(Types.INTERFACE_IMPL, CONSTR_INTERFACE_IMPL9);

		adapter.storeLocal(comp);

		// initInterface(pc,c);
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		// adapter.loadArg(0);
		adapter.loadLocal(comp);
		adapter.invokeVirtual(Types.INTERFACE_PAGE_IMPL, INIT_INTERFACE);

		adapter.visitLabel(methodEnd);

		// return interface;
		adapter.loadLocal(comp);

		adapter.returnValue();
		// ExpressionUtil.visitLine(adapter, interf.getEndLine());
		adapter.endMethod();

	}

	public static boolean hasMetaDataStruct(Map attrs, Map meta) {
		if ((attrs == null || attrs.size() == 0) && (meta == null || meta.size() == 0)) {
			return false;
		}
		return true;
	}

	public static void createMetaDataStruct(BytecodeContext bc, Map attrs, Map meta) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();
		if ((attrs == null || attrs.size() == 0) && (meta == null || meta.size() == 0)) {
			ASMConstants.NULL(bc.getAdapter());
			bc.getAdapter().cast(Types.OBJECT, Types.STRUCT_IMPL);
			return;
		}

		int sct = adapter.newLocal(Types.STRUCT_IMPL);
		adapter.newInstance(Types.STRUCT_IMPL);
		adapter.dup();
		adapter.invokeConstructor(Types.STRUCT_IMPL, INIT_STRUCT_IMPL);
		adapter.storeLocal(sct);
		if (meta != null) {
			_createMetaDataStruct(bc, adapter, sct, meta);
		}
		if (attrs != null) {
			_createMetaDataStruct(bc, adapter, sct, attrs);
		}

		adapter.loadLocal(sct);
	}

	private static void _createMetaDataStruct(BytecodeContext bc, GeneratorAdapter adapter, int sct, Map attrs) throws TransformerException {
		Attribute attr;
		Iterator it = attrs.entrySet().iterator();
		Entry entry;
		while (it.hasNext()) {
			entry = (Map.Entry) it.next();
			attr = (Attribute) entry.getValue();
			adapter.loadLocal(sct);

			// adapter.push(attr.getName());
			bc.getFactory().registerKey(bc, bc.getFactory().createLitString(attr.getName()), false);
			if (attr.getValue() instanceof Literal) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF);
			else adapter.push("[runtime expression]");

			adapter.invokeVirtual(Types.STRUCT_IMPL, SET_EL);
			adapter.pop();
		}
	}

	private List<IFunction> writeOutCall(ConstrBytecodeContext constr, List<LitString> keys, ClassWriter cw, String name) throws TransformerException {
		// GeneratorAdapter adapter = bc.getAdapter();
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, CALL1, null, new Type[] { Types.THROWABLE }, cw);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		List<IFunction> funcs = writeOutCallBody(new BytecodeContext(null, constr, this, keys, cw, name, adapter, CALL1, writeLog(), suppressWSbeforeArg, output, returnValue),
				this, IFunction.PAGE_TYPE_REGULAR);

		adapter.visitLabel(methodEnd);
		adapter.returnValue();
		adapter.endMethod();
		return funcs;
	}

	private List<IFunction> writeOutCallBody(BytecodeContext bc, Body body, int pageType) throws TransformerException {
		List<IFunction> funcs = new ArrayList<IFunction>();
		extractFunctions(bc, body, funcs, pageType);
		writeUDFProperties(bc, funcs, pageType);

		// writeTags(bc, extractProperties(body));

		if (pageType != IFunction.PAGE_TYPE_INTERFACE) {
			int rtn = -1;
			if (bc.returnValue()) {
				rtn = bc.getAdapter().newLocal(Types.OBJECT);
				bc.setReturn(rtn);
				// make sure we have a value
				ASMConstants.NULL(bc.getAdapter());
				bc.getAdapter().storeLocal(rtn);
			}
			BodyBase.writeOut(bc, body);

			if (rtn != -1) bc.getAdapter().loadLocal(rtn);
			else ASMConstants.NULL(bc.getAdapter());
		}

		// checkInterface
		if (pageType == IFunction.PAGE_TYPE_COMPONENT) {
			GeneratorAdapter adapter = bc.getAdapter();
			adapter.loadArg(1);
			adapter.loadArg(0);
			adapter.visitVarInsn(Opcodes.ALOAD, 0);
			adapter.invokeVirtual(Types.COMPONENT_IMPL, CHECK_INTERFACE);
		}
		return funcs;
	}

	private void writeUDFProperties(BytecodeContext bc, List<IFunction> funcs, int pageType) throws TransformerException {
		// set items
		Iterator<IFunction> it = funcs.iterator();
		while (it.hasNext()) {
			it.next().writeOut(bc, pageType);
		}
	}

	private void writeTags(BytecodeContext bc, List<TagOther> tags) throws TransformerException {
		if (tags == null) return;

		Iterator<TagOther> it = tags.iterator();
		while (it.hasNext()) {
			it.next().writeOut(bc);
		}
	}

	private static void getImports(List<String> list, Body body) throws TransformerException {
		if (ASMUtil.isEmpty(body)) return;
		Statement stat;
		List<Statement> stats = body.getStatements();
		int len = stats.size();
		for (int i = 0; i < len; i++) {
			stat = stats.get(i);

			// IFunction
			if (stat instanceof TagImport && !StringUtil.isEmpty(((TagImport) stat).getPath(), true)) {
				ImportDefintion id = ImportDefintionImpl.getInstance(((TagImport) stat).getPath(), null);
				if (id != null && (!list.contains(id.toString()) && !list.contains(id.getPackage() + ".*"))) {
					list.add(id.toString());
				}
				stats.remove(i);
				len--;
				i--;

			}
			else if (stat instanceof HasBody) getImports(list, ((HasBody) stat).getBody());
			else if (stat instanceof HasBodies) {
				Body[] bodies = ((HasBodies) stat).getBodies();
				for (int y = 0; y < bodies.length; y++) {
					getImports(list, bodies[y]);
				}
			}
		}
	}

	private static void extractFunctions(BytecodeContext bc, Body body, List<IFunction> funcs, int pageType) throws TransformerException {
		if (ASMUtil.isEmpty(body)) return;

		Statement stat;
		List<Statement> stats = body.getStatements();
		int len = stats.size();
		for (int i = 0; i < len; i++) {
			stat = stats.get(i);

			// IFunction
			if (stat instanceof IFunction) {
				funcs.add((IFunction) stat);

				stats.remove(i);
				len--;
				i--;
			}
			else if (stat instanceof HasBody) {
				extractFunctions(bc, ((HasBody) stat).getBody(), funcs, pageType);
			}
			else if (stat instanceof HasBodies) {
				Body[] bodies = ((HasBodies) stat).getBodies();
				for (int y = 0; y < bodies.length; y++) {
					extractFunctions(bc, bodies[y], funcs, pageType);
				}
			}
		}
	}

	private static List<TagOther> extractProperties(Body body) throws TransformerException {
		if (ASMUtil.isEmpty(body)) return null;

		Statement stat;
		List<TagOther> properties = null;
		List<Statement> stats = body.getStatements();
		for (int i = stats.size() - 1; i >= 0; i--) {
			stat = stats.get(i);

			if (stat instanceof TagOther) {
				TagLibTag tlt = ((TagOther) stat).getTagLibTag();
				if (Property.class.getName().equals(tlt.getTagClassDefinition().getClassName())) {
					if (properties == null) properties = new ArrayList<TagOther>();
					properties.add(0, (TagOther) stat);
					stats.remove(i);
				}

			}
		}
		return properties;
	}

	/**
	 * @return if it is a component
	 */
	public boolean isComponent() {
		return isComponent(null);
		/*
		 * TagCFObject comp = getTagCFObject(null); if(comp!=null &&
		 * comp.getTagLibTag().getTagClassName().equals("lucee.runtime.tag.Component")) return true; return
		 * false;
		 */
	}

	/**
	 * @return if it is an interface
	 */
	public boolean isInterface() {
		return isInterface(null);
		/*
		 * TagCFObject comp = getTagCFObject(null); if(comp!=null &&
		 * comp.getTagLibTag().getTagClassName().equals("lucee.runtime.tag.Interface")) return true; return
		 * false;
		 */
	}

	public boolean isComponent(TagCIObject cio) {
		if (cio == null) cio = getMainTagCFObject(null);
		return cio instanceof TagComponent;

	}

	/**
	 * @return if it is an interface
	 */
	public boolean isInterface(TagCIObject cio) {
		if (cio == null) cio = getMainTagCFObject(null);
		return cio instanceof TagInterface;
	}

	public boolean isPage() {
		return getMainTagCFObject(null) == null;
	}

	/**
	 * @return the lastModifed
	 */
	public long getLastModifed() {
		return lastModifed;
	}

	@Override
	public int[] addFunction(IFunction function) {
		int[] indexes = new int[2];
		Iterator<IFunction> it = functions.iterator();
		while (it.hasNext()) {
			if (it.next() instanceof FunctionImpl) indexes[IFunction.ARRAY_INDEX]++;
		}
		indexes[IFunction.VALUE_INDEX] = functions.size();
		functions.add(function);
		return indexes;
	}

	@Override
	public String registerJavaFunctionName(String functionName) {
		String fn = Caster.toVariableName(functionName, null);
		if (fn == null) fn = "tmp" + HashUtil.create64BitHashAsString(functionName); // should never happen

		int count = 0;
		if (javaFunctionNames == null) javaFunctionNames = new HashSet<String>();
		String tmp = fn;
		while (javaFunctionNames.contains(tmp)) {
			tmp = fn + (count++);
		}
		javaFunctionNames.add(tmp);
		return tmp;
	}

	public int addThread(TagThread thread) {
		threads.add(thread);
		return threads.size() - 1;
	}

	public static byte[] setSourceLastModified(byte[] barr, long lastModified) {
		ClassReader cr = new ClassReader(barr);
		ClassWriter cw = ASMUtil.getClassWriter();
		ClassVisitor ca = new SourceLastModifiedClassAdapter(cw, lastModified);
		cr.accept(ca, 0);
		return cw.toByteArray();
	}

	/**
	 * return null if not possible to register
	 * 
	 * @param bc
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public Range registerString(BytecodeContext bc, String str) throws IOException {
		boolean append = true;

		if (staticTextLocation == null) {
			if (bc.getPageSource() == null) return null;

			PageSource ps = bc.getPageSource();
			Mapping m = ps.getMapping();
			staticTextLocation = m.getClassRootDirectory();

			staticTextLocation.mkdirs();
			staticTextLocation = staticTextLocation.getRealResource(ps.getClassName().replace('.', '/') + ".txt");
			if (staticTextLocation.exists()) append = false;
			else staticTextLocation.createFile(true);

			off = 0;
		}

		IOUtil.write(staticTextLocation, str, CharsetUtil.UTF8, append);
		Range r = new Range(off, str.length());
		off += str.length();
		return r;
	}

	public int getMethodCount() {
		return ++methodCount;
	}

	public SourceCode getSourceCode() {
		return sourceCode;
	}

	public void setSplitIfNecessary(boolean splitIfNecessary) {
		this.splitIfNecessary = splitIfNecessary;
	}

	public boolean getSplitIfNecessary() {
		return splitIfNecessary;
	}

	public boolean getSupressWSbeforeArg() {
		return suppressWSbeforeArg;
	}

	public boolean getOutput() {
		return output;
	}

	public boolean returnValue() {
		return returnValue;
	}

	public Config getConfig() {
		return config;
	}

	public void doFinalize(BytecodeContext bc) {
		ExpressionUtil.visitLine(bc, getEnd());
	}

	public void registerJavaFunction(JavaFunction javaFunction) {
		if (javaFunctions == null) javaFunctions = new ArrayList<>();
		javaFunctions.add(javaFunction);
	}

	public List<JavaFunction> getJavaFunctions() {
		return javaFunctions;
	}

	private static Function[] toArray(List<IFunction> funcs) {
		Function[] arr = new Function[funcs.size()];
		int index = 0;
		for (IFunction f: funcs) {
			arr[index++] = (Function) f;
		}
		return arr;
	}
}

class SourceLastModifiedClassAdapter extends ClassVisitor {

	private long lastModified;

	public SourceLastModifiedClassAdapter(ClassWriter cw, long lastModified) {
		super(Opcodes.ASM4, cw);
		this.lastModified = lastModified;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

		if (!name.equals("getSourceLastModified")) return super.visitMethod(access, name, desc, signature, exceptions);

		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		mv.visitCode();
		mv.visitLdcInsn(Long.valueOf(lastModified));
		mv.visitInsn(Opcodes.LRETURN);
		mv.visitEnd();
		return mv;
	}
}