package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;

/**
 * Marker for a value used with the spread operator (...) inside a literal array, a literal struct
 * or the argument list of a function call.
 */
public final class SpreadValue {

	private final Object value;

	public SpreadValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * add the elements of the spread value to the given array. An array adds its elements, a simple
	 * value (string) adds every character.
	 */
	public void addTo(java.util.List<Object> target, String context) throws PageException {
		if (value instanceof Struct || (!(value instanceof String) && Decision.isStruct(value))) {
			throw new ExpressionException("the spread operator can not be used with a struct inside " + context + ", use it with an array or a string. "
					+ "To pass a struct as named arguments, use argumentCollection");
		}
		if (Decision.isArray(value)) {
			Array arr = Caster.toArray(value);
			int len = arr.size();
			for (int i = 1; i <= len; i++) {
				target.add(arr.get(i, null));
			}
		}
		else if (value instanceof CharSequence) {
			String str = value.toString();
			for (int i = 0; i < str.length(); i++) {
				target.add(String.valueOf(str.charAt(i)));
			}
		}
		else if (value == null) {
			throw new ExpressionException("the spread operator can not be used with a null value inside " + context);
		}
		else if (Decision.isSimpleValue(value)) {
			String str = Caster.toString(value);
			for (int i = 0; i < str.length(); i++) {
				target.add(String.valueOf(str.charAt(i)));
			}
		}
		else {
			throw new ExpressionException(
					"the spread operator can not be used with a value of type [" + Caster.toTypeName(value) + "] inside " + context + ", use it with an array or a string");
		}
	}

	/**
	 * copy the entries of the spread value to the given struct, existing keys are overwritten.
	 */
	public void addTo(Struct target) throws PageException {
		if (value != null && Decision.isStruct(value)) {
			Struct sct = Caster.toStruct(value);
			Iterator<Entry<Key, Object>> it = sct.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				target.setEL(e.getKey(), e.getValue());
			}
		}
		else if (value == null) {
			throw new ExpressionException("the spread operator can not be used with a null value inside a struct literal");
		}
		else {
			throw new ExpressionException("the spread operator can only be used with a struct inside a struct literal, got [" + Caster.toTypeName(value) + "]");
		}
	}

	public static Object[] expand(Object[] args, String context) throws PageException {
		java.util.List<Object> list = new java.util.ArrayList<Object>();
		for (Object arg: args) {
			if (arg instanceof SpreadValue) ((SpreadValue) arg).addTo(list, context);
			else list.add(arg);
		}
		return list.toArray();
	}
}
