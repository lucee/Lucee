package lucee.transformer.dynamic.meta.reflection;

import org.objectweb.asm.Type;

import lucee.transformer.dynamic.meta.LegacyMethod;
import lucee.transformer.dynamic.meta.Method;

class MethodReflection extends FunctionMemberReflection implements Method, LegacyMethod {

	private static final long serialVersionUID = 2024400200836232268L;
	private java.lang.reflect.Method method;

	public MethodReflection(java.lang.reflect.Method method) {
		super(method);
		this.method = method;
	}

	@Override
	public String getReturn() {
		return method.getReturnType().getName();
	}

	@Override
	public Type getReturnType() {
		return Type.getType(method.getReturnType());
	}

	@Override
	public Class getReturnClass() {
		return method.getReturnType();
	}

	@Override
	public java.lang.reflect.Method getMethod() {
		return method;
	}

	public Type getDeclaringProviderRtnType() {
		// TODO not correct
		return getReturnType();
	}

	@Override
	public String getDeclaringProviderRtnClassName() {
		// TODO not correct
		return getReturn();
	}

	@Override
	public Class getDeclaringProviderRtnClass() {
		// TODO not correct
		return getReturnClass();
	}

	@Override
	public String getDeclaringProviderRtnClassNameWithSameAccess() {
		// TODO not correct
		return getReturn();
	}

	@Override
	public Class getDeclaringProviderRtnClassWithSameAccess() {
		// TODO not correct
		return getReturnClass();
	}
}
