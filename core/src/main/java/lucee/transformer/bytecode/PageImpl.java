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
package lucee.transformer.bytecode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.runtime.Component;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.InterfacePageImpl;
import lucee.runtime.Mapping;
import lucee.runtime.PageSource;
import lucee.runtime.SubPage;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.PageContextUtil;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.Page;
import lucee.transformer.Range;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.ConstrBytecodeContext.Data;
import lucee.transformer.bytecode.literal.LitBooleanImpl;
import lucee.transformer.bytecode.literal.LitNumberImpl;
import lucee.transformer.bytecode.literal.LitStringImpl;
import lucee.transformer.bytecode.statement.NativeSwitch;
import lucee.transformer.bytecode.statement.tag.TagCIObject;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.statement.tag.TagImport;
import lucee.transformer.bytecode.statement.tag.TagInterface;
import lucee.transformer.bytecode.statement.tag.TagProperty;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.bytecode.visitor.ConditionVisitor;
import lucee.transformer.bytecode.visitor.DecisionIntVisitor;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryCatchFinallyVisitor;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.statement.Argument;
import lucee.transformer.statement.HasBodies;
import lucee.transformer.statement.HasBody;
import lucee.transformer.statement.IFunction;
import lucee.transformer.statement.Statement;
import lucee.transformer.statement.tag.ATagThread;
import lucee.transformer.statement.tag.Attribute;
import lucee.transformer.statement.tag.Tag;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * represent a single Page
 */
public final class PageImpl extends BodyBase implements Page {

	private static final long MIN_AGE_TO_CLEAR = 5000l;

	// Maximum UDFs per constructor helper method to avoid JVM's 64KB method bytecode limit, chosen
	// value is a safe estimate, size depends on CFC methdod signatures
	private static final int MAX_UDF_PER_CONSTRUCTOR_METHOD = 30;

	// Maximum keys per <cinit> helper method to avoid JVM's 64KB method bytecode limit, each key
	// generates ~30 bytes
	private static final int MAX_KEYS_PER_CINIT_METHOD = 1000;

	public static final Type NULL = Type.getType(lucee.runtime.type.Null.class);
	public static final Type KEY_IMPL = Type.getType(KeyImpl.class);
	public static final Type KEY_CONSTANTS = Type.getType(KeyConstants.class);

