package lucee.transformer.dynamic.meta.dynamic;

import java.io.IOException;

import org.objectweb.asm.Type;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;

class ConstructorDynamic extends FunctionMemberDynamic implements Constructor {

	private static final long serialVersionUID = 1788921105081153549L;

	public ConstructorDynamic() {
		super("<init>");
	}

	@Override
	public String toString() {
		// public java.lang.String java.lang.String.toString()
		StringBuilder sb = new StringBuilder("");

		// access modifier
		String am = Clazz.getAccessModifierAsString(this);
		if (!StringUtil.isEmpty(am)) sb.append(am).append(' ');

		// final
		if (isFinal()) sb.append("final ");

		// native
		if (isNative()) sb.append("native ");

		// static
		if (isStatic()) sb.append("static ");

		// declaring class
		sb.append(getDeclaringClassName());

		sb.append('(');

		// arguments;
		if (argTypes != null && argTypes.length > 0) {
			String del = "";
			for (Type t: argTypes) {
				sb.append(del).append(t.getClassName());
				del = ",";
			}
		}

		sb.append(")");

		// throws
		if (expTypes != null && expTypes.length > 0) {
			String del = "";
			sb.append(" throws ");
			for (Type t: expTypes) {
				sb.append(del).append(t.getClassName());
				del = ",";
			}
		}

		return sb.toString();
	}

	@Override
	public Object newInstance(Object... args) throws IOException {
		ConstructorInstance ci = Reflector.getConstructorInstance(getDeclaringClass(), args);
		try {
			return ci.invoke();
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}
}
