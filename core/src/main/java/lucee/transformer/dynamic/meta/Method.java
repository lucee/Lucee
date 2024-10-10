package lucee.transformer.dynamic.meta;

import java.io.IOException;

import org.objectweb.asm.Type;

public interface Method extends FunctionMember {
	public String getReturn();

	public Type getReturnType();

	public Class getReturnClass();

	public String getDeclaringProviderRtnClassName();

	public Class getDeclaringProviderRtnClass();

	public String getDeclaringProviderRtnClassNameWithSameAccess();

	public Class getDeclaringProviderRtnClassWithSameAccess();

	public Object invoke(Object obj, Object... args) throws IOException;
}