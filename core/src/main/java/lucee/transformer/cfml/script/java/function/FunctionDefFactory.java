package lucee.transformer.cfml.script.java.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.w3c.dom.Node;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.type.Array;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.TimeSpan;
import lucee.transformer.bytecode.statement.Argument;
import lucee.transformer.cfml.script.java.JavaSourceException;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.literal.LitString;

public class FunctionDefFactory {
	private static Map<Class<?>, JavaFunctionDef[]> map = new HashMap<>();

	static {
		// void
		map.put(void.class,
				new JavaFunctionDef[] { new JavaFunctionDef(Consumer.class, "accept", new Class[] { Object.class }, void.class),
						new JavaFunctionDef(DoubleConsumer.class, "accept", new Class[] { double.class }, void.class),
						new JavaFunctionDef(IntConsumer.class, "accept", new Class[] { int.class }, void.class),
						new JavaFunctionDef(LongConsumer.class, "accept", new Class[] { long.class }, void.class),
						new JavaFunctionDef(ObjDoubleConsumer.class, "accept", new Class[] { Object.class, double.class }, void.class),
						new JavaFunctionDef(BiConsumer.class, "accept", new Class[] { Object.class, Object.class }, void.class),
						new JavaFunctionDef(ObjIntConsumer.class, "accept", new Class[] { Object.class, int.class }, void.class),
						new JavaFunctionDef(ObjLongConsumer.class, "accept", new Class[] { Object.class, long.class }, void.class)

				});
		// boolean
		map.put(boolean.class,
				new JavaFunctionDef[] { new JavaFunctionDef(BooleanSupplier.class, "getAsBoolean", new Class[] {}, boolean.class),
						new JavaFunctionDef(DoublePredicate.class, "test", new Class[] { double.class }, boolean.class),
						new JavaFunctionDef(IntPredicate.class, "test", new Class[] { int.class }, boolean.class),
						new JavaFunctionDef(LongPredicate.class, "test", new Class[] { long.class }, boolean.class),
						new JavaFunctionDef(Predicate.class, "test", new Class[] { Object.class }, boolean.class),
						new JavaFunctionDef(BiPredicate.class, "test", new Class[] { Object.class, Object.class }, boolean.class)

				});
		// double
		map.put(double.class,
				new JavaFunctionDef[] { new JavaFunctionDef(IntToDoubleFunction.class, "applyAsDouble", new Class[] { int.class }, double.class),
						new JavaFunctionDef(DoubleSupplier.class, "getAsDouble", new Class[] {}, double.class),
						new JavaFunctionDef(DoubleBinaryOperator.class, "applyAsDouble", new Class[] { double.class, double.class }, double.class),
						new JavaFunctionDef(DoubleUnaryOperator.class, "applyAsDouble", new Class[] { double.class }, double.class),
						new JavaFunctionDef(LongToDoubleFunction.class, "applyAsDouble", new Class[] { long.class }, double.class),
						new JavaFunctionDef(ToDoubleBiFunction.class, "applyAsDouble", new Class[] { Object.class, Object.class }, double.class),
						new JavaFunctionDef(ToDoubleFunction.class, "applyAsDouble", new Class[] { Object.class }, double.class)

				});
		// int
		map.put(int.class,
				new JavaFunctionDef[] { new JavaFunctionDef(IntSupplier.class, "getAsInt", new Class[] {}, int.class),
						new JavaFunctionDef(DoubleToIntFunction.class, "applyAsInt", new Class[] { double.class }, int.class),
						new JavaFunctionDef(IntBinaryOperator.class, "applyAsInt", new Class[] { int.class, int.class }, int.class),
						new JavaFunctionDef(IntUnaryOperator.class, "applyAsInt", new Class[] { int.class }, int.class),
						new JavaFunctionDef(LongToIntFunction.class, "applyAsInt", new Class[] { long.class }, int.class),
						new JavaFunctionDef(ToIntBiFunction.class, "applyAsInt", new Class[] { Object.class, Object.class }, int.class),
						new JavaFunctionDef(ToIntFunction.class, "applyAsInt", new Class[] { Object.class }, int.class)

				});
		// long
		map.put(long.class,
				new JavaFunctionDef[] { new JavaFunctionDef(LongSupplier.class, "getAsLong", new Class[] {}, long.class),
						new JavaFunctionDef(DoubleToLongFunction.class, "applyAsLong", new Class[] { double.class }, long.class),
						new JavaFunctionDef(IntToLongFunction.class, "applyAsLong", new Class[] { int.class }, long.class),
						new JavaFunctionDef(LongBinaryOperator.class, "applyAsLong", new Class[] { long.class, long.class }, long.class),
						new JavaFunctionDef(LongUnaryOperator.class, "applyAsLong", new Class[] { long.class }, long.class),
						new JavaFunctionDef(ToLongBiFunction.class, "applyAsLong", new Class[] { Object.class, Object.class }, long.class),
						new JavaFunctionDef(ToLongFunction.class, "applyAsLong", new Class[] { Object.class }, long.class)

				});
		// Object
		map.put(Object.class,
				new JavaFunctionDef[] { new JavaFunctionDef(Function.class, "apply", new Class[] { Object.class }, Object.class),
						new JavaFunctionDef(DoubleFunction.class, "apply", new Class[] { double.class }, Object.class),
						new JavaFunctionDef(BiFunction.class, "apply", new Class[] { Object.class, Object.class }, Object.class),
						new JavaFunctionDef(IntFunction.class, "apply", new Class[] { int.class }, Object.class),
						new JavaFunctionDef(LongFunction.class, "apply", new Class[] { long.class }, Object.class),
						new JavaFunctionDef(Supplier.class, "get", new Class[] {}, Object.class) });

	}

