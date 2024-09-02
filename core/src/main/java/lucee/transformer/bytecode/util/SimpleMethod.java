package lucee.transformer.bytecode.util;

import java.io.IOException;

public interface SimpleMethod {
	public String getName();

	public Class[] getParameterTypes() throws IOException;

	public Class getReturnType() throws IOException;
}