	public static final Method KEY_INIT = new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING });

	public static final Method KEY_INIT_KEYS = new Method("initKeys", Types.COLLECTION_KEY, new Type[] { Types.STRING });
	public static final Method KEY_SOURCE = new Method("source", Types.COLLECTION_KEY, new Type[] { Types.STRING });

	// public static ImportDefintion getInstance(String fullname,ImportDefintion defaultValue)
	private static final Method ID_GET_INSTANCE = new Method("getInstance", Types.IMPORT_DEFINITIONS, new Type[] { Types.STRING, Types.IMPORT_DEFINITIONS });

	private static final Method CINIT = new Method("<clinit>", Types.VOID, new Type[] {});

	private static final Method CONSTRUCTOR = new Method("<init>", Types.VOID, new Type[] {});

	/*
	 * private static final Method CONSTRUCTOR_STR = new Method( "<init>", Types.VOID, new
	 * Type[]{Types.STRING}// );
	 */

	private static final Method CONSTRUCTOR_PS = new Method("<init>", Types.VOID, new Type[] { Types.PAGE_SOURCE });

	// public static final Type STRUCT_IMPL = Type.getType(StructImpl.class);
	public static final Method INIT_STRUCT_IMPL = new Method("<init>", Types.VOID, new Type[] {});

	// LDEV-3335: Flyweight UDF Type/Method constants
	private static final Type TYPE_MAP = Type.getType(Map.class);
	private static final Type TYPE_LINKED_HASH_MAP = Type.getType(LinkedHashMap.class);
	private static final Type TYPE_COLLECTION = Type.getType(Collection.class);
	private static final Type TYPE_ITERATOR = Type.getType(Iterator.class);
	private static final Type TYPE_UDF_GETTER_PROPERTY = Type.getType("Llucee/runtime/type/UDFGetterProperty;");
	private static final Type TYPE_UDF_SETTER_PROPERTY = Type.getType("Llucee/runtime/type/UDFSetterProperty;");
	private static final Method METHOD_MAP_PUT = new Method("put", Types.OBJECT, new Type[] { Types.OBJECT, Types.OBJECT });
	private static final Method METHOD_MAP_VALUES = new Method("values", TYPE_COLLECTION, new Type[] {});
	private static final Method METHOD_COLLECTION_ITERATOR = new Method("iterator", TYPE_ITERATOR, new Type[] {});
	private static final Method METHOD_ITERATOR_HAS_NEXT = new Method("hasNext", Type.BOOLEAN_TYPE, new Type[] {});
	private static final Method METHOD_ITERATOR_NEXT = new Method("next", Types.OBJECT, new Type[] {});
	private static final Method METHOD_PROPERTY_GET_GETTER = new Method("getGetter", Type.BOOLEAN_TYPE, new Type[] {});
	private static final Method METHOD_PROPERTY_GET_SETTER = new Method("getSetter", Type.BOOLEAN_TYPE, new Type[] {});
	private static final Method METHOD_PROPERTY_GET_GETTER_KEY = new Method("getGetterKey", Types.COLLECTION_KEY, new Type[] {});
	private static final Method METHOD_PROPERTY_GET_SETTER_KEY = new Method("getSetterKey", Types.COLLECTION_KEY, new Type[] {});
	private static final Method METHOD_UDF_CONSTRUCTOR = new Method("<init>", Type.VOID_TYPE, new Type[] { Types.COMPONENT, Types.PROPERTY });

	// void call (lucee.runtime.PageContext)
	private final static Method CALL1 = new Method("call", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });

	/*
	 * / void _try () private final static Method TRY = new Method( "_try", Types.VOID, new Type[]{} );
	 */

	// int getVersion()
	private final static Method VERSION = new Method("getVersion", Types.LONG_VALUE, new Type[] {});
	// void _init()

	private final static Method SET_PAGE_SOURCE = new Method("setPageSource", Types.VOID, new Type[] { Types.PAGE_SOURCE });

	// public ImportDefintion[] getImportDefintions()
	private final static Method GET_IMPORT_DEFINITIONS = new Method("getImportDefintions", Types.IMPORT_DEFINITIONS_ARRAY, new Type[] {});

	private final static Method GET_SUB_PAGES = new Method("getSubPages", Types.CI_PAGE_ARRAY, new Type[] {});

	// long getSourceLastModified()
	private final static Method LAST_MOD = new Method("getSourceLastModified", Types.LONG_VALUE, new Type[] {});
	private final static Method HAS_INIT = new Method("hasInit", Types.SHORT_VALUE, new Type[] {});

	private final static Method COMPILE_TIME = new Method("getCompileTime", Types.LONG_VALUE, new Type[] {});

	private final static Method HASH = new Method("getHash", Types.INT_VALUE, new Type[] {});

	private final static Method LENGTH = new Method("getSourceLength", Types.LONG_VALUE, new Type[] {});
	private final static Method GET_SUBNAME = new Method("getSubname", Types.STRING, new Type[] {});
	private final static Method GET_EXECUTABLE_LINES = new Method("getExecutableLines", Type.getType(Object[].class), new Type[] {});

	private static final Type USER_DEFINED_FUNCTION = Type.getType(UDF.class);
	private static final Method UDF_CALL = new Method("udfCall", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, USER_DEFINED_FUNCTION, Types.INT_VALUE });

	private static final Method THREAD_CALL = new Method("threadCall", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.INT_VALUE });

	private static final Method UDF_DEFAULT_VALUE = new Method("udfDefaultValue", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT, Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT });

	private static final Method NEW_COMPONENT_IMPL_INSTANCE = new Method("newInstance", Types.COMPONENT_IMPL,
			new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE });

	private static final Method NEW_INTERFACE_IMPL_INSTANCE = new Method("newInstance", Types.INTERFACE_IMPL, new Type[] { Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE });

	private static final Method STATIC_COMPONENT_CONSTR = new Method("staticConstructor", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_IMPL });

	// public StaticStruct getStaticStruct()
	private static final Method GET_STATIC_STRUCT = new Method("getStaticStruct", Types.STATIC_STRUCT, new Type[] {});

	// void init(PageContext pc,Component Impl c) throws PageException
	private static final Method INIT_COMPONENT3 = new Method("initComponent", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_IMPL, Types.BOOLEAN_VALUE });
	private static final Method INIT_INTERFACE = new Method("initInterface", Types.VOID, new Type[] { Types.INTERFACE_IMPL });

	// public boolean setMode(int mode) {
	private static final Method SET_MODE = new Method("setMode", Types.INT_VALUE, new Type[] { Types.INT_VALUE });

	private static final Method CONSTR_INTERFACE_IMPL8 = new Method("<init>", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.INTERFACE_PAGE_IMPL, Types.STRING, // extends
			Types.STRING, // hind
			Types.STRING, // display
			Types.STRING, // callpath
			Types.BOOLEAN_VALUE, // realpath
			Types.MAP // meta
	});
	private static final Method CONSTR_STATIC_STRUCT = new Method("<init>", Types.VOID, new Type[] {});

	// void init(PageContext pageContext,ComponentPage componentPage)
	private static final Method INIT_COMPONENT = new Method("init", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN_VALUE });

	private static final Method CHECK_INTERFACE = new Method("checkInterface", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL });

	// boolean getOutput()
	private static final Method GET_OUTPUT = new Method("getOutput", Types.BOOLEAN_VALUE, new Type[] {});

	private static final Method PUSH_BODY = new Method("pushBody", Types.OBJECT, new Type[] { Types.PAGE_CONTEXT });

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
	private static final Method BEFORE_STATIC_CONSTR = new Method("beforeStaticConstructor", Types.VARIABLES, new Type[] { Types.PAGE_CONTEXT });

	private static final org.objectweb.asm.commons.Method CONSTRUCTOR_EMPTY = new org.objectweb.asm.commons.Method("<init>", Types.VOID, new Type[] {});

	// Component Impl(ComponentPage,boolean, String, String, String, String) WS==With Style
	private static final Method CONSTR_COMPONENT_IMPL15 = new Method("<init>", Types.VOID,
			new Type[] { Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN_VALUE,
					Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRUCT_IMPL });
	private static final Method SET_EL = new Method("setEL", Types.OBJECT, new Type[] { Types.COLLECTION_KEY, Types.OBJECT });
	public static final Method UNDEFINED_SCOPE = new Method("us", Types.UNDEFINED, new Type[] {});
	private static final Method FLUSH_AND_POP = new Method("flushAndPop", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });
	private static final Method CLEAR_AND_POP = new Method("clearAndPop", Types.VOID, new Type[] { Types.PAGE_CONTEXT, Types.OBJECT });

	// Standard property attributes that are handled explicitly (not dynamic)
	private static final Set<String> STANDARD_PROPERTY_ATTRS = new HashSet<>(
			Arrays.asList("name", "type", "default", "access", "hint", "displayname", "required", "setter", "getter"));

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
	private ArrayList<ATagThread> threads = new ArrayList<ATagThread>();
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
	 * @param tc
	 * @param version
	 * @param lastModifed
	 * @param writeLog
	 * @param suppressWSbeforeArg
	 * @param output
	 * @param returnValue
	 * @param ignoreScopes
	 */
	public PageImpl(Factory factory, Config config, SourceCode sc, TagCIObject tc, long version, long lastModifed, boolean writeLog, boolean suppressWSbeforeArg, boolean output,
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
	 * @param className name of the genrated class (only necessary when Page object has no PageSource
	 *            reference)
	 * @return
	 * @throws TransformerException
	 */
	@Override
	public byte[] execute(String className) throws TransformerException {

		clearOldSubComponents(className);

		javaFunctions = null;// most likely not necessary
		// not exists in any case, so every usage must have a plan b for not existence
		PageSource optionalPS = sourceCode instanceof PageSourceCode ? ((PageSourceCode) sourceCode).getPageSource() : null;

		Map<LitString, Integer> keys = new LinkedHashMap<LitString, Integer>();
		ClassWriter cw = ASMUtil.getClassWriter();

		ArrayList<String> imports = new ArrayList<String>();
		getImports(imports, this);

		// look for component if necessary
		TagCIObject comp = getTagCFObject(null);

		// get class name

		if (!StringUtil.isEmpty(className)) {
			className = className.replace('.', '/');
			this.className = className;
		}
		else {
			className = getClassName();
		}

		boolean isSub = comp != null && !comp.isMain();

		// parent
		String parent = lucee.runtime.Page.class.getName();// "lucee/runtime/Page";
		String[] interfaces = null;
		if (isComponent(comp)) {
			parent = ComponentPageImpl.class.getName();// "lucee/runtime/ComponentPage";
			if (isSub) interfaces = new String[] { SubPage.class.getName().replace('.', '/') };
		}
		else if (isInterface(comp)) parent = InterfacePageImpl.class.getName();// "lucee/runtime/InterfacePage";
		parent = parent.replace('.', '/');

		cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, className, null, parent, interfaces);
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

		// constructor
		GeneratorAdapter constrAdapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_PS, null, null, cw);
		ConstrBytecodeContext constr = new ConstrBytecodeContext(config, optionalPS, this, keys, cw, className, constrAdapter, CONSTRUCTOR_PS, writeLog(), suppressWSbeforeArg,
				output, returnValue, sourceCode.getSourceOffset());
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
			t = Types.PAGE;
			constrAdapter.invokeConstructor(t, CONSTRUCTOR);
		}

		// call _init()
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);

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

		if (comp != null) {
			writeOutStaticConstructor(constr, keys, cw, comp, className);
		}

		List<Function> tmpFunctions = getFunctions();
		if (false && _comp instanceof TagComponent) {
			TagComponent tc;
			List<Function> tmps = new ArrayList<>();
			for (Function f: tmpFunctions) {
				tc = ASMUtil.getAncestorComponent(f);
				// function from another component
				if (tc != null && tc != _comp) {
					continue;
				}
				tmps.add(f);
			}
			tmpFunctions = tmps;
		}

		// hasInit
		boolean hasInit = false;
		if (isComponent(comp)) {
			ExprString name;
			for (Function f: tmpFunctions) {
				name = f.getName();
				if (!(name instanceof Literal)) {
					hasInit = true;
					break;
				}
				if ("init".equalsIgnoreCase(((Literal) name).getString())) {
					hasInit = true;
					break;
				}

			}
		}

		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, HAS_INIT, null, null, cw);
		adapter.push(hasInit ? ComponentUtil.HAS_INIT_TRUE : ComponentUtil.HAS_INIT_FALSE);
		adapter.returnValue();
		adapter.endMethod();

		Function[] functions = tmpFunctions.toArray(new Function[tmpFunctions.size()]);

		List<IFunction> funcs;
		// newInstance/initComponent/call
		if (isComponent()) {
			// writeOutGetStaticStructX(constr, keys, cw, comp, className);
			writeOutNewComponent(constr, keys, cw, comp, className);
			funcs = writeOutInitComponent(constr, functions, keys, cw, comp, className);

		}
		else if (isInterface()) {
			// writeOutGetStaticStructX(constr, keys, cw, comp, className);
			writeOutNewInterface(constr, keys, cw, comp, className);
			funcs = writeOutInitInterface(constr, keys, cw, comp, className);
		}
		else {
			funcs = writeOutCall(constr, keys, cw, className);
		}

		// write UDFProperties to constructor
		// writeUDFProperties(bc,funcs,pageType);

		// udfCall

		ConditionVisitor cv;
		DecisionIntVisitor div;
		// Function[] functions = extractFunctions(constr.getUDFProperties());
		// less/equal than 10 functions
		if (isInterface()) {}
		else if (functions.length <= 10) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, new Type[] { Types.THROWABLE }, cw);
			BytecodeContext bc = new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue,
					sourceCode.getSourceOffset());

			if (functions.length == 0) {}
			else if (functions.length == 1) {
				bc.visitLine(functions[0].getStart());
				functions[0].getBody().writeOut(bc);
				bc.visitLine(functions[0].getEnd());
			}
			else writeOutUdfCallInner(bc, functions, 0, functions.length);
			adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();
		}
		// more than 10 functions
		else {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, new Type[] { Types.THROWABLE }, cw);
			BytecodeContext bc = new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue,
					sourceCode.getSourceOffset());
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
				writeOutUdfCallInner(new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, innerCall, writeLog(), suppressWSbeforeArg, output,
						returnValue, sourceCode.getSourceOffset()), functions, i, i + 10 > functions.length ? functions.length : i + 10);

				adapter.visitInsn(Opcodes.ACONST_NULL);
				adapter.returnValue();
				adapter.endMethod();
			}
		}

		// threadCall
		ATagThread[] threads = getThreads();
		if (true) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, THREAD_CALL, null, new Type[] { Types.THROWABLE }, cw);
			if (threads.length > 0) writeOutThreadCallInner(new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, THREAD_CALL, writeLog(),
					suppressWSbeforeArg, output, returnValue, sourceCode.getSourceOffset()), threads, 0, threads.length);
			// adapter.visitInsn(Opcodes.ACONST_NULL);
			adapter.returnValue();
			adapter.endMethod();
		}

		// udfDefaultValue
		// less/equal than 10 functions
		if (isInterface()) {}
		else if (functions.length <= 10) {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
			if (functions.length > 0) writeUdfDefaultValueInner(new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(),
					suppressWSbeforeArg, output, returnValue, sourceCode.getSourceOffset()), functions, 0, functions.length);

			adapter.loadArg(DEFAULT_VALUE);
			adapter.returnValue();
			adapter.endMethod();
		}
		else {
			adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
			BytecodeContext bc = new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(), suppressWSbeforeArg, output,
					returnValue, sourceCode.getSourceOffset());
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
				writeUdfDefaultValueInner(new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, adapter, innerDefaultValue, writeLog(), suppressWSbeforeArg,
						output, returnValue, sourceCode.getSourceOffset()), functions, i, i + 10 > functions.length ? functions.length : i + 10);

				adapter.loadArg(DEFAULT_VALUE);
				// adapter.visitInsn(Opcodes.ACONST_NULL);
				adapter.returnValue();
				adapter.endMethod();
			}

		}

		// CONSTRUCTOR
		List<Data> udfProperties = constr.getUDFProperties();
		String udfpropsClassName = Types.UDF_PROPERTIES_ARRAY.toString();

		// new UDFProperties Array
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		constrAdapter.push(functions.length);// MUST6 ATM the array is to big, it has empty spaces for every closure, this is not necessary
		constrAdapter.newArray(Types.UDF_PROPERTIES);
		constrAdapter.visitFieldInsn(Opcodes.PUTFIELD, getClassName(), "udfs", udfpropsClassName);

		// set item — split into helper methods if there are enough functions to risk
		// MethodTooLargeException (LDEV-6126)
		if (functions.length > MAX_UDF_PER_CONSTRUCTOR_METHOD) {
			int batchNum = 0;
			for (int batchStart = 0; batchStart < functions.length; batchStart += MAX_UDF_PER_CONSTRUCTOR_METHOD) {
				int batchEnd = Math.min(batchStart + MAX_UDF_PER_CONSTRUCTOR_METHOD, functions.length);
				String helperMethodName = ASMUtil.createOverfowMethod("_constrUdfs", batchNum++);
				Method helperMethod = new Method(helperMethodName, Types.VOID, new Type[] { Types.PAGE_SOURCE });
				GeneratorAdapter helperAdapter = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, helperMethod, null, new Type[] { Types.THROWABLE }, cw);
				BytecodeContext helperBc = new BytecodeContext(config, optionalPS, constr, this, keys, cw, className, helperAdapter, helperMethod, writeLog(), suppressWSbeforeArg,
						output, returnValue, sourceCode.getSourceOffset());

				// call helper from constructor
				constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
				constrAdapter.visitVarInsn(Opcodes.ALOAD, 1);
				constrAdapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, helperMethodName, "(Llucee/runtime/PageSource;)V");

				// write batch into helper method
				writeUdfProperties(helperBc, functions, udfProperties, className, udfpropsClassName, batchStart, batchEnd);

				helperAdapter.returnValue();
				helperAdapter.endMethod();
			}
		}
		else {
			writeUdfProperties(constr, functions, udfProperties, className, udfpropsClassName, 0, functions.length);
		}

		// setPageSource(pageSource);
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		constrAdapter.visitVarInsn(Opcodes.ALOAD, 1);
		constrAdapter.invokeVirtual(t, SET_PAGE_SOURCE);

		constrAdapter.returnValue();
		constrAdapter.endMethod();

		// newInstance/initComponent/call
		writeOutStatic(optionalPS, constr, keys, cw, comp, className);

		// set field subs
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "subs", "[Llucee/runtime/CIPage;", null, null);
		fv.visitEnd();

		// create sub components/interfaces
		if (comp != null && comp.isMain()) {
			List<TagCIObject> subs = getSubs(null);
			if (!ArrayUtil.isEmpty(subs)) {
				Iterator<TagCIObject> _it = subs.iterator();
				TagCIObject tc;
				while (_it.hasNext()) {
					tc = _it.next();

					tc.writeOut(constr, this);
				}
				writeGetSubPages(cw, className, subs);
			}
		}

		// getExecutableLines - only generated in debug mode for debugger support
		if (writeLog()) {
			GeneratorAdapter execLinesAdapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_EXECUTABLE_LINES, null, null, cw);

			// Create Object[2] array
			execLinesAdapter.push(2);
			execLinesAdapter.newArray(Types.OBJECT);

			// [0] = compileTime (Long) - call this.getCompileTime() for cache invalidation
			execLinesAdapter.dup();
			execLinesAdapter.push(0);
			execLinesAdapter.loadThis();
			execLinesAdapter.invokeVirtual(Type.getObjectType(className), COMPILE_TIME);
			execLinesAdapter.invokeStatic(Type.getType(Long.class), new Method("valueOf", Type.getType(Long.class), new Type[] { Type.LONG_TYPE }));
			execLinesAdapter.arrayStore(Types.OBJECT);

			// [1] = lines (int[] or null)
			execLinesAdapter.dup();
			execLinesAdapter.push(1);
			int[] execLines = constr.getExecutableLines();
			String encoded = PageContextUtil.encodeExecutableLines(execLines);
			int maxLine = PageContextUtil.getMaxLine(execLines);
			if (encoded == null) {
				execLinesAdapter.visitInsn(Opcodes.ACONST_NULL);
			}
			else {
				// Call PageContextUtil.decodeExecutableLines(encoded, maxLine)
				execLinesAdapter.push(encoded);
				execLinesAdapter.push(maxLine);
				execLinesAdapter.invokeStatic(Types.PAGE_CONTEXT_UTIL, new Method("decodeExecutableLines", Type.getType(int[].class), new Type[] { Types.STRING, Type.INT_TYPE }));
			}
			execLinesAdapter.arrayStore(Types.OBJECT);

			execLinesAdapter.returnValue();
			execLinesAdapter.endMethod();
		}

		return ASMUtil.verify(cw.toByteArray());
	}

	private void clearOldSubComponents(final String className) {
		if (!className.endsWith(Constants.CFML_CLASS_SUFFIX)) return;

		if (sourceCode instanceof PageSourceCode) {
			final String subComName = className.substring(0, className.length() - Constants.CFML_CLASS_SUFFIX.length()) + "$" + Constants.SUB_COMPONENT_APPENDIX;
			long now = System.currentTimeMillis();
			//
			PageSourceCode psc = (PageSourceCode) sourceCode;
			Resource classFile = psc.getPageSource().getMapping().getClassRootDirectory().getRealResource(className + ".class");
			if (classFile.isFile()) {
				classFile.getParentResource().listResources(new ResourceNameFilter() {

					@Override
					public boolean accept(Resource parent, String name) {
						if (!name.startsWith(subComName)) return false;
						Resource r = parent.getRealResource(name);
						if (r.lastModified() + MIN_AGE_TO_CLEAR < now) {
							r.delete();
						}
						return false;
					}
				});
			}
		}
	}

	private void writeUdfProperties(BytecodeContext bc, Function[] functions, List<Data> udfProperties, String className, String udfpropsClassName, int from, int to)
			throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		for (int i = from; i < to; i++) {
			Data data = getMatchingData(functions[i], udfProperties);
			if (data == null) continue;

			adapter.visitVarInsn(Opcodes.ALOAD, 0);
			adapter.visitFieldInsn(Opcodes.GETFIELD, className, "udfs", udfpropsClassName);
			adapter.push(i);
			data.function.createUDFProperties(bc, i, data.type);
			adapter.visitInsn(Opcodes.AASTORE);
		}
	}

	private Data getMatchingData(Function func, List<Data> datas) {
		for (Data d: datas) {
			if (d.function == func) return d;
		}
		return null;
	}

	/*
	 * private static Function[] extractFunctions(List<Data> udfProperties) { Function[] functions = new
	 * Function[udfProperties.size()]; int index = 0; for (Data d: udfProperties) { functions[index++] =
	 * d.function; } return functions; }
	 */

	public static String createSubClass(String name, String subName) {
		// TODO handle special characters
		if (!StringUtil.isEmpty(subName)) {
			String suffix = (Constants.CFML_CLASS_SUFFIX);
			subName = subName.toLowerCase();
			if (name.endsWith(suffix)) name = name.substring(0, name.length() - 3) + "$" + subName + suffix;
			else name += "$" + subName;
		}
		return name;
	}

	private void writeGetSubPages(ClassWriter cw, String name, List<TagCIObject> subs) {
		// pageSource.getFullClassName().replace('.', '/');
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_SUB_PAGES, null, null, cw);
		Label endIF = new Label();

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, name, "subs", "[Llucee/runtime/CIPage;");
		adapter.visitJumpInsn(Opcodes.IFNONNULL, endIF);

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.CI_PAGE, subs.size());
		Iterator<TagCIObject> it = subs.iterator();
		String className;
		int index = 0;
		while (it.hasNext()) {
			TagCIObject ci = it.next();
			av.visitBeginItem(adapter, index++);
			className = createSubClass(name, ci.getName());

			adapter.visitVarInsn(Opcodes.ALOAD, 0);
			adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name, "getPageSource", "()Llucee/runtime/PageSource;");
			adapter.push(className);
			adapter.visitMethodInsn(Opcodes.INVOKESTATIC, "lucee/runtime/MappingImpl", "loadCIPage", "(Llucee/runtime/PageSource;Ljava/lang/String;)Llucee/runtime/CIPage;");

			av.visitEndItem(adapter);
		}
		av.visitEnd();

		adapter.visitFieldInsn(Opcodes.PUTFIELD, name, "subs", "[Llucee/runtime/CIPage;");

		adapter.visitLabel(endIF);

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
					className = createSubClass(className, comp.getName());
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
	private TagCIObject getTagCFObject(TagCIObject defaultValue) {
		if (_comp != null) return _comp; // return sub component

		// look for main
		Iterator<Statement> it = getStatements().iterator();
		Statement s;
		TagCIObject t, sub = null;

		while (it.hasNext()) {
			s = it.next();
			if (s instanceof TagCIObject) {
				t = (TagCIObject) s;
				if (t.isMain()) return _comp = t;
				// else if (sub == null) sub = t;
			}
		}
		// if (sub != null) return _comp = sub;
		return defaultValue;
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
				if (!t.isMain()) {
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

	public static void registerFields(BytecodeContext bc, Map<LitString, Integer> keys) throws TransformerException {
		// if(keys.size()==0) return;
		GeneratorAdapter ga = bc.getAdapter();

		FieldVisitor fv = bc.getClassWriter().visitField(Opcodes.ACC_PRIVATE, "keys", Types.COLLECTION_KEY_ARRAY.toString(), null, null);
		fv.visitEnd();

		int index = 0;
		LitString value;
		Iterator<LitString> it = keys.keySet().iterator();
		ga.visitVarInsn(Opcodes.ALOAD, 0);
		ga.push(keys.size());
		ga.newArray(Types.COLLECTION_KEY);
		while (it.hasNext()) {
			value = it.next();
			ga.dup();
			ga.push(index++);
			// value.setExternalize(false);
			ExpressionUtil.writeOutSilent(value, bc, Expression.MODE_REF);
			ga.invokeStatic(KEY_IMPL, KEY_INIT_KEYS);
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

	private void writeOutUdfCallInner(BytecodeContext bc, Function[] functions, int offset, int length) throws TransformerException {
		NativeSwitch ns = new NativeSwitch(bc.getFactory(), 2, NativeSwitch.ARG_REF, null, null);

		for (int i = offset; i < length; i++) {
			ns.addCase(i, functions[i].getBody(), functions[i].getStart(), functions[i].getEnd(), true);
		}
		ns._writeOut(bc);
	}

	private void writeOutThreadCallInner(BytecodeContext bc, ATagThread[] threads, int offset, int length) throws TransformerException {
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

	private void writeOutStatic(PageSource optionalPS, ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, TagCIObject component, String name) {

		boolean addStatic = isComponent() || isInterface();

		if (addStatic) {
			cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "staticStruct", "Llucee/runtime/component/StaticStruct;", null, null).visitEnd();
			// Generate per-class static property registry field
			cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "__staticProperties", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Llucee/runtime/component/PropertyImpl;>;", null).visitEnd();
			// LDEV-3335: Generate flyweight accessor UDF registry field
			cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "__staticAccessorUDFs", "Ljava/util/Map;",
					"Ljava/util/Map<Llucee/runtime/type/Collection$Key;Llucee/runtime/type/UDF;>;", null).visitEnd();
		}

		cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "keys", Types.COLLECTION_KEY_ARRAY.toString(), null, null).visitEnd();

		{
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_STATIC, CINIT, null, null, cw);

			if (addStatic) {
				ga.newInstance(Types.STATIC_STRUCT);
				ga.dup();
				ga.invokeConstructor(Types.STATIC_STRUCT, CONSTR_STATIC_STRUCT);
				ga.putStatic(Type.getObjectType(name), "staticStruct", Types.STATIC_STRUCT);

				// Initialize __staticProperties = new LinkedHashMap<>()
				ga.newInstance(Type.getType(LinkedHashMap.class));
				ga.dup();
				ga.invokeConstructor(Type.getType(LinkedHashMap.class), new Method("<init>", Type.VOID_TYPE, new Type[] {}));
				ga.putStatic(Type.getObjectType(name), "__staticProperties", Type.getType(Map.class));

				// LDEV-3335: Initialize __staticAccessorUDFs = new LinkedHashMap<>()
				ga.newInstance(TYPE_LINKED_HASH_MAP);
				ga.dup();
				ga.invokeConstructor(TYPE_LINKED_HASH_MAP, new Method("<init>", Type.VOID_TYPE, new Type[] {}));
				ga.putStatic(Type.getObjectType(name), "__staticAccessorUDFs", TYPE_MAP);
			}

			/////////////////
			// Register static properties for components
			// IMPORTANT: Do this BEFORE creating keys array so all keys are registered first
			if (addStatic && component != null && component.getBody() != null) {
				List<Statement> statements = component.getBody().getStatements();
				if (statements != null) {
					for (Statement stmt: statements) {
						if (stmt instanceof TagProperty) {
							TagProperty tagProp = (TagProperty) stmt;
							Tag tag = tagProp;

							// Extract property attributes
							String propName = getTagAttributeValue(tag, "name");
							String propType = getTagAttributeValue(tag, "type");
							String propAccess = getTagAttributeValue(tag, "access");
							String propHint = getTagAttributeValue(tag, "hint");
							String propDisplayname = getTagAttributeValue(tag, "displayname");
							String propRequired = getTagAttributeValue(tag, "required");
							String propSetter = getTagAttributeValue(tag, "setter");
							String propGetter = getTagAttributeValue(tag, "getter");
							Attribute propDefaultAttr = tag.getAttribute("default");

							if (propName != null) {
								// Generate: PropertyImpl prop = new PropertyImpl();
								ga.newInstance(Types.PROPERTY_IMPL);
								ga.dup();
								ga.invokeConstructor(Types.PROPERTY_IMPL, new Method("<init>", Type.VOID_TYPE, new Type[] {}));
								int propLocal = ga.newLocal(Types.PROPERTY_IMPL);
								ga.storeLocal(propLocal);

								// prop.setName("propName");
								ga.loadLocal(propLocal);
								ga.push(propName);
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setName", Type.VOID_TYPE, new Type[] { Types.STRING }));

								// prop.setNameAsKey(KeyImpl.init(propName)) - cache the key at class-load time
								ga.loadLocal(propLocal);
								ga.push(propName);
								ga.invokeStatic(Type.getType("Llucee/runtime/type/KeyImpl;"), new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING }));
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setNameAsKey", Type.VOID_TYPE, new Type[] { Types.COLLECTION_KEY }));

								// prop.setGetterKey(KeyImpl.init("get" + propName)) - cache getter key
								ga.loadLocal(propLocal);
								ga.push("get" + propName);
								ga.invokeStatic(Type.getType("Llucee/runtime/type/KeyImpl;"), new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING }));
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setGetterKey", Type.VOID_TYPE, new Type[] { Types.COLLECTION_KEY }));

								// prop.setSetterKey(KeyImpl.init("set" + propName)) - cache setter key
								ga.loadLocal(propLocal);
								ga.push("set" + propName);
								ga.invokeStatic(Type.getType("Llucee/runtime/type/KeyImpl;"), new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING }));
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setSetterKey", Type.VOID_TYPE, new Type[] { Types.COLLECTION_KEY }));

								// prop.setType("type") if provided
								if (propType != null) {
									ga.loadLocal(propLocal);
									ga.push(propType);
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setType", Type.VOID_TYPE, new Type[] { Types.STRING }));
								}

								// prop.setAccess("access") if provided
								if (propAccess != null) {
									ga.loadLocal(propLocal);
									ga.push(propAccess);
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setAccess", Type.VOID_TYPE, new Type[] { Types.STRING }));
								}

								// prop.setHint("hint") if provided
								if (propHint != null) {
									ga.loadLocal(propLocal);
									ga.push(propHint);
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setHint", Type.VOID_TYPE, new Type[] { Types.STRING }));
								}

								// prop.setDisplayname("displayname") if provided
								if (propDisplayname != null) {
									ga.loadLocal(propLocal);
									ga.push(propDisplayname);
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setDisplayname", Type.VOID_TYPE, new Type[] { Types.STRING }));
								}

								// prop.setRequired(boolean) only if explicitly provided
								if (propRequired != null) {
									ga.loadLocal(propLocal);
									ga.push("true".equalsIgnoreCase(propRequired) || "yes".equalsIgnoreCase(propRequired));
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setRequired", Type.VOID_TYPE, new Type[] { Type.BOOLEAN_TYPE }));
								}

								// prop.setSetter(boolean)
								boolean setter = propSetter == null || "true".equalsIgnoreCase(propSetter) || "yes".equalsIgnoreCase(propSetter);
								ga.loadLocal(propLocal);
								ga.push(setter);
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setSetter", Type.VOID_TYPE, new Type[] { Type.BOOLEAN_TYPE }));

								// prop.setGetter(boolean)
								boolean getter = propGetter == null || "true".equalsIgnoreCase(propGetter) || "yes".equalsIgnoreCase(propGetter);
								ga.loadLocal(propLocal);
								ga.push(getter);
								ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setGetter", Type.VOID_TYPE, new Type[] { Type.BOOLEAN_TYPE }));

								// prop.setDefault(value) if it's a simple literal
								// Only handle simple literals (Literal interface) - complex expressions like now()
								// or #myVar# need PageContext and must be evaluated at runtime, not class-load time
								if (propDefaultAttr != null && propDefaultAttr.getValue() instanceof Literal) {
									Expression defaultExpr = propDefaultAttr.getValue();

									// Handle simple literals only - complex expressions handled at runtime
									if (defaultExpr instanceof LitStringImpl) {
										String value = ((LitStringImpl) defaultExpr).getString();
										ga.loadLocal(propLocal);
										ga.push(value);
										ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setDefault", Type.VOID_TYPE, new Type[] { Types.OBJECT }));
									}
									else if (defaultExpr instanceof LitNumberImpl) {
										Number value = ((LitNumberImpl) defaultExpr).getNumber();
										ga.loadLocal(propLocal);
										ga.push(value.doubleValue());
										ga.box(Type.DOUBLE_TYPE);
										ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setDefault", Type.VOID_TYPE, new Type[] { Types.OBJECT }));
									}
									else if (defaultExpr instanceof LitBooleanImpl) {
										Boolean value = ((LitBooleanImpl) defaultExpr).getBoolean();
										ga.loadLocal(propLocal);
										ga.push(value.booleanValue());
										ga.box(Type.BOOLEAN_TYPE);
										ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setDefault", Type.VOID_TYPE, new Type[] { Types.OBJECT }));
									}
									// else: complex expression - will be handled at runtime in TagProperty
								}

								// Collect dynamic attributes (non-standard attributes)
								Map<String, Attribute> allAttrs = tag.getAttributes();
								List<Attribute> dynamicAttrs = new ArrayList<>();
								for (Attribute attr: allAttrs.values()) {
									String attrName = attr.getName().toLowerCase();
									// Skip standard attributes
									if (!STANDARD_PROPERTY_ATTRS.contains(attrName)) {
										dynamicAttrs.add(attr);
									}
								}

								// If there are dynamic attributes OR explicit required, create metadata struct and set it
								if (!dynamicAttrs.isEmpty() || propRequired != null) {
									// Calculate exact size needed: dynamic attrs + required (if set)
									int dynAttrCount = dynamicAttrs.size() + (propRequired != null ? 1 : 0);

									// Generate: Struct dynAttrs = new StructImpl(TYPE_REGULAR, size);
									ga.newInstance(Types.STRUCT_IMPL);
									ga.dup();
									ga.getStatic(Types.STRUCT_IMPL, "TYPE_REGULAR", Type.INT_TYPE);
									ga.push(dynAttrCount);
									ga.invokeConstructor(Types.STRUCT_IMPL, new Method("<init>", Type.VOID_TYPE, new Type[] { Type.INT_TYPE, Type.INT_TYPE }));
									int dynAttrsLocal = ga.newLocal(Types.STRUCT);
									ga.storeLocal(dynAttrsLocal);

									// For each dynamic attribute: dynAttrs.setEL(key, value)
									for (Attribute dynAttr: dynamicAttrs) {
										String dynAttrValue = getTagAttributeValue(tag, dynAttr.getName());
										if (dynAttrValue != null) {
											ga.loadLocal(dynAttrsLocal);

											// Create key at class-load time
											ga.push(dynAttr.getName());
											ga.invokeStatic(Type.getType("Llucee/runtime/type/KeyImpl;"), new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING }));

											ga.push(dynAttrValue);
											ga.invokeInterface(Types.STRUCT, SET_EL);
											ga.pop(); // Pop return value
										}
									}

									// Add required to dynamic attributes if explicitly set
									if (propRequired != null) {
										ga.loadLocal(dynAttrsLocal);

										// Create "required" key at class-load time
										ga.push("required");
										ga.invokeStatic(Type.getType("Llucee/runtime/type/KeyImpl;"), new Method("init", Types.COLLECTION_KEY, new Type[] { Types.STRING }));

										ga.push("true".equalsIgnoreCase(propRequired) || "yes".equalsIgnoreCase(propRequired) ? "yes" : "no");
										ga.invokeInterface(Types.STRUCT, SET_EL);
										ga.pop(); // Pop return value
									}

									// prop.setDynamicAttributes(dynAttrs);
									ga.loadLocal(propLocal);
									ga.loadLocal(dynAttrsLocal);
									ga.invokeVirtual(Types.PROPERTY_IMPL, new Method("setDynamicAttributes", Type.VOID_TYPE, new Type[] { Types.STRUCT }));
								}

								// __staticProperties.put(propName.toLowerCase(), prop);
								ga.getStatic(Type.getObjectType(name), "__staticProperties", Type.getType(Map.class));
								ga.push(propName.toLowerCase());
								ga.loadLocal(propLocal);
								ga.invokeInterface(Type.getType(Map.class),
										new Method("put", Type.getType(Object.class), new Type[] { Type.getType(Object.class), Type.getType(Object.class) }));
								ga.pop(); // Pop return value from map.put()
							}
						}
					}
				}
			}

			// LDEV-3335: Generate flyweight accessor UDFs from static properties
			// Iterate over __staticProperties.values() and create getter/setter UDFs
			if (addStatic && component != null) {
				// for (Property prop : __staticProperties.values())
				ga.getStatic(Type.getObjectType(name), "__staticProperties", TYPE_MAP);
				ga.invokeInterface(TYPE_MAP, METHOD_MAP_VALUES);
				ga.invokeInterface(TYPE_COLLECTION, METHOD_COLLECTION_ITERATOR);
				int iteratorLocal = ga.newLocal(TYPE_ITERATOR);
				ga.storeLocal(iteratorLocal);

				Label loopStart = ga.newLabel();
				Label loopEnd = ga.newLabel();

				ga.mark(loopStart);
				ga.loadLocal(iteratorLocal);
				ga.invokeInterface(TYPE_ITERATOR, METHOD_ITERATOR_HAS_NEXT);
				ga.visitJumpInsn(Opcodes.IFEQ, loopEnd);

				ga.loadLocal(iteratorLocal);
				ga.invokeInterface(TYPE_ITERATOR, METHOD_ITERATOR_NEXT);
				ga.checkCast(Types.PROPERTY_IMPL);
				int propLocal = ga.newLocal(Types.PROPERTY_IMPL);
				ga.storeLocal(propLocal);

				// if (prop.getGetter())
				Label skipGetter = ga.newLabel();
				ga.loadLocal(propLocal);
				ga.invokeVirtual(Types.PROPERTY_IMPL, METHOD_PROPERTY_GET_GETTER);
				ga.visitJumpInsn(Opcodes.IFEQ, skipGetter);

				// __staticAccessorUDFs.put(prop.getGetterKey(), new UDFGetterProperty(null, prop))
				ga.getStatic(Type.getObjectType(name), "__staticAccessorUDFs", TYPE_MAP);
				ga.loadLocal(propLocal);
				ga.invokeVirtual(Types.PROPERTY_IMPL, METHOD_PROPERTY_GET_GETTER_KEY);
				ga.newInstance(TYPE_UDF_GETTER_PROPERTY);
				ga.dup();
				ga.visitInsn(Opcodes.ACONST_NULL); // null component for flyweight
				ga.loadLocal(propLocal);
				ga.invokeConstructor(TYPE_UDF_GETTER_PROPERTY, METHOD_UDF_CONSTRUCTOR);
				ga.invokeInterface(TYPE_MAP, METHOD_MAP_PUT);
				ga.pop();

				ga.mark(skipGetter);

				// if (prop.getSetter())
				Label skipSetter = ga.newLabel();
				ga.loadLocal(propLocal);
				ga.invokeVirtual(Types.PROPERTY_IMPL, METHOD_PROPERTY_GET_SETTER);
				ga.visitJumpInsn(Opcodes.IFEQ, skipSetter);

				// __staticAccessorUDFs.put(prop.getSetterKey(), new UDFSetterProperty(null, prop))
				ga.getStatic(Type.getObjectType(name), "__staticAccessorUDFs", TYPE_MAP);
				ga.loadLocal(propLocal);
				ga.invokeVirtual(Types.PROPERTY_IMPL, METHOD_PROPERTY_GET_SETTER_KEY);
				ga.newInstance(TYPE_UDF_SETTER_PROPERTY);
				ga.dup();
				ga.visitInsn(Opcodes.ACONST_NULL); // null component
				ga.loadLocal(propLocal);
				ga.invokeConstructor(TYPE_UDF_SETTER_PROPERTY, METHOD_UDF_CONSTRUCTOR);
				ga.invokeInterface(TYPE_MAP, METHOD_MAP_PUT);
				ga.pop();

				ga.mark(skipSetter);

				ga.goTo(loopStart);
				ga.mark(loopEnd);
			}
			// Array initialization - MUST be done AFTER property processing so all keys are registered
			ga.push(keys.size()); // Array size
			ga.newArray(Types.COLLECTION_KEY);

			// Split into helper methods if there are enough keys to risk MethodTooLargeException (LDEV-6134)
			List<LitString> keyList = new ArrayList<>(keys.keySet());

			if (keyList.size() > MAX_KEYS_PER_CINIT_METHOD) {
				int batchNum = 0;
				for (int batchStart = 0; batchStart < keyList.size(); batchStart += MAX_KEYS_PER_CINIT_METHOD) {
					int batchEnd = Math.min(batchStart + MAX_KEYS_PER_CINIT_METHOD, keyList.size());
					String helperMethodName = ASMUtil.createOverfowMethod("_cinitKeys", batchNum++);
					Method helperMethod = new Method(helperMethodName, Type.VOID_TYPE, new Type[] { Types.COLLECTION_KEY_ARRAY });
					GeneratorAdapter helperAdapter = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC, helperMethod, null, null, cw);

					// call helper from <cinit>
					ga.dup();
					ga.invokeStatic(Type.getObjectType(name), helperMethod);

					// write batch into helper method
					writeKeysBatch(helperAdapter, keyList, batchStart, batchEnd);

					helperAdapter.returnValue();
					helperAdapter.endMethod();
				}
			}
			else {
				// Small number of keys, inline directly in <cinit>
				int index = 0;
				for (LitString ls: keyList) {
					ga.dup();
					ga.push(index++);
					ga.push(ls.getString());

					// ExpressionUtil.writeOutSilent(ls, bc, Expression.MODE_REF);
					ga.invokeStatic(KEY_IMPL, KEY_INIT_KEYS);
					ga.arrayStore(Types.COLLECTION_KEY);
				}
			}

			ga.putStatic(Type.getObjectType(name), "keys", Types.COLLECTION_KEY_ARRAY);

			ga.returnValue();
			ga.endMethod();

		}

		// public StaticStruct getStaticStruct() {return _static;}
		{
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_STATIC_STRUCT, null, null, cw);
			ga.getStatic(Type.getObjectType(name), "staticStruct", Types.STATIC_STRUCT);
			ga.returnValue();
			ga.endMethod();
		}

		// Generate: public Map getStaticProperties() { return __staticProperties; }
		// Override ComponentPageImpl.getStaticProperties() to return the static property map
		if (addStatic) {
			Method getStaticPropsMethod = new Method("getStaticProperties", Type.getType(Map.class), new Type[] {});
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, getStaticPropsMethod, null, null, cw);
			ga.getStatic(Type.getObjectType(name), "__staticProperties", Type.getType(Map.class));
			ga.returnValue();
			ga.endMethod();
		}

		// LDEV-3335: Generate: public Map getStaticAccessorUDFs() { return __staticAccessorUDFs; }
		// Override ComponentPageImpl.getStaticAccessorUDFs() to return the flyweight UDF map
		if (addStatic) {
			Method getStaticAccessorUDFsMethod = new Method("getStaticAccessorUDFs", TYPE_MAP, new Type[] {});
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, getStaticAccessorUDFsMethod, null, null, cw);
			ga.getStatic(Type.getObjectType(name), "__staticAccessorUDFs", TYPE_MAP);
			ga.returnValue();
			ga.endMethod();
		}

		// Generate: public void initPropertiesStub(ComponentImpl impl) throws PageException
		// Optimized property initialization that directly accesses static __staticProperties field
		if (addStatic && component != null) {
			Method initPropsStubMethod = new Method("initPropertiesStub", Type.VOID_TYPE, new Type[] { Types.COMPONENT_IMPL });
			final GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, initPropsStubMethod, null, new Type[] { Types.PAGE_EXCEPTION }, cw);

			// if (__staticProperties != null && !__staticProperties.isEmpty()) {
			Label afterNullCheck = ga.newLabel();
			Label continueProcessing = ga.newLabel();

			// Get __staticProperties and check for null
			ga.getStatic(Type.getObjectType(name), "__staticProperties", Type.getType(Map.class));
			ga.dup(); // Duplicate for null check
			Label notNull = ga.newLabel();
			ga.visitJumpInsn(Opcodes.IFNONNULL, notNull); // If not null, continue
			// If null: pop the dup and jump to end
			ga.pop();
			ga.goTo(afterNullCheck);

			// Not null: check if empty
			ga.mark(notNull);
			ga.invokeInterface(Type.getType(Map.class), new Method("isEmpty", Type.BOOLEAN_TYPE, new Type[] {}));
			ga.visitJumpInsn(Opcodes.IFEQ, continueProcessing); // IFEQ = if equal to zero (if false/not empty)
			// If empty: jump to end
			ga.goTo(afterNullCheck);

			// Continue processing: Get __staticProperties again for iteration
			ga.mark(continueProcessing);
			ga.getStatic(Type.getObjectType(name), "__staticProperties", Type.getType(Map.class));
			ga.invokeInterface(Type.getType(Map.class), new Method("values", Type.getType(java.util.Collection.class), new Type[] {}));
			ga.invokeInterface(Type.getType(java.util.Collection.class), new Method("iterator", Type.getType(java.util.Iterator.class), new Type[] {}));

			int iteratorLocal = ga.newLocal(Type.getType(java.util.Iterator.class));
			ga.storeLocal(iteratorLocal);

			// Loop: while (iterator.hasNext())
			Label loopStart = ga.newLabel();
			Label loopEnd = ga.newLabel();

			ga.mark(loopStart);
			ga.loadLocal(iteratorLocal);
			ga.invokeInterface(Type.getType(java.util.Iterator.class), new Method("hasNext", Type.BOOLEAN_TYPE, new Type[] {}));
			ga.visitJumpInsn(Opcodes.IFEQ, loopEnd); // IFEQ = if equal to zero (if false)

			// PropertyImpl prop = iterator.next();
			ga.loadLocal(iteratorLocal);
			ga.invokeInterface(Type.getType(java.util.Iterator.class), new Method("next", Type.getType(Object.class), new Type[] {}));
			ga.checkCast(Types.PROPERTY_IMPL);
			int propLocal = ga.newLocal(Types.PROPERTY_IMPL);
			ga.storeLocal(propLocal);

			// impl.setProperty(prop); - this handles everything: registration, defaults, and UDF creation
			ga.loadArg(0); // impl
			ga.loadLocal(propLocal);
			ga.invokeVirtual(Types.COMPONENT_IMPL, new Method("setProperty", Type.VOID_TYPE, new Type[] { Types.PROPERTY }));

			// Continue loop
			ga.goTo(loopStart);

			ga.mark(loopEnd);
			ga.mark(afterNullCheck);

			// return;
			ga.returnValue();
			ga.endMethod();
		}

	}

	private String getTagAttributeValue(Tag tag, String attrName) {
		Attribute attr = tag.getAttribute(attrName);
		if (attr != null && attr.getValue() != null) {
			try {
				return attr.getValue().toString();
			}
			catch (Exception e) {
				// Can't get literal value
			}
		}
		return null;
	}

	private static void writeKeysBatch(GeneratorAdapter ga, List<LitString> keyList, int from, int to) {
		for (int i = from; i < to; i++) {
			ga.loadArg(0); // CollectionKey[] array
			ga.push(i);
			ga.push(keyList.get(i).getString());

			// ExpressionUtil.writeOutSilent(ls, bc, Expression.MODE_REF);
			ga.invokeStatic(KEY_IMPL, KEY_INIT_KEYS);
			ga.arrayStore(Types.COLLECTION_KEY);
		}
	}

	private void writeOutStaticConstructor(ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, TagCIObject component, String name)
			throws TransformerException {

		List<StaticBody> staticBodies = component.getStaticBodies();
		if (ArrayUtil.isEmpty(staticBodies)) return;

		// if(true) return;
		final GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, STATIC_COMPONENT_CONSTR, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, STATIC_COMPONENT_CONSTR, writeLog(), suppressWSbeforeArg, output, returnValue,
				sourceCode.getSourceOffset());
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		// Scope oldData=null;
		final int oldData = adapter.newLocal(Types.VARIABLES);
		ASMConstants.NULL(adapter);
		adapter.storeLocal(oldData);

		// BodyContentUtil.pushBody(pc);
		final int localBC = adapter.newLocal(Types.OBJECT);
		adapter.loadArg(0);
		adapter.invokeStatic(Types.BC_UTIL, PUSH_BODY);
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
		// ExpressionUtil.visitLine(bc, component.getStart());

		writeOutConstrBody(bc, staticBodies, IFunction.PAGE_TYPE_COMPONENT);

		// ExpressionUtil.visitLine(bc, component.getEnd());
		int t = tcf.visitTryEndCatchBeging(bc);
		// BodyContentUtil.flushAndPop(pc,bc);
		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BC_UTIL, FLUSH_AND_POP);

		// throw Caster.toPageException(t);
		adapter.loadLocal(t);
		adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);
		adapter.throwException();
		tcf.visitCatchEnd(bc);

		adapter.loadArg(0);
		adapter.loadLocal(localBC);
		adapter.invokeStatic(Types.BC_UTIL, FLUSH_AND_POP);// TODO why does the body constuctor call clear and it works?

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

	private int pushBody(BytecodeContext bc, final GeneratorAdapter adapter, boolean store) {
		int localBC = store ? adapter.newLocal(Types.OBJECT) : 0;
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
		adapter.invokeStatic(Types.BC_UTIL, PUSH_BODY);

		cv.visitOtherviseAfterBody();
		cv.visitAfter(bc);
		if (store) adapter.storeLocal(localBC);
		return localBC;
	}

	private List<IFunction> writeOutInitComponent(ConstrBytecodeContext constr, Function[] functions, Map<LitString, Integer> keys, ClassWriter cw, Tag component, String name)
			throws TransformerException {

		boolean hasStatements = ASMUtil.countNoneFunctionsStatements(component.getBody()) > 0;

		final GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_COMPONENT3, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, INIT_COMPONENT3, writeLog(), suppressWSbeforeArg, output, returnValue,
				sourceCode.getSourceOffset());
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		// Scope oldData=null;
		final int oldData = adapter.newLocal(Types.VARIABLES);
		ASMConstants.NULL(adapter);
		adapter.storeLocal(oldData);

		// INIT - c.init(pc,this);
		{
			adapter.loadArg(1);
			adapter.loadArg(0);
			adapter.loadThis();
			adapter.loadArg(2);
			adapter.invokeVirtual(Types.COMPONENT_IMPL, INIT_COMPONENT);
		}

		// Check if executeConstr is true, if so execute the main logic
		adapter.loadArg(2);
		Label afterIf = new Label();
		adapter.visitJumpInsn(Opcodes.IFEQ, afterIf); // Changed from IFNE to IFEQ

		List<IFunction> funcs;
		if (hasStatements) {

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

			int localBC = pushBody(bc, adapter, true);

			tcf.visitTryBegin(bc);
			// oldData=c.beforeCall(pc);
			adapter.loadArg(1);
			adapter.loadArg(0);
			adapter.invokeVirtual(Types.COMPONENT_IMPL, BEFORE_CALL);
			adapter.storeLocal(oldData);
			bc.visitLine(component.getStart());

			funcs = writeOutCallBody(bc, component.getBody(), IFunction.PAGE_TYPE_COMPONENT, false);

			bc.visitLine(component.getEnd());
			int t = tcf.visitTryEndCatchBeging(bc);
			// BodyContentUtil.flushAndPop(pc,bc);
			adapter.loadArg(0);
			adapter.loadLocal(localBC);
			adapter.invokeStatic(Types.BC_UTIL, FLUSH_AND_POP);

			// throw Caster.toPageException(t);
			adapter.loadLocal(t);
			adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION);
			adapter.throwException();
			tcf.visitCatchEnd(bc);

			adapter.loadArg(0);
			adapter.loadLocal(localBC);
			adapter.invokeStatic(Types.BC_UTIL, CLEAR_AND_POP);
		}
		else {
			bc.visitLine(component.getStart());
			funcs = writeOutCallBody(bc, component.getBody(), IFunction.PAGE_TYPE_COMPONENT, false);
			bc.visitLine(component.getEnd());
		}

		adapter.visitLabel(afterIf); // Move this label after the main logic

		adapter.returnValue();
		adapter.visitLabel(methodEnd);

		adapter.endMethod();
		return funcs;
	}

	private List<IFunction> writeOutInitInterface(ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, Tag interf, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_INTERFACE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, INIT_INTERFACE, writeLog(), suppressWSbeforeArg, output, returnValue,
				sourceCode.getSourceOffset());
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		bc.visitLine(interf.getStart());
		List<IFunction> funcs = writeOutCallBody(bc, interf.getBody(), IFunction.PAGE_TYPE_INTERFACE, false);
		bc.visitLine(interf.getEnd());

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

	public List<Function> getFunctions() {
		List<Function> funcs = new ArrayList<>();
		for (IFunction f: functions) {
			funcs.add((Function) f);
		}
		return funcs;
	}

	private ATagThread[] getThreads() {
		ATagThread[] threads = new ATagThread[this.threads.size()];
		Iterator<ATagThread> it = this.threads.iterator();
		int count = 0;
		while (it.hasNext()) {
			threads[count++] = it.next();
		}
		return threads;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {

	}

	private void writeOutNewComponent(ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, Tag component, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_COMPONENT_IMPL_INSTANCE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, NEW_COMPONENT_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output,
				returnValue, sourceCode.getSourceOffset());
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		bc.visitLine(component.getStart());
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
			persistent = ASMUtil.toBoolean(constr, attr, component.getStart()).booleanValue();
		}

		// accessors
		attr = component.removeAttribute("accessors");
		boolean accessors = false;
		if (attr != null) {
			accessors = ASMUtil.toBoolean(constr, attr, component.getStart()).booleanValue();
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

		adapter.invokeConstructor(Types.COMPONENT_IMPL, CONSTR_COMPONENT_IMPL15);

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

	private void writeOutNewInterface(ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, Tag interf, String name) throws TransformerException {
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_INTERFACE_IMPL_INSTANCE, null, new Type[] { Types.PAGE_EXCEPTION }, cw);
		BytecodeContext bc = new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, NEW_INTERFACE_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output,
				returnValue, sourceCode.getSourceOffset());
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		bc.visitLine(interf.getStart());
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

		adapter.invokeConstructor(Types.INTERFACE_IMPL, CONSTR_INTERFACE_IMPL8);

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
			bc.getAdapter().checkCast(Types.STRUCT_IMPL);
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

	private List<IFunction> writeOutCall(ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, String name) throws TransformerException {
		// GeneratorAdapter adapter = bc.getAdapter();
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, CALL1, null, new Type[] { Types.THROWABLE }, cw);
		Label methodBegin = new Label();
		Label methodEnd = new Label();

		adapter.visitLocalVariable("this", "L" + name + ";", null, methodBegin, methodEnd, 0);
		adapter.visitLabel(methodBegin);

		List<IFunction> funcs = writeOutCallBody(
				new BytecodeContext(config, null, constr, this, keys, cw, name, adapter, CALL1, writeLog(), suppressWSbeforeArg, output, returnValue, sourceCode.getSourceOffset()),
				this, IFunction.PAGE_TYPE_REGULAR, true);

		adapter.visitLabel(methodEnd);
		adapter.returnValue();
		adapter.endMethod();
		return funcs;
	}

	private List<IFunction> writeOutCallBody(BytecodeContext bc, Body body, int pageType, boolean needReturn) throws TransformerException {
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
			else if (needReturn) ASMConstants.NULL(bc.getAdapter());
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
		int index = 0;
		IFunction f;
		while (it.hasNext()) {
			f = it.next();
			f.writeOut(bc, pageType);
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

	/**
	 * @return if it is a component
	 */
	@Override
	public boolean isComponent() {
		return isComponent(null);
	}

	/**
	 * @return if it is an interface
	 */
	@Override
	public boolean isInterface() {
		return isInterface(null);
	}

	public boolean isComponent(TagCIObject cio) {
		if (cio == null) cio = getTagCFObject(null);
		return cio instanceof TagComponent;

	}

	/**
	 * @return if it is an interface
	 */
	public boolean isInterface(TagCIObject cio) {
		if (cio == null) cio = getTagCFObject(null);
		return cio instanceof TagInterface;
	}

	@Override
	public boolean isPage() {
		return getTagCFObject(null) == null;
	}

	/**
	 * @return the lastModifed
	 */
	@Override
	public long getLastModifed() {
		return lastModifed;
	}

	@Override
	public int addFunction(IFunction function) {
		functions.add(function);
		if (function instanceof Function) {
			((Function) function).setIndex(functions.size() - 1);
		}
		return functions.size() - 1;
	}

	public void removeFunction(IFunction function) {
		functions.remove(function);
		int index = 0;
		for (IFunction f: functions) {
			if (function instanceof Function) {
				((Function) f).setIndex(index);
			}
			index++;
		}
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

	public int addThread(ATagThread thread) {
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

	@Override
	public SourceCode getSourceCode() {
		return sourceCode;
	}

	public void setSplitIfNecessary(boolean splitIfNecessary) {
		this.splitIfNecessary = splitIfNecessary;
	}

	public boolean getSplitIfNecessary() {
		return splitIfNecessary;
	}

	@Override
	public boolean getSupressWSbeforeArg() {
		return suppressWSbeforeArg;
	}

	@Override
	public boolean getOutput() {
		return output;
	}

	@Override
	public boolean returnValue() {
		return returnValue;
	}

	@Override
	public Config getConfig() {
		return config;
	}

	public void doFinalize(BytecodeContext bc) {
		bc.visitLine(getEnd());
	}

	public void registerJavaFunction(JavaFunction javaFunction) {
		if (javaFunctions == null) javaFunctions = new ArrayList<>();
		javaFunctions.add(javaFunction);
	}

	public List<JavaFunction> getJavaFunctions() {
		return javaFunctions;
	}

	@Override
	public void dump(Struct sct) {
		// make sure they are at the start
		sct.setEL(KeyConstants._start, null);
		sct.setEL(KeyConstants._end, null);

		super.dump(sct);
		sct.setEL(KeyConstants._type, "Program");

		Array arr = Caster.toArray(sct.get(KeyConstants._body, null), null);
		if (arr != null) {
			Iterator<Object> it = arr.valueIterator();
			Struct bodyElement, s = null, e, end = null;
			while (it.hasNext()) {
				bodyElement = Caster.toStruct(it.next(), null);
				if (bodyElement != null) {
					// start
					if (s == null) {
						s = Caster.toStruct(bodyElement.get(KeyConstants._start, null), null);
						if (s != null) {
							sct.setEL(KeyConstants._start, s);
						}
					}
					// end
					e = Caster.toStruct(bodyElement.get(KeyConstants._end, null), null);
					if (e != null) end = e;
				}

			}
			if (end != null) {
				sct.setEL(KeyConstants._end, end);
			}

		}
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