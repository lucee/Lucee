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
package lucee.runtime.type;

import java.security.NoSuchAlgorithmException;

import lucee.commons.digest.Hash;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.component.MemberSupport;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.UDFCasterException;
import lucee.runtime.functions.decision.IsValid;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public abstract class UDFGSProperty extends MemberSupport implements UDFPlus {

	private static final long serialVersionUID = 285652503901488683L;

	private static final Collection.Key MIN_LENGTH = KeyImpl.getInstance("minLength");
	private static final Collection.Key MAX_LENGTH = KeyImpl.getInstance("maxLength");

	protected final FunctionArgument[] arguments;
	protected final String name;
	protected Component srcComponent;
	private UDFPropertiesBase properties;
	private String id;

	public UDFGSProperty(Component component, String name, FunctionArgument[] arguments, short rtnType) {
		super(Component.ACCESS_PUBLIC);
		properties = UDFProperties(null, component.getPageSource(), arguments, name, rtnType);
		this.name = name;
		this.arguments = arguments;
		this.srcComponent = component;
	}

	private static UDFPropertiesBase UDFProperties(Page page, PageSource pageSource, FunctionArgument[] arguments, String functionName, short returnType) {
		return new UDFPropertiesLight(page, pageSource, arguments, functionName, returnType);
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		return arguments;
	}

	@Override
	public String getFunctionName() {
		return name;
	}

	/*
	 * @Override public PageSource getPageSource() { return component.getPageSource(); }
	 */

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UDF)) return false;
		return UDFImpl.equals(this, (UDF) other);
	}

	@Override
	public String getSource() {
		PageSource ps = srcComponent.getPageSource();
		if (ps != null) return ps.getDisplayPath();
		return "";
	}

	@Override
	public String id() {
		if (id == null) {
			try {
				id = Hash.md5(srcComponent.id() + ":" + getFunctionName());
			}
			catch (NoSuchAlgorithmException e) {
				id = srcComponent.id() + ":" + getFunctionName();
			}
		}
		return id;
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public Component getOwnerComponent() {

		return getOwnerComponent(null);
	}

	public Component getOwnerComponent(PageContext pc) {
		return srcComponent;
	}

	@Override
	public void setOwnerComponent(Component component) {
		this.srcComponent = component;
	}

	public Page getPage() {
		throw new PageRuntimeException(new DeprecatedException("method getPage():Page is no longer suppoted, use instead getPageSource():PageSource"));
	}

	@Override
	public boolean getOutput() {
		return false;
	}

	@Override
	public final UDF duplicate(boolean deep) {
		UDF udf = duplicate(); // deep has no influence here, because a UDF is not a collection
		if (udf instanceof UDFPlus) {
			UDFPlus udfp = (UDFPlus) udf;
			udfp.setOwnerComponent(srcComponent);
			udfp.setAccess(getAccess());
		}
		return udf;
	}

	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getHint() {
		return "";
	}

	@Override
	public int getReturnFormat() {
		return UDF.RETURN_FORMAT_WDDX;
	}

	@Override
	public int getReturnFormat(int defaultValue) {
		return defaultValue;
	}

	@Override
	public int getReturnType() {
		return CFTypes.toShortStrict(getReturnTypeAsString(), CFTypes.TYPE_UNKNOW);
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public Boolean getSecureJson() {
		return null;
	}

	@Override
	public Boolean getVerifyClient() {
		return null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return UDFUtil.toDumpData(pageContext, maxlevel, properties, this, UDFUtil.TYPE_UDF);
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return ComponentUtil.getMetaData(pc, properties);
	}

	final Object cast(PageContext pc, FunctionArgument arg, Object value, int index) throws PageException {
		if (value == null || Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value)) return value;
		throw new UDFCasterException(this, arg, value, index);
	}

	final static void validate(String validate, Struct validateParams, Object obj) throws PageException {
		if (StringUtil.isEmpty(validate, true)) return;
		validate = validate.trim().toLowerCase();

		if (!validate.equals("regex") && !Decision.isValid(validate, obj)) throw new ExpressionException(createMessage(validate, obj));

		// range
		if (validateParams == null) return;

		if (validate.equals("integer") || validate.equals("numeric") || validate.equals("number")) {
			double min = Caster.toDoubleValue(validateParams.get(KeyConstants._min, null), false, Double.NaN);
			double max = Caster.toDoubleValue(validateParams.get(KeyConstants._max, null), false, Double.NaN);
			double d = Caster.toDoubleValue(obj);
			if (!Double.isNaN(min) && d < min)
				throw new ExpressionException(validate + " [" + Caster.toString(d) + "] is out of range, value must be more than or equal to [" + min + "]");
			if (!Double.isNaN(max) && d > max)
				throw new ExpressionException(validate + " [" + Caster.toString(d) + "] is out of range, value must be less than or equal to [" + max + "]");
		}
		else if (validate.equals("string")) {
			double min = Caster.toDoubleValue(validateParams.get(MIN_LENGTH, null), false, Double.NaN);
			double max = Caster.toDoubleValue(validateParams.get(MAX_LENGTH, null), false, Double.NaN);
			String str = Caster.toString(obj);
			int l = str.length();
			if (!Double.isNaN(min) && l < ((int) min))
				throw new ExpressionException("string [" + str + "] is to short [" + l + "], the string must be at least [" + min + "] characters");
			if (!Double.isNaN(max) && l > ((int) max))
				throw new ExpressionException("string [" + str + "] is to long [" + l + "], the string can have a maximum length of [" + max + "] characters");
		}
		else if (validate.equals("regex")) {
			String pattern = Caster.toString(validateParams.get(KeyConstants._pattern, null), null);
			String value = Caster.toString(obj);
			if (!StringUtil.isEmpty(pattern, true) && !IsValid.regex(ThreadLocalPageContext.get(), value, pattern))
				throw new ExpressionException("the string [" + value + "] does not match the regular expression pattern [" + pattern + "]");
		}
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key calledName, Struct values, boolean doIncludePath) throws PageException {
		PageContextImpl pci = ((PageContextImpl) pc);
		UDF parent = pc.getActiveUDF();
		Key parentName = pci.getActiveUDFCalledName();
		pci.setActiveUDF(this);
		pci.setActiveUDFCalledName(calledName);
		try {
			return callWithNamedValues(pci, values, doIncludePath);
		}
		finally {
			pci.setActiveUDF(parent);
			pci.setActiveUDFCalledName(parentName);
		}
	}

	@Override
	public Object call(PageContext pc, Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		PageContextImpl pci = ((PageContextImpl) pc);
		UDF parent = pc.getActiveUDF();
		Key parentName = pci.getActiveUDFCalledName();
		pci.setActiveUDFCalledName(calledName);
		pci.setActiveUDF(this);
		try {
			return call(pci, args, doIncludePath);
		}
		finally {
			pci.setActiveUDF(parent);
			pci.setActiveUDFCalledName(parentName);
		}
	}

	@Override
	public final Object call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pageContext;
		UDF parent = pci.getActiveUDF();
		pci.setActiveUDF(this);
		try {
			return _call(pageContext, args, doIncludePath);
		}
		finally {
			pci.setActiveUDF(parent);
		}
	}

	public abstract Object _call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException;

	@Override
	public final Object callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pageContext;
		UDF parent = pci.getActiveUDF();
		pci.setActiveUDF(this);
		try {
			return _callWithNamedValues(pageContext, values, doIncludePath);
		}
		finally {
			pci.setActiveUDF(parent);
		}
	}

	public abstract Object _callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException;

	private static String createMessage(String format, Object value) {
		if (Decision.isSimpleValue(value)) return "the value [" + Caster.toString(value, null) + "] is not in  [" + format + "] format";
		return "cannot convert object from type [" + Caster.toTypeName(value) + "] to a [" + format + "] format";
	}

	@Override
	public PageSource getPageSource() {
		return srcComponent.getPageSource();
		// return this.properties.getPageSource();
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		return pc.getApplicationContext().getBufferOutput();
	}

	public Component getComponent(PageContext pc) {
		if (pc == null) pc = ThreadLocalPageContext.get();
		if (pc != null) {
			Variables var = pc.variablesScope();
			if (var instanceof ComponentScope) {
				Component comp = ((ComponentScope) var).getComponent();
				if (comp != null) return comp;
			}
		}
		return srcComponent;
	}
}