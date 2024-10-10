package lucee.transformer.dynamic.meta.dynamic;

import java.io.IOException;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.KeyImpl;
import lucee.transformer.dynamic.meta.Method;

class MethodDynamic extends FunctionMemberDynamic implements Method {

	private static final long serialVersionUID = 7046827988301434206L;

	public MethodDynamic(Class declaringClass, String name) {
		super(name);
	}

	@Override
	public Object invoke(Object obj, Object... args) throws IOException {
		// TODO is there a better way to do this?
		MethodInstance mi = Reflector.getMethodInstance(getDeclaringClass(), KeyImpl.init(getName()), args);
		try {
			return mi.invoke(obj);
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}
}
