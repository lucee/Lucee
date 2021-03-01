/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.type.util;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.ArrayUtilException;
import lucee.commons.lang.ComparatorUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.math.MathUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Operator;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayClassic;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.ArrayTyped;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.comparator.SortRegister;

/**
 * Util for diffrent methods to manipulate arrays
 */
public final class ArrayUtil {

	public static final Object[] OBJECT_EMPTY = new Object[] {};

	public static Array getInstance(int dimension) throws ExpressionException {
		return getInstance(dimension, false);
	}

	public static Array getInstance(int dimension, boolean _synchronized) throws ExpressionException {
		if (dimension > 1) return new ArrayClassic(dimension);
		return new ArrayImpl(ArrayImpl.DEFAULT_CAP, _synchronized);
	}

	/**
	 * trims all value of a String Array
	 * 
	 * @param arr
	 * @return trimmed array
	 */
	public static String[] trim(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/**
	 * @param list
	 * @return array
	 */
	public static SortRegister[] toSortRegisterArray(ArrayList list) {
		SortRegister[] arr = new SortRegister[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new SortRegister(i, list.get(i));
		}
		return arr;
	}

	/**
	 * @param column
	 * @return array
	 */
	public static SortRegister[] toSortRegisterArray(QueryColumn column) {
		SortRegister[] arr = new SortRegister[column.size()];
		int type = column.getType();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new SortRegister(i, toSortRegisterArray(column.get(i + 1, null), type));
		}
		return arr;
	}

	private static Object toSortRegisterArray(Object value, int type) {

		Object mod = null;
		// Date
		if (Types.TIMESTAMP == type) {
			mod = Caster.toDate(value, true, null, null);
		}
		// Double
		else if (Types.DOUBLE == type) {
			mod = Caster.toDouble(value, null);
		}
		// Boolean
		else if (Types.BOOLEAN == type) {
			mod = Caster.toBoolean(value, null);
		}
		// Varchar
		else if (Types.VARCHAR == type) {
			mod = Caster.toString(value, null);
		}
		else return value;

		if (mod != null) return mod;
		return value;
	}

	/**
	 * swap to values of the array
	 * 
	 * @param array
	 * @param left left value to swap
	 * @param right right value to swap
	 * @throws ExpressionException
	 */
	public static void swap(Array array, int left, int right) throws ExpressionException {
		int len = array.size();

		if (len == 0) throw new ExpressionException("array is empty");
		if (left < 1 || left > len) throw new ExpressionException("invalid index [" + left + "]", "valid indexes are from 1 to " + len);
		if (right < 1 || right > len) throw new ExpressionException("invalid index [" + right + "]", "valid indexes are from 1 to " + len);

		try {
			Object leftValue = array.get(left, null);
			Object rightValue = array.get(right, null);

			array.setE(left, rightValue);
			array.setE(right, leftValue);
		}
		catch (PageException e) {
			throw new ExpressionException("can't swap values of array", e.getMessage());
		}

	}

	/**
	 * find an object in array
	 * 
	 * @param array
	 * @param object object to find
	 * @return position in array or 0
	 */
	public static int find(Array array, Object object) {
		int len = array.size();
		for (int i = 1; i <= len; i++) {
			Object tmp = array.get(i, null);
			try {
				if (tmp != null && Operator.compare(object, tmp) == 0) return i;
			}
			catch (PageException e) {}
		}
		return 0;
	}

	/**
	 * average of all values of the array, only work when all values are numeric
	 * 
	 * @param array
	 * @return average of all values
	 * @throws ExpressionException
	 */
	public static double avg(Array array) throws ExpressionException {
		if (array.size() == 0) return 0;
		return sum(array) / array.size();
	}

