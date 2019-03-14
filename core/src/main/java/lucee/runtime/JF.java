package lucee.runtime;

import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.UDFUtil;

public abstract class JF implements UDF {

    private static final long serialVersionUID = 3952006862868945777L;

    private String functionName;
    private String description;
    private int type;
    private String strType;

    public JF(String functionName, int type, String strType, String description) {
	this.functionName = functionName;
	this.type = type;
	this.strType = strType;
	this.description = description;
    }

    /*
     * public FunctionArgument[] getFunctionArguments() { return new FunctionArgument[] {
     * 
     * }; }
     */

    @Override
    public String getFunctionName() {
	return functionName;
    }

    @Override
    public String getDescription() {
	return description;
    }

    @Override
    public String getHint() {
	return description;
    }

    @Override
    public Component getOwnerComponent() {
	return null;
    }

    @Override
    public PageSource getPageSource() {
	return null;
    }

    @Override
    public String getReturnTypeAsString() {
	return strType;
    }

    @Override
    public int getReturnType() {
	return type;
    }

    @Override
    public Struct getMetaData(PageContext pc) throws PageException {
	return null;
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
	    return this.getClass().newInstance();
	}
	catch (Exception e) {
	    return this;
	}
    }

    @Override
    public Object callWithNamedValues(PageContext pc, Struct args, boolean b) throws PageException {
	return null;
    }

    @Override
    public int getReturnFormat() {
	return UDF.RETURN_FORMAT_WDDX;
    }

    @Override
    public int getReturnFormat(int df) {
	return df;
    }

    @Override
    public String id() {
	return toString();
    }

    @Override
    public int getAccess() {
	return Component.ACCESS_PUBLIC;
    }

    @Override
    public int getModifier() {
	return Component.MODIFIER_NONE;
    }

    @Override
    public Object getValue() {
	return this;
    }

    @Override
    public int getIndex() {
	return 0;
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
	return null;
    }

    @Override
    public String getSource() {
	return getPageSource() != null ? getPageSource().getDisplayPath() : "";
    }

    @Override
    public Boolean getVerifyClient() {
	return null;
    }

    @Override
    public Boolean getSecureJson() {
	return null;
    }

    @Override
    public boolean getOutput() {
	return false;
    }

    @Override
    public Object call(PageContext pc, Key calledName, Object[] args, boolean b) throws PageException {
	return call(pc, args, b);
    }

    @Override
    public Object callWithNamedValues(PageContext pc, Key calledName, Struct args, boolean b) throws PageException {
	return callWithNamedValues(pc, args, b);
    }
}
