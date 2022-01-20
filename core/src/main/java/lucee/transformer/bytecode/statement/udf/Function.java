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
package lucee.transformer.bytecode.statement.udf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.runtime.Component;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.FunctionArgumentImpl;
import lucee.runtime.type.FunctionArgumentLight;
import lucee.runtime.type.util.ComponentUtil;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.ConstrBytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Root;
import lucee.transformer.bytecode.statement.Argument;
import lucee.transformer.bytecode.statement.HasBody;
import lucee.transformer.bytecode.statement.IFunction;
import lucee.transformer.bytecode.statement.StatementBaseNoFinal;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;

public abstract class Function extends StatementBaseNoFinal implements Opcodes, IFunction, HasBody {

	// Scope variablesScope()
	static final Method VARIABLE_SCOPE = new Method("variablesScope", Types.VARIABLES, new Type[] {});
	// Scope variablesScope()
	static final Method GET_PAGESOURCE = new Method("getPageSource", Types.PAGE_SOURCE, new Type[] {});

	// Object set(String,Object)
	/*
	 * static final Method SET_STR = new Method( "set", Types.OBJECT, new
	 * Type[]{Types.STRING,Types.OBJECT} );
	 */

	static final Method SET_KEY = new Method("set", Types.OBJECT, new Type[] { Types.COLLECTION_KEY, Types.OBJECT });

