package lucee.transformer.dynamic.meta.dynamic;

import java.util.function.BiFunction;

import org.objectweb.asm.Type;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;

class ConstructorDynamic extends FunctionMemberDynamic implements Constructor {

	private static final long serialVersionUID = 1788921105081153549L;
	private java.lang.reflect.Constructor constructor;
	public static boolean USE_DYN_CLASS_CREATION = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.dynamic.invocation", null), false);

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
				sb.append(del).append(ASMUtil.getClassName(t));
				del = ",";
			}
		}

		sb.append(")");

		// throws
		if (expTypes != null && expTypes.length > 0) {
			String del = "";
			sb.append(" throws ");
			for (Type t: expTypes) {
				sb.append(del).append(ASMUtil.getClassName(t));
				del = ",";
			}
		}

		return sb.toString();
	}

	@Override
	public Object newInstance(Object... args) throws Exception {
		if (USE_DYN_CLASS_CREATION) {
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			Clazz clazzz = di.toClazz(getDeclaringClass());
			return ((BiFunction<Object, Object[], Object>) di.getInstance(clazzz, this, args)).apply(null, args);

		}

		if (constructor == null) {
			constructor = getDeclaringClass().getConstructor(getArgumentClasses());
		}
		return constructor.newInstance(args);

	}

}
