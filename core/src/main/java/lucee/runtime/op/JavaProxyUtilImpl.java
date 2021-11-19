package lucee.runtime.op;

import lucee.runtime.Component;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.java.JavaProxy;
import lucee.runtime.util.JavaProxyUtil;

public class JavaProxyUtilImpl implements JavaProxyUtil {

	@Override
	public Object call(ConfigWeb config, Component cfc, String methodName, Object... arguments) {
		return JavaProxy.call(config, cfc, methodName, arguments);
	}

	@Override
	public boolean toBoolean(Object obj) {
		return JavaProxy.toBoolean(obj);
	}

	@Override
	public float toFloat(Object obj) {
		return JavaProxy.toFloat(obj);
	}

	@Override
	public int toInt(Object obj) {
		return JavaProxy.toInt(obj);
	}

	@Override
	public double toDouble(Object obj) {
		return JavaProxy.toDouble(obj);
	}

	@Override
	public long toLong(Object obj) {
		return JavaProxy.toLong(obj);
	}

	@Override
	public char toChar(Object obj) {
		return JavaProxy.toChar(obj);
	}

	@Override
	public byte toByte(Object obj) {
		return JavaProxy.toByte(obj);
	}

	@Override
	public short toShort(Object obj) {
		return JavaProxy.toShort(obj);
	}

	public String toString(Object obj) {
		return JavaProxy.toString(obj);
	}

	@Override
	public Object to(Object obj, Class<?> clazz) {
		return JavaProxy.to(obj, clazz);
	}

	public Object to(Object obj, String className) {
		return JavaProxy.to(obj, className);
	}

	@Override
	public Object toCFML(boolean value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(byte value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(char value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(double value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(float value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(int value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(long value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(short value) {
		return JavaProxy.toCFML(value);
	}

	@Override
	public Object toCFML(Object value) {
		return JavaProxy.toCFML(value);
	}

}
