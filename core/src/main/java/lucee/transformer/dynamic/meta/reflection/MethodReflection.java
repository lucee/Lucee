package lucee.transformer.dynamic.meta;

import org.objectweb.asm.Type;

public class MethodReflection extends FunctionMemberReflection implements Method {

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

	public java.lang.reflect.Method getMethod() {
		return method;
	}
}
