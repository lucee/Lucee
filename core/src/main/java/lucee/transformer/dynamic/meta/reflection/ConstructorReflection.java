package lucee.transformer.dynamic.meta.reflection;

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
}
