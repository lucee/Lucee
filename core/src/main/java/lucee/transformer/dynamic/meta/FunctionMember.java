package lucee.transformer.dynamic.meta;

import java.io.Serializable;

import org.objectweb.asm.Type;

public interface FunctionMember extends Serializable {

	public abstract String getName();

	public abstract String getDeclaringClassName();

	public abstract Class getDeclaringClass();

	public abstract boolean isPublic();

	public abstract boolean isProtected();

	public abstract boolean isPrivate();

	public abstract boolean isDefault();

	public abstract boolean isStatic();

	public abstract boolean isAbstract();

	public abstract boolean isFinal();

	public abstract boolean isNative();

	public abstract String[] getArguments();

	public abstract int getArgumentCount();

	public abstract Class[] getArgumentClasses();

	public abstract Type[] getArgumentTypes();

	public abstract String[] getExceptions();

	public abstract boolean inInterface();
}
