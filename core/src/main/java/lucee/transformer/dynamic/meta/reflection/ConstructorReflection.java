package lucee.transformer.dynamic.meta.reflection;

import org.objectweb.asm.Type;

import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.LegacyConstuctor;

class ConstructorReflection extends FunctionMemberReflection implements Constructor, LegacyConstuctor {

	private java.lang.reflect.Constructor constructor;

	public ConstructorReflection(java.lang.reflect.Constructor constructor) {
		super(constructor);
		this.constructor = constructor;
	}

	@Override
	public java.lang.reflect.Constructor getConstructor() {
		return constructor;
	}

	@Override
	public Object newInstance(Object... args) throws Exception {
		return constructor.newInstance(args);

	}

	@Override
	public String getReturn() {
		return getReturnClass().getName();
	}

	@Override
	public Type getReturnType() {
		return Type.getType(getDeclaringClass());
	}

	@Override
	public Class getReturnClass() {
		return getDeclaringClass();
	}
}