	static final Method REG_UDF_KEY = new Method("registerUDF", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.UDF_PROPERTIES });
	static final Method REG_STATIC_UDF_KEY = new Method("registerStaticUDF", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.UDF_PROPERTIES });
	// private static final ExprString ANY = LitString.toExprString("any");

	// <init>(Page,FunctionArgument[],int String,String,boolean);
	private static final Type FUNCTION_ARGUMENT = Type.getType(FunctionArgument.class);
	private static final Type FUNCTION_ARGUMENT_IMPL = Type.getType(FunctionArgumentImpl.class);
	private static final Type FUNCTION_ARGUMENT_LIGHT = Type.getType(FunctionArgumentLight.class);
	private static final Type FUNCTION_ARGUMENT_ARRAY = Type.getType(FunctionArgument[].class);

	protected static final Method INIT_UDF_IMPL_PROP = new Method("<init>", Types.VOID, new Type[] { Types.UDF_PROPERTIES });

	private static final Method INIT_UDF_PROPERTIES_STRTYPE = new Method("<init>", Types.VOID,
			new Type[] { Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE, FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.STRING, Types.STRING,
					Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN, Types.BOOLEAN, Types.OBJECT, Types.INTEGER,
					Types.INT_VALUE, Types.STRUCT_IMPL });
	private static final Method INIT_UDF_PROPERTIES_SHORTTYPE = new Method("<init>", Types.VOID,
			new Type[] { Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE, FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.SHORT_VALUE, Types.STRING,
					Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN, Types.BOOLEAN, Types.OBJECT, Types.INTEGER,
					Types.INT_VALUE, Types.STRUCT_IMPL });
	private static final Method INIT_UDF_PROPERTIES_SHORTTYPE_LIGHT = new Method("<init>", Types.VOID, new Type[] { Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE,
			FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.SHORT_VALUE, Types.STRING, Types.BOOLEAN_VALUE, Types.INT_VALUE });

	// FunctionArgumentImpl(String name,String type,boolean required,int defaultType,String
	// dspName,String hint,StructImpl meta)
	private static final Method INIT_FAI_KEY1 = new Method("<init>", Types.VOID, new Type[] { Types.COLLECTION_KEY });
	private static final Method INIT_FAI_KEY3 = new Method("<init>", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE });
	private static final Method INIT_FAI_KEY4 = new Method("<init>", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE });
	private static final Method INIT_FAI_KEY5 = new Method("<init>", Types.VOID,
			new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE });
	private static final Method INIT_FAI_KEY6 = new Method("<init>", Types.VOID,
			new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE });
	private static final Method INIT_FAI_KEY7 = new Method("<init>", Types.VOID,
			new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING });
	private static final Method INIT_FAI_KEY8 = new Method("<init>", Types.VOID,
			new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING });
	private static final Method INIT_FAI_KEY9 = new Method("<init>", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE,
			Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING, Types.STRUCT_IMPL });
	private static final Method[] INIT_FAI_KEY = new Method[] { INIT_FAI_KEY1, INIT_FAI_KEY3, INIT_FAI_KEY4, INIT_FAI_KEY5, INIT_FAI_KEY6, INIT_FAI_KEY7, INIT_FAI_KEY8,
			INIT_FAI_KEY9 };
	private static final Method[] INIT_FAI_KEY_LIGHT = new Method[] { INIT_FAI_KEY1, INIT_FAI_KEY3 };

	protected static final Method USE_JAVA_FUNCTION = new Method("useJavaFunction", Types.OBJECT, new Type[] { Types.PAGE, Types.STRING });
	protected static final Method REG_JAVA_FUNCTION = new Method("regJavaFunction", Types.VOID, new Type[] { Types.COLLECTION_KEY, Types.STRING });

	ExprString name;
	ExprString returnType;
	ExprBoolean output;
	ExprBoolean bufferOutput;
	// ExprBoolean abstry=LitBoolean.FALSE;
	int access = Component.ACCESS_PUBLIC;
	ExprString displayName;
	ExprString hint;
	Body body;
	List<Argument> arguments = new ArrayList<Argument>();
	Map<String, Attribute> metadata;
	ExprString returnFormat;
	ExprString description;
	ExprBoolean secureJson;
	ExprBoolean verifyClient;
	ExprInt localMode;
	protected int valueIndex = -1;
	protected int arrayIndex = -1;
	private Literal cachedWithin;
	private int modifier;
	protected JavaFunction jf;
	private final Root root;

	public Function(Root root, String name, int access, int modifier, String returnType, Body body, Position start, Position end) {
		super(body.getFactory(), start, end);
		this.name = body.getFactory().createLitString(name);
		this.access = access;
		this.modifier = modifier;
		if (!StringUtil.isEmpty(returnType)) this.returnType = body.getFactory().createLitString(returnType);
		else this.returnType = body.getFactory().createLitString("any");
		this.body = body;
		body.setParent(this);
		output = body.getFactory().TRUE();
		displayName = body.getFactory().EMPTY();
		hint = body.getFactory().EMPTY();
		this.root = root;
	}

	public Function(Root root, Expression name, Expression returnType, Expression returnFormat, Expression output, Expression bufferOutput, int access, Expression displayName,
			Expression description, Expression hint, Expression secureJson, Expression verifyClient, Expression localMode, Literal cachedWithin, int modifier, Body body,
			Position start, Position end) {
		super(body.getFactory(), start, end);

		this.name = body.getFactory().toExprString(name);
		this.returnType = body.getFactory().toExprString(returnType);
		this.returnFormat = returnFormat != null ? body.getFactory().toExprString(returnFormat) : null;
		this.output = body.getFactory().toExprBoolean(output);
		this.bufferOutput = bufferOutput == null ? null : body.getFactory().toExprBoolean(bufferOutput);
		this.access = access;
		this.description = description != null ? body.getFactory().toExprString(description) : null;
		this.displayName = body.getFactory().toExprString(displayName);
		this.hint = body.getFactory().toExprString(hint);
		this.secureJson = secureJson != null ? body.getFactory().toExprBoolean(secureJson) : null;
		this.verifyClient = verifyClient != null ? body.getFactory().toExprBoolean(verifyClient) : null;
		this.cachedWithin = cachedWithin;
		this.modifier = modifier;
		this.localMode = toLocalMode(localMode, null);
		this.root = root;
		this.body = body;
		body.setParent(this);

	}

	public void register() {
		if (valueIndex != -1) throw new RuntimeException("you can register only once!"); // just to be safe

		int[] indexes = root.addFunction(this);
		valueIndex = indexes[VALUE_INDEX];
		arrayIndex = indexes[ARRAY_INDEX];
	}

	public static ExprInt toLocalMode(Expression expr, ExprInt defaultValue) {
		int mode = -1;
		if (expr instanceof Literal) {
			String str = ((Literal) expr).getString();
			str = str.trim().toLowerCase();
			mode = AppListenerUtil.toLocalMode(str, -1);
		}
		if (mode == -1) return defaultValue;
		return expr.getFactory().createLitInteger(mode);
	}

	@Override
	public final void writeOut(BytecodeContext bc, int type) throws TransformerException {
		ExpressionUtil.visitLine(bc, getStart());
		_writeOut(bc, type);
		ExpressionUtil.visitLine(bc, getEnd());
	}

	@Override
	public final void _writeOut(BytecodeContext bc) throws TransformerException {
		_writeOut(bc, PAGE_TYPE_REGULAR);
	}

	public abstract void _writeOut(BytecodeContext bc, int pageType) throws TransformerException;

	public final void loadUDFProperties(BytecodeContext bc, int valueIndex, int arrayIndex, int type) {
		ConstrBytecodeContext constr = bc.getConstructor();
		// GeneratorAdapter cga = constr.getAdapter();
		GeneratorAdapter ga = bc.getAdapter();

		// store to construction method

		constr.addUDFProperty(this, arrayIndex, valueIndex, type);
		/*
		 * cga.visitVarInsn(ALOAD, 0); cga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs",
		 * Types.UDF_PROPERTIES_ARRAY.toString()); cga.push(arrayIndex);
		 * createUDFProperties(constr,valueIndex,type); cga.visitInsn(AASTORE);
		 */

		// load in execution method
		ga.visitVarInsn(ALOAD, 0);
		ga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs", Types.UDF_PROPERTIES_ARRAY.toString());
		ga.push(arrayIndex);
		ga.visitInsn(AALOAD);
	}

	public final void createUDFProperties(BytecodeContext bc, int index, int type) throws TransformerException {

		GeneratorAdapter adapter = bc.getAdapter();
		adapter.newInstance(Types.UDF_PROPERTIES_IMPL);
		adapter.dup();

		// Page
		adapter.loadThis();

		// PageSource
		if (type != TYPE_UDF) {
			adapter.loadThis();
			adapter.invokeVirtual(Types.PAGE, GET_PAGESOURCE);
		}
		else adapter.visitVarInsn(ALOAD, 1);

		// position
		adapter.push(getStart() == null ? 0 : getStart().line);
		adapter.push(getEnd() == null ? 0 : getEnd().line);

		// arguments
		createArguments(bc);
		// index
		adapter.push(index);
		// name
		ExpressionUtil.writeOutSilent(name, bc, Expression.MODE_REF);
		// return type
		short sType = ExpressionUtil.toShortType(returnType, false, CFTypes.TYPE_UNKNOW);
		if (sType == CFTypes.TYPE_UNKNOW) ExpressionUtil.writeOutSilent(returnType, bc, Expression.MODE_REF);
		else adapter.push(sType);

		// return format
		if (returnFormat != null) ExpressionUtil.writeOutSilent(returnFormat, bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);

		// output
		ExpressionUtil.writeOutSilent(output, bc, Expression.MODE_VALUE);

		// access
		writeOutAccess(bc, access);

		boolean light = sType != -1;
		if (light && !bc.getFactory().EMPTY().equals(displayName)) light = false;
		if (light && description != null && !bc.getFactory().EMPTY().equals(description)) light = false;
		if (light && !bc.getFactory().EMPTY().equals(hint)) light = false;
		if (light && secureJson != null) light = false;
		if (light && verifyClient != null) light = false;
		if (light && cachedWithin != null) light = false;
		if (light && bufferOutput != null) light = false;
		if (light && localMode != null) light = false;
		if (light && modifier != Component.MODIFIER_NONE) light = false;
		if (light && Page.hasMetaDataStruct(metadata, null)) light = false;
		if (light) {
			adapter.invokeConstructor(Types.UDF_PROPERTIES_IMPL, INIT_UDF_PROPERTIES_SHORTTYPE_LIGHT);
			return;
		}

		// buffer output
		if (bufferOutput != null) ExpressionUtil.writeOutSilent(bufferOutput, bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);

		// displayName
		ExpressionUtil.writeOutSilent(displayName, bc, Expression.MODE_REF);// displayName;

		// description
		if (description != null) ExpressionUtil.writeOutSilent(description, bc, Expression.MODE_REF);// displayName;
		else adapter.push("");

		// hint
		ExpressionUtil.writeOutSilent(hint, bc, Expression.MODE_REF);// hint;

		// secureJson
		if (secureJson != null) ExpressionUtil.writeOutSilent(secureJson, bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);

		// verify client
		if (verifyClient != null) ExpressionUtil.writeOutSilent(verifyClient, bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);

		// cachedwithin
		if (cachedWithin != null) {
			cachedWithin.writeOut(bc, Expression.MODE_REF);
		}
		else ASMConstants.NULL(adapter);
		// adapter.push(cachedWithin<0?0:cachedWithin);

		// localMode
		if (localMode != null) ExpressionUtil.writeOutSilent(localMode, bc, Expression.MODE_REF);
		else ASMConstants.NULL(adapter);

		adapter.push(modifier);

		// meta
		Page.createMetaDataStruct(bc, metadata, null);

		adapter.invokeConstructor(Types.UDF_PROPERTIES_IMPL, sType == -1 ? INIT_UDF_PROPERTIES_STRTYPE : INIT_UDF_PROPERTIES_SHORTTYPE);

	}

	public final void createFunction(BytecodeContext bc, int index, int type) throws TransformerException {

		if (this.jf != null) {
			GeneratorAdapter adapter = bc.getAdapter();
			bc.registerJavaFunction(jf);
			adapter.loadArg(0);
			adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
			adapter.visitVarInsn(ALOAD, 0);
			adapter.push(jf.getClassName());
			adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, USE_JAVA_FUNCTION);
		}
		else {
			// new UDF(...)
			GeneratorAdapter adapter = bc.getAdapter();
			Type t;
			if (TYPE_CLOSURE == type) t = Types.CLOSURE;
			else if (TYPE_LAMBDA == type) t = Types.LAMBDA;
			else t = Types.UDF_IMPL;
			adapter.newInstance(t);

			adapter.dup();

			createUDFProperties(bc, index, type);

			adapter.invokeConstructor(t, INIT_UDF_IMPL_PROP);
		}

	}

	private final void createArguments(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter ga = bc.getAdapter();
		ga.push(arguments.size());
		ga.newArray(FUNCTION_ARGUMENT);
		Argument arg;
		for (int i = 0; i < arguments.size(); i++) {
			arg = arguments.get(i);

			boolean canHaveKey = Factory.canRegisterKey(arg.getName());

			// CHECK if default values
			// type
			ExprString _strType = arg.getType();
			short _type = CFTypes.TYPE_UNKNOW;
			if (_strType instanceof LitString) {
				_type = CFTypes.toShortStrict(((LitString) _strType).getString(), CFTypes.TYPE_UNKNOW);
			}
			boolean useType = !canHaveKey || _type != CFTypes.TYPE_ANY;
			// boolean useStrType=useType && (_type==CFTypes.TYPE_UNDEFINED || _type==CFTypes.TYPE_UNKNOW ||
			// CFTypes.toString(_type, null)==null);

			// required
			ExprBoolean _req = arg.getRequired();
			boolean useReq = !canHaveKey || toBoolean(_req, null) != Boolean.FALSE;

			// default-type
			Expression _def = arg.getDefaultValueType(bc.getFactory());
			boolean useDef = !canHaveKey || toInt(_def, -1) != FunctionArgument.DEFAULT_TYPE_NULL;

			// pass by reference
			ExprBoolean _pass = arg.isPassByReference();
			boolean usePass = !canHaveKey || toBoolean(_pass, null) != Boolean.TRUE;

			// display-hint
			ExprString _dsp = arg.getDisplayName();
			boolean useDsp = !canHaveKey || !isLiteralEmptyString(_dsp);

			// hint
			ExprString _hint = arg.getHint();
			boolean useHint = !canHaveKey || !isLiteralEmptyString(_hint);

			// meta
			Map _meta = arg.getMetaData();
			boolean useMeta = !canHaveKey || (_meta != null && !_meta.isEmpty());
			int functionIndex = 7;
			if (!useMeta) {
				functionIndex--;
				if (!useHint) {
					functionIndex--;
					if (!useDsp) {
						functionIndex--;
						if (!usePass) {
							functionIndex--;
							if (!useDef) {
								functionIndex--;
								if (!useReq) {
									functionIndex--;
									if (!useType) {
										functionIndex--;
									}
								}
							}
						}
					}
				}
			}
			// write out arguments
			ga.dup();
			ga.push(i);

			// new FunctionArgument(...)
			ga.newInstance(canHaveKey && functionIndex < INIT_FAI_KEY_LIGHT.length ? FUNCTION_ARGUMENT_LIGHT : FUNCTION_ARGUMENT_IMPL);
			ga.dup();
			// name
			bc.getFactory().registerKey(bc, arg.getName(), false);

			// type
			if (functionIndex >= INIT_FAI_KEY.length - 7) {
				_strType.writeOut(bc, Expression.MODE_REF);
				bc.getAdapter().push(_type);
			}
			// required
			if (functionIndex >= INIT_FAI_KEY.length - 6) _req.writeOut(bc, Expression.MODE_VALUE);
			// default value
			if (functionIndex >= INIT_FAI_KEY.length - 5) _def.writeOut(bc, Expression.MODE_VALUE);
			// pass by reference
			if (functionIndex >= INIT_FAI_KEY.length - 4) _pass.writeOut(bc, Expression.MODE_VALUE);
			// display-name
			if (functionIndex >= INIT_FAI_KEY.length - 3) _dsp.writeOut(bc, Expression.MODE_REF);
			// hint
			if (functionIndex >= INIT_FAI_KEY.length - 2) _hint.writeOut(bc, Expression.MODE_REF);
			// meta
			if (functionIndex == INIT_FAI_KEY.length - 1) Page.createMetaDataStruct(bc, _meta, null);

			if (functionIndex < INIT_FAI_KEY_LIGHT.length) ga.invokeConstructor(FUNCTION_ARGUMENT_LIGHT, INIT_FAI_KEY[functionIndex]);
			else ga.invokeConstructor(FUNCTION_ARGUMENT_IMPL, INIT_FAI_KEY[functionIndex]);

			ga.visitInsn(Opcodes.AASTORE);
		}
	}

	private final int toInt(Expression expr, int defaultValue) {
		if (expr instanceof LitInteger) {
			return ((LitInteger) expr).getInteger().intValue();
		}
		return defaultValue;
	}

	private final Boolean toBoolean(ExprBoolean expr, Boolean defaultValue) {
		if (expr instanceof LitBoolean) {
			return ((LitBoolean) expr).getBooleanValue() ? Boolean.TRUE : Boolean.FALSE;
		}
		return defaultValue;
	}

	private final boolean isLiteralEmptyString(ExprString expr) {
		if (expr instanceof LitString) {
			return StringUtil.isEmpty(((LitString) expr).getString());
		}
		return false;
	}

	private final void writeOutAccess(BytecodeContext bc, ExprString expr) {

		// write short type
		if (expr instanceof LitString) {
			int access = ComponentUtil.toIntAccess(((LitString) expr).getString(), Component.ACCESS_PUBLIC);
			bc.getAdapter().push(access);
		}
		else bc.getAdapter().push(Component.ACCESS_PUBLIC);
	}

	private final void writeOutAccess(BytecodeContext bc, int access) {
		bc.getAdapter().push(access);
	}

	public final void addArgument(Factory factory, String name, String type, boolean required, Expression defaultValue) {
		addArgument(factory.createLitString(name), factory.createLitString(type), factory.createLitBoolean(required), defaultValue, factory.TRUE(), factory.EMPTY(),
				factory.EMPTY(), null);
	}

	public final void addArgument(Expression name, Expression type, Expression required, Expression defaultValue, ExprBoolean passByReference, Expression displayName,
			Expression hint, Map meta) {
		arguments.add(new Argument(name, type, required, defaultValue, passByReference, displayName, hint, meta));
	}

	/**
	 * @return the arguments
	 */
	public final List<Argument> getArguments() {
		return arguments;
	}

	/**
	 * @return the body
	 */
	@Override
	public final Body getBody() {
		return body;
	}

	public final void setMetaData(Map<String, Attribute> metadata) {
		this.metadata = metadata;
	}

	public final void setHint(Factory factory, String hint) {
		this.hint = factory.createLitString(hint);
	}

	public final void addAttribute(Attribute attr) throws TemplateException {
		String name = attr.getName().toLowerCase();
		// name
		if ("name".equals(name)) {
			throw new TransformerException("Name cannot be defined twice", getStart());
		}
		else if ("returntype".equals(name)) {
			this.returnType = toLitString(name, attr.getValue());
		}
		else if ("access".equals(name)) {

			LitString ls = toLitString(name, attr.getValue());
			String strAccess = ls.getString();
			int acc = ComponentUtil.toIntAccess(strAccess, -1);
			if (acc == -1) throw new TransformerException("Invalid access type [" + strAccess + "], access types are (remote, public, package, private)", getStart());
			access = acc;

		}

		else if ("output".equals(name)) this.output = toLitBoolean(name, attr.getValue());
		else if ("bufferoutput".equals(name)) this.bufferOutput = toLitBoolean(name, attr.getValue());
		else if ("displayname".equals(name)) this.displayName = toLitString(name, attr.getValue());
		else if ("hint".equals(name)) this.hint = toLitString(name, attr.getValue());
		else if ("description".equals(name)) this.description = toLitString(name, attr.getValue());
		else if ("returnformat".equals(name)) this.returnFormat = toLitString(name, attr.getValue());
		else if ("securejson".equals(name)) this.secureJson = toLitBoolean(name, attr.getValue());
		else if ("verifyclient".equals(name)) this.verifyClient = toLitBoolean(name, attr.getValue());
		else if ("localmode".equals(name)) {
			Expression v = attr.getValue();
			if (v != null) {
				String str = ASMUtil.toString(v, null);
				if (!StringUtil.isEmpty(str)) {
					int mode = AppListenerUtil.toLocalMode(str, -1);
					if (mode != -1) this.localMode = v.getFactory().createLitInteger(mode);
					else throw new TransformerException("Attribute [localMode] of the tag [Function], must be a literal value (modern, classic, true or false)", getStart());
				}
			}
		}
		else if ("cachedwithin".equals(name)) {
			try {
				this.cachedWithin = ASMUtil.cachedWithinValue(attr.getValue());// ASMUtil.timeSpanToLong(attr.getValue());
			}
			catch (EvaluatorException e) {
				throw new TemplateException(e.getMessage());
			}
		}
		else if ("modifier".equals(name)) {
			Expression val = attr.getValue();
			if (val instanceof Literal) {
				Literal l = (Literal) val;
				String str = StringUtil.emptyIfNull(l.getString()).trim();
				if ("abstract".equalsIgnoreCase(str)) modifier = Component.MODIFIER_ABSTRACT;
				else if ("final".equalsIgnoreCase(str)) modifier = Component.MODIFIER_FINAL;
			}
		}

		else {
			toLitString(name, attr.getValue());// needed for testing
			if (metadata == null) metadata = new HashMap<String, Attribute>();
			metadata.put(attr.getName(), attr);
		}
	}

	private final LitString toLitString(String name, Expression value) throws TransformerException {
		ExprString es = value.getFactory().toExprString(value);
		if (!(es instanceof LitString)) throw new TransformerException("Value of attribute [" + name + "] must have a literal/constant value", getStart());
		return (LitString) es;
	}

	private final LitBoolean toLitBoolean(String name, Expression value) throws TransformerException {
		ExprBoolean eb = value.getFactory().toExprBoolean(value);
		if (!(eb instanceof LitBoolean)) throw new TransformerException("Value of attribute [" + name + "] must have a literal/constant value", getStart());
		return (LitBoolean) eb;
	}

	private final ExprInt toLitInt(String name, Expression value) throws TransformerException {
		ExprInt eb = value.getFactory().toExprInt(value);
		if (!(eb instanceof Literal)) throw new TransformerException("Value of attribute [" + name + "] must have a literal/constant value", getStart());
		return eb;
	}

	public void setJavaFunction(JavaFunction jf) {
		this.jf = jf;
	}
}