	/**
	 * sum of all values of an array, only work when all values are numeric
	 * 
	 * @param array Array
	 * @return sum of all values
	 * @throws ExpressionException
	 */
	public static double sum(Array array) throws ExpressionException {
		if (array.getDimension() > 1) throw new ExpressionException("can only get sum/avg from 1 dimensional arrays");

		double rtn = 0;
		int len = array.size();
		// try {
		for (int i = 1; i <= len; i++) {
			rtn += _toDoubleValue(array, i);
		}
		/*
		 * } catch (PageException e) { throw new
		 * ExpressionException("exception while execute array operation: "+e.getMessage()); }
		 */
		return rtn;
	}

	/**
	 * median value of all items in the arrays, only works when all values are numeric
	 *
	 * @param array
	 * @return
	 * @throws ExpressionException
	 */
	public static double median(Array array) throws ExpressionException {

		int len = array.size();

		if (len == 0) return 0;

		if (array.getDimension() > 1) throw new ExpressionException("Median() can only be calculated for one dimensional arrays");

		double[] arr = new double[len];

		for (int i = 0; i < len; i++)
			arr[i] = _toDoubleValue(array, i + 1);

		Arrays.sort(arr);

		double result = arr[len / 2];

		if (len % 2 == 0) {

			return (result + arr[(len - 2) / 2]) / 2;
		}

		return result;
	}

	private static double _toDoubleValue(Array array, int i) throws ExpressionException {
		Object obj = array.get(i, null);
		if (obj == null) throw new ExpressionException("there is no element at position [" + i + "] or the element is null");
		double tmp = Caster.toDoubleValue(obj, true, Double.NaN);
		if (Double.isNaN(tmp)) throw new CasterException(obj, Double.class);
		return tmp;
	}

	/**
	 * the smallest value, of all values inside the array, only work when all values are numeric
	 * 
	 * @param array
	 * @return the smallest value
	 * @throws PageException
	 */
	public static double min(Array array) throws PageException {
		if (array.getDimension() > 1) throw new ExpressionException("can only get max value from 1 dimensional arrays");
		if (array.size() == 0) return 0;

		double rtn = _toDoubleValue(array, 1);
		int len = array.size();
		try {
			for (int i = 2; i <= len; i++) {
				double v = _toDoubleValue(array, i);
				if (rtn > v) rtn = v;

			}
		}
		catch (PageException e) {
			throw new ExpressionException("exception while execute array operation: " + e.getMessage());
		}
		return rtn;
	}

	/**
	 * the greatest value, of all values inside the array, only work when all values are numeric
	 * 
	 * @param array
	 * @return the greatest value
	 * @throws PageException
	 */
	public static double max(Array array) throws PageException {
		if (array.getDimension() > 1) throw new ExpressionException("can only get max value from 1 dimensional arrays");
		if (array.size() == 0) return 0;

		double rtn = _toDoubleValue(array, 1);
		int len = array.size();
		try {
			for (int i = 2; i <= len; i++) {
				double v = _toDoubleValue(array, i);
				if (rtn < v) rtn = v;

			}
		}
		catch (PageException e) {
			throw new ExpressionException("exception while execute array operation: " + e.getMessage());
		}
		return rtn;
	}

