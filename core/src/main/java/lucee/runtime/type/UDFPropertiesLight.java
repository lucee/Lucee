package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import lucee.commons.lang.CFTypes;
import lucee.runtime.Component;
import lucee.runtime.Page;
import lucee.runtime.PageSource;
import lucee.runtime.op.Constants;
import lucee.runtime.type.Collection.Key;

public class UDFPropertiesLight extends UDFPropertiesBase {

	private final FunctionArgument[] arguments;
	private final String functionName;
	private final short returnType;
	private HashSet<Key> argumentsSet;

	public UDFPropertiesLight(Page page, PageSource pageSource, FunctionArgument[] arguments, String functionName, short returnType) {
		super(page, pageSource, 0, 0);
		this.arguments = arguments;
		this.functionName = functionName;
		this.returnType = returnType;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

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
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public boolean getOutput() {
		return false;
	}

	@Override
	public Boolean getBufferOutput() {
		return Boolean.TRUE;
	}

	@Override
	public int getReturnType() {
		return returnType;
	}

	@Override
	public String getReturnTypeAsString() {
		return CFTypes.toString(returnType, "any");
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public int getReturnFormat() {
		return UDF.RETURN_FORMAT_JSON;
	}

	@Override
	public String getReturnFormatAsString() {
		return "json";
	}

	@Override
	public int getIndex() {
		return -1;
	}

	@Override
	public Object getCachedWithin() {
		return Constants.LONG_ZERO;
	}

	@Override
	public Boolean getSecureJson() {
		return Boolean.FALSE;
	}

	@Override
	public Boolean getVerifyClient() {
		return Boolean.FALSE;
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		return arguments;
	}

	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public String getHint() {
		return "";
	}

	@Override
	public Struct getMeta() {
		return null;
	}

	@Override
	public Integer getLocalMode() {
		return null;
	}

	@Override
	public Set<Key> getArgumentsSet() {
		if (arguments != null && arguments.length > 0) {
			this.argumentsSet = new HashSet<Collection.Key>();
			for (int i = 0; i < arguments.length; i++) {
				argumentsSet.add(arguments[i].getName());
			}
		}
		return argumentsSet;
	}

}
