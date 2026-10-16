package lucee.transformer.dynamic.meta.reflection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import lucee.commons.io.log.Log;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.Method;

public final class ClazzReflection extends Clazz {

	private static final long serialVersionUID = -9046348146944695783L;

	private String id;

	public ClazzReflection(Class clazz, Log log) {
		super(clazz, log);
	}

	@Override
	public Class getDeclaringClass() {
		return clazz;
	}

	@Override
	public Type getDeclaringType() {
		return Type.getType(this.clazz);
	}

	@Override
	public String id() {
		if (id == null) {
			id = clazz.getClassLoader() + ":" + clazz.getName();
		}
		return id;
	}

	@Override
	public List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException {
		List<Method> list = new ArrayList<>();
		for (java.lang.reflect.Method m: clazz.getMethods()) {
			if ((argumentLength < 0 || argumentLength == m.getParameterCount())
					&& (methodName == null || (nameCaseSensitive ? methodName.equals(m.getName()) : methodName.equalsIgnoreCase(m.getName()))))
				list.add(new MethodReflection(m));
		}
		return list;
	}

	@Override
	public List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException {
		List<Method> list = new ArrayList<>();
		for (java.lang.reflect.Method m: clazz.getDeclaredMethods()) {
			if ((argumentLength < 0 || argumentLength == m.getParameterCount())
					&& (methodName == null || (nameCaseSensitive ? methodName.equals(m.getName()) : methodName.equalsIgnoreCase(m.getName()))))
				list.add(new MethodReflection(m));
		}
		return list;
	}

	@Override
	public List<Constructor> getConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new ArrayList<>();
		for (java.lang.reflect.Constructor c: clazz.getConstructors()) {
			if ((argumentLength < 0 || argumentLength == c.getParameterCount())) list.add(new ConstructorReflection(c));
		}
		return list;
	}

	@Override
	public List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new ArrayList<>();
		for (java.lang.reflect.Constructor c: clazz.getDeclaredConstructors()) {
			if ((argumentLength < 0 || argumentLength == c.getParameterCount())) list.add(new ConstructorReflection(c));
		}
		return list;
	}

	@Override
	public Method getDeclaredMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException {
		if (!nameCaseSensitive) {
			throw new IOException("not supported yet!"); // TODO
		} // (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))
		return new MethodReflection(clazz.getDeclaredMethod(methodName, arguments));

	}

	@Override
	public Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException {
		if (!nameCaseSensitive) {
			throw new IOException("not supported yet!"); // TODO
		}
		return new MethodReflection(clazz.getMethod(methodName, arguments));
	}

	@Override
	public Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive, Method defaultValue) {
		if (!nameCaseSensitive) {
			throw new RuntimeException("not supported yet!"); // TODO
		}
		try {
			return new MethodReflection(clazz.getMethod(methodName, arguments));
		}
		catch (NoSuchMethodException e) {
			return defaultValue;
		}
	}

	@Override
	public Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		return new ConstructorReflection(clazz.getConstructor(arguments));
	}

	@Override
	public Constructor[] getConstructors() {
		java.lang.reflect.Constructor[] src = clazz.getConstructors();
		if (src == null || src.length == 0) return new ConstructorReflection[] {};

		ConstructorReflection[] trg = new ConstructorReflection[src.length];
		for (int i = 0; i < src.length; i++) {
			trg[i] = new ConstructorReflection(src[i]);
		}

		return trg;
	}

	@Override
	public Method[] getMethods() {
		java.lang.reflect.Method[] src = clazz.getMethods();
		if (src == null || src.length == 0) return new MethodReflection[] {};

		MethodReflection[] trg = new MethodReflection[src.length];
		for (int i = 0; i < src.length; i++) {
			trg[i] = new MethodReflection(src[i]);
		}
		return trg;
	}

	@Override
	public Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		return new ConstructorReflection(clazz.getDeclaredConstructor(arguments));

	}
}