	/**
	 * return index of given value in Array or -1
	 * 
	 * @param arr
	 * @param value
	 * @return index of position in array
	 */
	public static int indexOf(String[] arr, String value) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(value)) return i;
		}
		return -1;
	}

	/**
	 * return index of given value in Array or -1
	 * 
	 * @param arr
	 * @param value
	 * @return index of position in array
	 */
	public static int indexOfIgnoreCase(String[] arr, String value) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equalsIgnoreCase(value)) return i;
		}
		return -1;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Boolean[] toReferenceType(boolean[] primArr) {
		Boolean[] refArr = new Boolean[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = Caster.toBoolean(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Byte[] toReferenceType(byte[] primArr) {
		Byte[] refArr = new Byte[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = new Byte(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Character[] toReferenceType(char[] primArr) {
		Character[] refArr = new Character[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = new Character(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Short[] toReferenceType(short[] primArr) {
		Short[] refArr = new Short[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = Short.valueOf(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Integer[] toReferenceType(int[] primArr) {
		Integer[] refArr = new Integer[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = Integer.valueOf(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Long[] toReferenceType(long[] primArr) {
		Long[] refArr = new Long[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = Long.valueOf(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Float[] toReferenceType(float[] primArr) {
		Float[] refArr = new Float[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = new Float(primArr[i]);
		return refArr;
	}

	/**
	 * convert a primitive array (value type) to Object Array (reference type).
	 * 
	 * @param primArr value type Array
	 * @return reference type Array
	 */
	public static Double[] toReferenceType(double[] primArr) {
		Double[] refArr = new Double[primArr.length];
		for (int i = 0; i < primArr.length; i++)
			refArr[i] = new Double(primArr[i]);
		return refArr;
	}

	/**
	 * gets a value of an array at defined index
	 * 
	 * @param o
	 * @param index
	 * @return value at index position
	 * @throws ArrayUtilException
	 */
	public static Object get(Object o, int index) throws ArrayUtilException {
		o = get(o, index, null);
		if (o != null) return o;
		throw new ArrayUtilException("Object is not an array, or index is invalid");
	}

	/**
	 * gets a value of an array at defined index
	 * 
	 * @param o
	 * @param index
	 * @return value of the variable
	 */
	public static Object get(Object o, int index, Object defaultValue) {
		if (index < 0) return null;
		if (o instanceof Object[]) {
			Object[] arr = ((Object[]) o);
			if (arr.length > index) return arr[index];
		}
		else if (o instanceof boolean[]) {
			boolean[] arr = ((boolean[]) o);
			if (arr.length > index) return arr[index] ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (o instanceof byte[]) {
			byte[] arr = ((byte[]) o);
			if (arr.length > index) return new Byte(arr[index]);
		}
		else if (o instanceof char[]) {
			char[] arr = ((char[]) o);
			if (arr.length > index) return "" + (arr[index]);
		}
		else if (o instanceof short[]) {
			short[] arr = ((short[]) o);
			if (arr.length > index) return Short.valueOf(arr[index]);
		}
		else if (o instanceof int[]) {
			int[] arr = ((int[]) o);
			if (arr.length > index) return Integer.valueOf(arr[index]);
		}
		else if (o instanceof long[]) {
			long[] arr = ((long[]) o);
			if (arr.length > index) return Long.valueOf(arr[index]);
		}
		else if (o instanceof float[]) {
			float[] arr = ((float[]) o);
			if (arr.length > index) return new Float(arr[index]);
		}
		else if (o instanceof double[]) {
			double[] arr = ((double[]) o);
			if (arr.length > index) return new Double(arr[index]);
		}
		return defaultValue;
	}

	/**
	 * sets a value to an array at defined index
	 * 
	 * @param o
	 * @param index
	 * @param value
	 * @return value setted
	 * @throws ArrayUtilException
	 */
	public static Object set(Object o, int index, Object value) throws ArrayUtilException {
		if (index < 0) throw invalidIndex(index, 0);
		if (o instanceof Object[]) {
			Object[] arr = ((Object[]) o);
			if (arr.length > index) return arr[index] = value;
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof boolean[]) {
			boolean[] arr = ((boolean[]) o);
			if (arr.length > index) {
				arr[index] = Caster.toBooleanValue(value, false);
				return arr[index] ? Boolean.TRUE : Boolean.FALSE;
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof byte[]) {
			byte[] arr = ((byte[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return new Byte(arr[index] = (byte) v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof short[]) {
			short[] arr = ((short[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return Short.valueOf(arr[index] = (short) v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof int[]) {
			int[] arr = ((int[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return Integer.valueOf(arr[index] = (int) v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof long[]) {
			long[] arr = ((long[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return Long.valueOf(arr[index] = (long) v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof float[]) {
			float[] arr = ((float[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return new Float(arr[index] = (float) v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof double[]) {
			double[] arr = ((double[]) o);
			if (arr.length > index) {
				double v = Caster.toDoubleValue(value, true, Double.NaN);
				if (Decision.isValid(v)) {
					return new Double(arr[index] = v);
				}
			}
			throw invalidIndex(index, arr.length);
		}
		else if (o instanceof char[]) {
			char[] arr = ((char[]) o);
			if (arr.length > index) {
				String str = Caster.toString(value, null);
				if (str != null && str.length() > 0) {
					char c = str.charAt(0);
					arr[index] = c;
					return str;
				}
			}
			throw invalidIndex(index, arr.length);
		}
		throw new ArrayUtilException("Object [" + Caster.toClassName(o) + "] is not an Array");
	}

	private static ArrayUtilException invalidIndex(int index, int length) {
		return new ArrayUtilException("Invalid index [" + index + "] for native Array call, Array has a Size of " + length);
	}

	/**
	 * sets a value to an array at defined index
	 * 
	 * @param o
	 * @param index
	 * @param value
	 * @return value setted
	 */
	public static Object setEL(Object o, int index, Object value) {
		try {
			return set(o, index, value);
		}
		catch (ArrayUtilException e) {
			return null;
		}
	}

	public static boolean isEmpty(List list) {
		return list == null || list.isEmpty();
	}

	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(boolean[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(char[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(double[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(long[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(float[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static int size(Object[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(boolean[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(char[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(double[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(long[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(int[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(float[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static int size(byte[] array) {
		if (array == null) return 0;
		return array.length;
	}

	public static boolean[] toBooleanArray(Object obj) throws PageException {
		if (obj instanceof boolean[]) return (boolean[]) obj;

		Array arr = Caster.toArray(obj);
		boolean[] tarr = new boolean[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toBooleanValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static byte[] toByteArray(Object obj) throws PageException {
		if (obj instanceof byte[]) return (byte[]) obj;

		Array arr = Caster.toArray(obj);
		byte[] tarr = new byte[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toByteValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static short[] toShortArray(Object obj) throws PageException {
		if (obj instanceof short[]) return (short[]) obj;

		Array arr = Caster.toArray(obj);
		short[] tarr = new short[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toShortValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static int[] toIntArray(Object obj) throws PageException {
		if (obj instanceof int[]) return (int[]) obj;

		Array arr = Caster.toArray(obj);
		int[] tarr = new int[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toIntValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static Object[] toNullArray(Object obj) throws PageException {
		Array arr = Caster.toArray(obj);
		Object[] tarr = new Object[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toNull(arr.getE(i + 1));
		}
		return tarr;
	}

	public static long[] toLongArray(Object obj) throws PageException {
		if (obj instanceof long[]) return (long[]) obj;

		Array arr = Caster.toArray(obj);
		long[] tarr = new long[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toLongValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static float[] toFloatArray(Object obj) throws PageException {
		if (obj instanceof float[]) return (float[]) obj;

		Array arr = Caster.toArray(obj);
		float[] tarr = new float[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toFloatValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static double[] toDoubleArray(Object obj) throws PageException {
		if (obj instanceof double[]) return (double[]) obj;

		Array arr = Caster.toArray(obj);
		double[] tarr = new double[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toDoubleValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static char[] toCharArray(Object obj) throws PageException {
		if (obj instanceof char[]) return (char[]) obj;

		Array arr = Caster.toArray(obj);
		char[] tarr = new char[arr.size()];
		for (int i = 0; i < tarr.length; i++) {
			tarr[i] = Caster.toCharValue(arr.getE(i + 1));
		}
		return tarr;
	}

	public static int arrayContainsIgnoreEmpty(Array arr, String value, boolean ignoreCase) {
		int count = 0;
		int len = arr.size();

		for (int i = 1; i <= len; i++) {
			String item = Caster.toString(arr.get(i, ""), "");
			if (ignoreCase) {
				if (StringUtil.indexOfIgnoreCase(item, value) != -1) return count;
			}
			else {
				if (item.indexOf(value) != -1) return count;
			}
			count++;
		}
		return -1;
	}

	public static Object[] toReferenceType(Object obj) throws CasterException {
		Object[] ref = toReferenceType(obj, null);
		if (ref != null) return ref;
		throw new CasterException(obj, Object[].class);

	}

	public static Object[] toReferenceType(Object obj, Object[] defaultValue) {
		if (obj instanceof Object[]) return (Object[]) obj;
		else if (obj instanceof boolean[]) return toReferenceType((boolean[]) obj);
		else if (obj instanceof byte[]) return toReferenceType((byte[]) obj);
		else if (obj instanceof char[]) return toReferenceType((char[]) obj);
		else if (obj instanceof short[]) return toReferenceType((short[]) obj);
		else if (obj instanceof int[]) return toReferenceType((int[]) obj);
		else if (obj instanceof long[]) return toReferenceType((long[]) obj);
		else if (obj instanceof float[]) return toReferenceType((float[]) obj);
		else if (obj instanceof double[]) return toReferenceType((double[]) obj);
		return defaultValue;
	}

	public static Object[] clone(Object[] src, Object[] trg) {
		for (int i = 0; i < src.length; i++) {
			trg[i] = src[i];
		}
		return trg;
	}

	public static Object[] keys(Map map) {
		if (map == null) return new Object[0];
		Set set = map.keySet();
		if (set == null) return new Object[0];
		Object[] arr = set.toArray();
		if (arr == null) return new Object[0];
		return arr;
	}

	public static Object[] values(Map map) {
		if (map == null) return new Object[0];
		return map.values().toArray();
	}

	/**
	 * creates a native array out of the input list, if all values are from the same type, this type is
	 * used for the array, otherwise object
	 * 
	 * @param list
	 */
	public static Object[] toArray(List<?> list) {
		Iterator<?> it = list.iterator();
		Class clazz = null;
		while (it.hasNext()) {
			Object v = it.next();
			if (v == null) continue;
			if (clazz == null) clazz = v.getClass();
			else if (clazz != v.getClass()) return list.toArray();
		}
		if (clazz == Object.class || clazz == null) return list.toArray();

		Object arr = java.lang.reflect.Array.newInstance(clazz, list.size());
		return list.toArray((Object[]) arr);
	}

	public static Comparator toComparator(PageContext pc, String strSortType, String sortOrder, boolean localeSensitive) throws PageException {

		// check order
		boolean isAsc = true;
		if (sortOrder.equalsIgnoreCase("asc")) isAsc = true;
		else if (sortOrder.equalsIgnoreCase("desc")) isAsc = false;
		else throw new ExpressionException("invalid sort order type [" + sortOrder + "], sort order types are [asc and desc]");

		// check type
		int sortType;
		if (strSortType.equalsIgnoreCase("text")) sortType = ComparatorUtil.SORT_TYPE_TEXT;
		else if (strSortType.equalsIgnoreCase("textnocase")) sortType = ComparatorUtil.SORT_TYPE_TEXT_NO_CASE;
		else if (strSortType.equalsIgnoreCase("numeric")) sortType = ComparatorUtil.SORT_TYPE_NUMBER;
		else throw new ExpressionException("invalid sort type [" + strSortType + "], sort types are [text, textNoCase, numeric]");

		return ComparatorUtil.toComparator(sortType, isAsc, localeSensitive ? ThreadLocalPageContext.getLocale(pc) : null, null);

	}

	public static <E> List<E> merge(E[] a1, E[] a2) {
		List<E> list = new ArrayList<E>();
		for (int i = 0; i < a1.length; i++) {
			list.add(a1[i]);
		}
		for (int i = 0; i < a2.length; i++) {
			list.add(a2[i]);
		}
		return list;
	}

	/**
	 * this method efficiently copy the contents of one native array into another by using
	 * System.arraycopy()
	 *
	 * @param dst - the array that will be modified
	 * @param src - the data to be copied
	 * @param dstPosition - pass -1 to append to the end of the dst array, or a valid position to add it
	 *            elsewhere
	 * @param doPowerOf2 - if true, and the array needs to be resized, it will be resized to the next
	 *            power of 2 size
	 * @return - either the original dst array if it had enough capacity, or a new array.
	 */
	public static Object[] mergeNativeArrays(Object[] dst, Object[] src, int dstPosition, boolean doPowerOf2) {

		if (dstPosition < 0) dstPosition = dst.length;

		Object[] result = resizeIfNeeded(dst, dstPosition + src.length, doPowerOf2);

		System.arraycopy(src, 0, result, dstPosition, src.length);

		return result;
	}

	/**
	 * this method returns the original array if its length is equal or greater than the minSize, or
	 * create a new array and copies the data from the original array into the new one.
	 *
	 * @param arr - the array to check
	 * @param minSize - the required minimum size
	 * @param doPowerOf2 - if true, and a resize is required, the new size will be a power of 2
	 * @return - either the original arr array if it had enough capacity, or a new array.
	 */
	public static Object[] resizeIfNeeded(Object[] arr, int minSize, boolean doPowerOf2) {

		if (arr.length >= minSize) return arr;

		if (doPowerOf2) minSize = MathUtil.nextPowerOf2(minSize);

		Object[] result = new Object[minSize];
		System.arraycopy(arr, 0, result, 0, arr.length);

		return result;
	}

	public static String[] toArray(String[] arr1, String[] arr2) {
		String[] ret = new String[arr1.length + arr2.length];
		for (int i = 0; i < arr1.length; i++) {
			ret[i] = arr1[i];
		}
		for (int i = 0; i < arr2.length; i++) {
			ret[arr1.length + i] = arr2[i];
		}
		return ret;
	}

	public static String[] toArray(String[] arr1, String[] arr2, String[] arr3) {
		String[] ret = new String[arr1.length + arr2.length + arr3.length];
		for (int i = 0; i < arr1.length; i++) {
			ret[i] = arr1[i];
		}
		for (int i = 0; i < arr2.length; i++) {
			ret[arr1.length + i] = arr2[i];
		}
		for (int i = 0; i < arr3.length; i++) {
			ret[arr1.length + arr2.length + i] = arr3[i];
		}
		return ret;
	}

	public static String[] toArray(String[] arr, String str) {
		String[] ret = new String[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = arr[i];
		}
		ret[arr.length] = str;
		return ret;
	}

	public static String[] toArray(String[] arr, String str1, String str2) {
		String[] ret = new String[arr.length + 2];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = arr[i];
		}
		ret[arr.length] = str1;
		ret[arr.length + 1] = str2;
		return ret;
	}

	public static String[] toArray(String[] arr, String str1, String str2, String str3) {
		String[] ret = new String[arr.length + 3];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = arr[i];
		}
		ret[arr.length] = str1;
		ret[arr.length + 1] = str2;
		ret[arr.length + 2] = str3;
		return ret;
	}

	public static String[] toArray(String[] arr, String str1, String str2, String str3, String str4) {
		String[] ret = new String[arr.length + 4];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = arr[i];
		}
		ret[arr.length] = str1;
		ret[arr.length + 1] = str2;
		ret[arr.length + 2] = str3;
		ret[arr.length + 3] = str4;
		return ret;
	}

	public static void addAll(List list, Object[] arr) {
		for (int i = 0; i < arr.length; i++) {
			list.add(arr[i]);
		}
	}

	public static ArrayPro toArrayPro(Array array) {
		if (array instanceof ArrayPro) return (ArrayPro) array;
		return new ArrayAsArrayPro(array);
	}

	public static Struct getMetaData(Array arr) throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._type, arr instanceof ArrayImpl && ((ArrayImpl) arr).sync() ? "synchronized" : "unsynchronized");
		sct.set("dimensions", arr.getDimension());
		sct.set("datatype", arr instanceof ArrayTyped ? ((ArrayTyped) arr).getTypeAsString() : "any");
		return sct;
	}
}