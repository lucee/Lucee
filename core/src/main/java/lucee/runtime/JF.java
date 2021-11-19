package lucee.runtime;

import java.util.HashSet;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFPropertiesImpl;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.UDFUtil;

public abstract class JF implements UDF {

	private static final long serialVersionUID = 3952006862868945777L;

	private PageSource pagesource;

	private UDFPropertiesImpl props;

	public JF(String functionName, int access, int modifier, int type, String strType, String hint, Boolean output, Boolean bufferOutput, String displayName, String description,
			int returnFormat, Boolean secureJson, Boolean verifyClient, int localMode) {
		props = new UDFPropertiesImpl();
		props.access = access;
		props.modifier = modifier;
		props.hint = hint;
		props.functionName = functionName;
		props.index = 0;
		props.strReturnType = strType;
		props.output = output == null ? false : output.booleanValue();
		props.bufferOutput = bufferOutput;
		props.displayName = displayName;
		props.description = description;
		props.returnFormat = returnFormat;
		props.secureJson = secureJson;
		props.verifyClient = verifyClient;
		props.localMode = localMode;

		props.cachedWithin = null;// TODO
		props.strReturnFormat = UDFUtil.toReturnFormat(props.getReturnFormat(), "wddx");
		props.meta = null; // TODO

	}

	/*
	 * public FunctionArgument[] getFunctionArguments() { return new FunctionArgument[] {
	 * 
	 * }; }
	 */

	@Override
	public String getFunctionName() {
		return props.getFunctionName();
	}

	@Override
	public String getDescription() {
		return props.getDescription();
	}

	@Override
	public String getHint() {
		return props.getHint();
	}

	@Override
	public Component getOwnerComponent() {
		return null;
	}

	@Override
	public PageSource getPageSource() {
		return pagesource;
	}

	@Override
	public String getReturnTypeAsString() {
		return props.getReturnTypeAsString();
	}

	@Override
	public int getReturnType() {
		return props.getReturnType();
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return ComponentUtil.getMetaData(pc, getUDFProperties());
	}

	private UDFPropertiesImpl getUDFProperties() throws PageException {
		if (props.arguments == null) {
			props.arguments = getFunctionArguments();
			props.argumentsSet = new HashSet<Key>();
			for (FunctionArgument arg: props.arguments) {
				props.argumentsSet.add(arg.getName());
			}
		}

		return props;
	}

	@Override
	public DumpData toDumpData(PageContext pc, int maxlevel, DumpProperties dp) {
		return UDFUtil.toDumpData(pc, maxlevel, dp, this, UDFUtil.TYPE_UDF);
	}

	@Override
	public Object implementation(PageContext pc) throws Throwable {
		return this;
	}

	@Override
	public UDF duplicate() {
		try {
			return (UDF) ClassUtil.newInstance(this.getClass());
		}
		catch (Exception e) {
			return this;
		}
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Struct args, boolean b) throws PageException {
		return null; // TODO
	}

	@Override
	public int getReturnFormat() {
		return props.getReturnFormat();
	}

	@Override
	public int getReturnFormat(int df) {
		// TODO
		return df;
	}

	@Override
	public String id() {
		return toString();
	}

	@Override
	public int getAccess() {
		return props.getAccess();
	}

	@Override
	public int getModifier() {
		return props.getModifier();
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public int getIndex() {
		return props.getIndex();
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return null;
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object df) throws PageException {
		return df;
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		return false;
	}

	@Override
	public String getDisplayName() {
		return props.getDisplayName();
	}

	@Override
	public String getSource() {
		return getPageSource() != null ? getPageSource().getDisplayPath() : "";
	}

	@Override
	public Boolean getVerifyClient() {
		return props.getVerifyClient();
	}

	@Override
	public Boolean getSecureJson() {
		return props.getSecureJson();
	}

	@Override
	public boolean getOutput() {
		return props.getOutput();
	}

	@Override
	public Object call(PageContext pc, Key calledName, Object[] args, boolean b) throws PageException {
		return call(pc, args, b);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key calledName, Struct args, boolean b) throws PageException {
		return callWithNamedValues(pc, args, b);
	}

	public void setPageSource(PageSource pageSource) {
		this.pagesource = pageSource;
	}

	@Override
	public abstract FunctionArgument[] getFunctionArguments();
}
