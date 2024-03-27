package lucee.transformer.dynamic.meta.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.objectweb.asm.Type;

import lucee.transformer.dynamic.meta.FunctionMember;

public abstract class FunctionMemberReflection implements FunctionMember {
	private static final long serialVersionUID = 6812458458981023205L;
	private Executable executable;

	public FunctionMemberReflection(Executable method) {
		this.executable = method;
	}

	@Override
	public String getName() {
		return executable.getName();
	}

	@Override
	public String getDeclaringClassName() {
		return executable.getDeclaringClass().getName();
	}

	@Override
	public Class getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	@Override
	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	@Override
	public boolean isDefault() {
		return !Modifier.isPublic(executable.getModifiers()) && !Modifier.isProtected(executable.getModifiers()) && !Modifier.isPrivate(executable.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	@Override
	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	@Override
	public boolean inInterface() {
		return executable.getDeclaringClass().isInterface();
	}

	@Override
	public String[] getArguments() {
		Parameter[] params = executable.getParameters();
		String[] arguments = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			arguments[i] = params[i].getType().getName();
		}
		return arguments;
	}

	@Override
	public Type[] getArgumentTypes() {
		Parameter[] params = executable.getParameters();
		Type[] arguments = new Type[params.length];
		for (int i = 0; i < params.length; i++) {
			arguments[i] = Type.getType(params[i].getType());
		}
		return arguments;
	}

	@Override
	public Class[] getArgumentClasses() {
		Parameter[] params = executable.getParameters();
		Class[] arguments = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			arguments[i] = params[i].getType();
		}
		return arguments;
	}

	@Override
	public String[] getExceptions() {
		Class<?>[] exceptions = executable.getExceptionTypes();
		String[] arguments = new String[exceptions.length];
		for (int i = 0; i < exceptions.length; i++) {
			arguments[i] = exceptions[i].getName();
		}
		return arguments;
	}

	@Override
	public String toString() {
		return executable.toString();
	}
}
