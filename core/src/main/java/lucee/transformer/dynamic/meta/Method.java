package lucee.transformer.dynamic.meta;

import org.objectweb.asm.Type;

public interface Method extends FunctionMember {
	public String getReturn();

	public Type getReturnType();

	public Class getReturnClass();
}
