package lucee.runtime.util;

import lucee.runtime.Component;
import lucee.runtime.config.ConfigWeb;

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
public interface JavaProxyUtil {

	public Object call(ConfigWeb config, Component cfc, String methodName, Object... arguments);

	public boolean toBoolean(Object obj);

	public float toFloat(Object obj);

	public int toInt(Object obj);

	public double toDouble(Object obj);

	public long toLong(Object obj);

	public char toChar(Object obj);

	public byte toByte(Object obj);

	public short toShort(Object obj);

	public String toString(Object obj);

	public Object to(Object obj, Class<?> clazz);

	public Object to(Object obj, String className);

	public Object toCFML(boolean value);

	public Object toCFML(byte value);

	public Object toCFML(char value);

	public Object toCFML(double value);

	public Object toCFML(float value);

	public Object toCFML(int value);

	public Object toCFML(long value);

	public Object toCFML(short value);

	public Object toCFML(Object value);
}