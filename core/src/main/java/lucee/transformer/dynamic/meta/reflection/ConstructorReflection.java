package lucee.transformer.dynamic.meta.reflection;

import lucee.transformer.dynamic.meta.Constructor;

public class ConstructorReflection extends FunctionMemberReflection implements Constructor {

	private java.lang.reflect.Constructor constructor;

	public ConstructorReflection(java.lang.reflect.Constructor constructor) {
		super(constructor);
		this.constructor = constructor;
	}

	public java.lang.reflect.Constructor getConstructor() {
		return constructor;
	}
}
