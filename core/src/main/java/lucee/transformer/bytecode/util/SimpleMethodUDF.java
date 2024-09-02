package lucee.transformer.bytecode.util;

import java.io.IOException;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.UDF;

public class SimpleMethodUDF implements SimpleMethod {

	private UDF udf;

	public SimpleMethodUDF(UDF udf) {
		this.udf = udf;
	}

	@Override
	public String getName() {
		return udf.getFunctionName();
	}

	@Override
	public Class[] getParameterTypes() throws IOException {
		try {
			FunctionArgument[] args = udf.getFunctionArguments();
			if (args == null) return new Class[0];
			Class[] classes = new Class[args.length];
			int index = 0;
			for (FunctionArgument fa: args) {
				classes[index++] = Caster.cfTypeToClass(null, fa.getTypeAsString());
			}
			return classes;
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public Class getReturnType() throws IOException {
		try {
			return Caster.cfTypeToClass(null, udf.getReturnTypeAsString());
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}
}