	public static FunctionDef getFunctionDef(ArrayList<Argument> argList, String strRtnType) throws JavaSourceException, ClassException {
		if (StringUtil.isEmpty(strRtnType)) {
			throw new JavaSourceException("you need to define a return type for java type functions, possible values are [void,boolean,double,int,long,Object]");
		}
		// convert type definition to classes
		Class<?> rtnType = toClass(strRtnType);
		Class<?>[] argTypes = new Class<?>[argList.size()];
		Iterator<Argument> it = argList.iterator();
		{
			int i = 0;
			while (it.hasNext()) {
				argTypes[i++] = toClass(it.next().getType());
			}
		}

		// Java Function
		JavaFunctionDef[] jcs = map.get(rtnType);
		if (jcs != null) {
			Class<?>[] args;
			outer: for (JavaFunctionDef jc: jcs) {
				args = jc.getArgs();
				if (argList.size() != args.length) continue outer;
				for (int i = 0; i < args.length; i++) {
					if (!argTypes[i].equals(args[i])) continue outer;
				}
				return jc;
			}

			// create exception
			/*
			 * StringBuilder sb = new StringBuilder(); for (JavaFunctionDef jc: jcs) { if (sb.length() > 0)
			 * sb.append(','); sb.append(jc.toStringShort()); } throw new JavaSourceException(
			 * "found no matching function interface with return type [" + strRtnType +
			 * "], for the arguments defined, valid argument combinations are [" + sb + "]");
			 */
		}
		return new JavaFunctionDef(null, "invoke", argTypes, rtnType, true);

	}

	private static Class<?> toClass(ExprString type) throws JavaSourceException, ClassException {
		if (type instanceof LitString) return toClass(((LitString) type).getString());
		throw new JavaSourceException("type definition must be literal");
	}

	private static Class<?> toClass(String type) throws ClassException {
		type = type.trim();
		if ("void".equals(type)) return void.class;
		if ("double".equals(type)) return double.class;
		if ("int".equals(type)) return int.class;
		if ("long".equals(type)) return long.class;
		if ("short".equals(type)) return short.class;
		if ("char".equals(type)) return char.class;
		if ("byte".equals(type)) return byte.class;
		if ("boolean".equals(type)) return boolean.class;
		if ("float".equals(type)) return float.class;

		if ("Void".equals(type)) return void.class;
		if ("Double".equals(type)) return Double.class;
		if ("Integer".equals(type)) return Integer.class;
		if ("Long".equals(type)) return Long.class;
		if ("Short".equals(type)) return Short.class;
		if ("Character".equals(type)) return Character.class;
		if ("Byte".equals(type)) return Byte.class;
		if ("Boolean".equals(type)) return Boolean.class;
		if ("Float".equals(type)) return Float.class;

		if ("Object".equalsIgnoreCase(type)) return Object.class;

		String lcType = StringUtil.toLowerCase(type);
		if (lcType.length() > 2) {
			char first = lcType.charAt(0);
			switch (first) {
			case 'a':
				if (lcType.equals("any")) {
					return Object.class;
				}
				else if (lcType.equals("array")) {
					return Array.class;
				}
				break;
			case 'b':
				if (lcType.equals("binary")) {
					return byte[].class;
				}
				else if (lcType.equals("base64")) {
					return String.class;
				}
				break;
			case 'c':
				if (lcType.equals("component")) {
					return Component.class;
				}
				break;
			case 'd':
				if (lcType.equals("date")) {
					return Date.class;
				}
				else if (lcType.equals("datetime")) {
					return Date.class;
				}
				break;
			case 'n':
				if (lcType.equals("numeric")) {
					return Double.class;
				}
				else if (lcType.equals("number")) {
					return Double.class;
				}
				else if (lcType.equals("node")) {
					return Node.class;
				}
				break;
			case 'o':
				if (lcType.equals("object")) {
					return Object.class;
				}
				break;
			case 'q':
				if (lcType.equals("query")) {
					return Query.class;
				}
				break;
			case 's':
				if (lcType.equals("string")) {
					return String.class;
				}
				else if (lcType.equals("struct")) {
					return Struct.class;
				}
				break;
			case 't':
				if (lcType.equals("timespan")) {
					return TimeSpan.class;
				}
				break;
			case 'x':
				if (lcType.equals("xml")) {
					return Node.class;
				}
				break;
			}
		}

		// array
		/*
		 * if (type.endsWith("[]")) { Class clazz = toClass(type.substring(0, type.length() - 2)); clazz =
		 * ClassUtil.toArrayClass(clazz); return clazz; }
		 */

		return ClassUtil.loadClass(CFMLEngineFactory.class.getClassLoader(), type);
	}

	private static Class<?> toJavaFunctionType(Class<?> clazz, Class<?> defaultValue) {
		if (void.class == clazz) return void.class;
		if (double.class == clazz) return double.class;
		if (Double.class == clazz) return double.class;
		if (int.class == clazz) return int.class;
		if (Integer.class == clazz) return int.class;
		if (long.class == clazz) return long.class;
		if (Long.class == clazz) return long.class;
		if (boolean.class == clazz) return boolean.class;
		if (Boolean.class == clazz) return boolean.class;
		if (Object.class == clazz) return Object.class;

		return defaultValue;
		// throw new JavaSourceException("invalid type definition [" + clazz.getName() + "],valid types are
		// [void,boolean,double,int,long,Object]");
	}
}
