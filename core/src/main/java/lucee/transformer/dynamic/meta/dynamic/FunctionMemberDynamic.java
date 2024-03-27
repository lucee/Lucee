package lucee.transformer.dynamic.meta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageRuntimeException;

public abstract class FunctionMemberDynamic implements FunctionMember {

	private static final long serialVersionUID = 4596750766856217930L;

	protected String name;
	protected int access;
	protected transient Type declaringType;
	protected transient Class declaringClass;
	protected transient Type rtnType;
	protected transient Type[] argTypes;
	protected transient Class[] argClasses;
	protected transient Type[] expTypes;
	protected transient Class[] expClasses;

	public FunctionMemberDynamic(String name) {
		this.name = name;
	}

	// loaded by object evaluation
	public FunctionMemberDynamic() {

	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.writeObject(name);
		out.writeInt(access);

		// declaring class
		out.writeObject(declaringType.getDescriptor());
		// return
		out.writeObject(rtnType.getDescriptor());

		// arguments
		String[] arr = new String[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			arr[i] = argTypes[i].getDescriptor();
		}
		out.writeObject(arr);

		// exception
		arr = new String[expTypes.length];
		for (int i = 0; i < expTypes.length; i++) {
			arr[i] = expTypes[i].getDescriptor();
		}
		out.writeObject(arr);

	}

	// protected transient Type declaringType;
	// protected transient Type rtnType;
	// protected transient Type[] argTypes;
	// protected transient Type[] expTypes;
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();

		// name
		this.name = (String) in.readObject();
		// access
		this.access = in.readInt();

		// declaring class
		this.declaringType = Type.getType((String) in.readObject());
		// declaring class
		this.rtnType = Type.getType((String) in.readObject());

		// arguments
		String[] arr = (String[]) in.readObject();
		this.argTypes = new Type[arr.length];
		for (int i = 0; i < argTypes.length; i++) {
			argTypes[i] = Type.getType(arr[i]);
		}

		// exception
		arr = (String[]) in.readObject();
		this.expTypes = new Type[arr.length];
		for (int i = 0; i < expTypes.length; i++) {
			expTypes[i] = Type.getType(arr[i]);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public static FunctionMember createInstance(Class declaringClass, String name, int access, String descriptor, String[] exceptions) {
		FunctionMemberDynamic fm;
		if ("<init>".equals(name)) {
			fm = new ConstructorDynamic();
		}
		else {
			fm = new MethodDynamic(declaringClass, name);
		}

		// declaring class
		fm.declaringClass = declaringClass;
		fm.declaringType = Type.getType(declaringClass);

		// return
		fm.rtnType = Type.getReturnType(descriptor);

		// arguments
		fm.argTypes = Type.getArgumentTypes(descriptor);
		if (fm.argTypes == null) fm.argTypes = new Type[0];

		// exceptions

		if (exceptions != null) {
			fm.expTypes = new Type[exceptions.length];
			for (int i = 0; i < exceptions.length; i++) {
				fm.expTypes[i] = Type.getObjectType(exceptions[i]);
			}
		}
		else {
			fm.expTypes = new Type[0];
		}

		// access
		fm.access = access;

		return fm;
	}

	@Override
	public String getDeclaringClassName() {
		return declaringType.getClassName();
	}

	@Override
	public Class getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public boolean isPublic() {
		return (access & Opcodes.ACC_PUBLIC) != 0;
	}

	@Override
	public boolean isProtected() {
		return (access & Opcodes.ACC_PROTECTED) != 0;
	}

	@Override
	public boolean isPrivate() {
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}

	@Override
	public boolean isDefault() {
		return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0;
		// return !(isPublic() || isProtected() || isPrivate());
	}

	@Override
	public boolean isStatic() {
		return (access & Opcodes.ACC_STATIC) != 0;
	}

	@Override
	public boolean isAbstract() {
		return (access & Opcodes.ACC_ABSTRACT) != 0;
	}

	@Override
	public boolean isFinal() {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	@Override
	public boolean isNative() {
		return (access & Opcodes.ACC_NATIVE) != 0;
	}

	public String getReturn() {
		return rtnType.getClassName();
	}

	public Type getReturnType() {
		return rtnType;
	}

	public Class getReturnClass() {
		try {
			return Clazz.toClass(Clazz.getClassLoader(this.declaringClass), rtnType);
		}
		catch (ClassException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public String[] getArguments() {
		String[] arguments = new String[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			arguments[i] = argTypes[i].getClassName();
		}
		return arguments;
	}

	@Override
	public Class[] getArgumentClasses() {
		if (argClasses == null) {
			ClassLoader cl = Clazz.getClassLoader(this.declaringClass);
			argClasses = new Class[argTypes.length];
			for (int i = 0; i < argTypes.length; i++) {
				try {

					argClasses[i] = Clazz.toClass(cl, argTypes[i]);
				}
				catch (ClassException e) {
					throw new PageRuntimeException(e);
				}
			}
		}
		return argClasses;
	}

	@Override
	public Type[] getArgumentTypes() {
		return argTypes;
	}

	@Override
	public String[] getExceptions() {
		String[] exceptions = new String[expTypes.length];
		for (int i = 0; i < expTypes.length; i++) {
			exceptions[i] = expTypes[i].getClassName();
		}
		return exceptions;
	}

	public Type[] getExceptionTypes() {
		return expTypes;
	}

	@Override
	public String toString() {
		// public java.lang.String java.lang.String.toString()
		StringBuilder sb = new StringBuilder();

		// access modifier
		String am = Clazz.getAccessModifierAsString(this);
		if (!StringUtil.isEmpty(am)) sb.append(am).append(' ');

		// final
		if (isFinal()) sb.append("final ");
		else if (isAbstract()) sb.append("abstract ");

		// native
		if (isNative()) sb.append("native ");

		// static
		if (isStatic()) sb.append("static ");

		// return type
		sb.append(getReturnType().getClassName()).append(' ');

		// declaring class
		sb.append(getDeclaringClassName()).append('.');

		// name
		sb.append(name)

				.append('(');

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

}
