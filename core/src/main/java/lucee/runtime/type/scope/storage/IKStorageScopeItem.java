package lucee.runtime.type.scope.storage;

import java.io.Serializable;
import java.util.Date;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.dt.DateTime;

public class IKStorageScopeItem implements Serializable, ObjectWrap, Castable {

	private static final long serialVersionUID = -8187816208907138226L;

	private Object value;
	private long lastModifed;
	private boolean removed;

	public IKStorageScopeItem(Object value) {
		this(value, System.currentTimeMillis());
	}

	public IKStorageScopeItem(Object value, long lastModified) {
		this.value = value;
		this.lastModifed = lastModified;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public Object getEmbededObject() {
		return value;
	}

	@Override
	public Object getEmbededObject(Object defaultValue) {
		return value;
	}

	// needed for containsValue
	public boolean equals(Object o) {
		return value.equals(o);
	}

	public Object remove() {
		return remove(System.currentTimeMillis());
	}

	public Object remove(long lastMod) {
		this.lastModifed = lastMod;
		Object v = value;
		value = null;
		removed = true;
		return v;
	}

	public boolean removed() {
		return removed;
	}

	public long lastModified() {
		return lastModifed;
	}

	@Override
	public Boolean castToBoolean(Boolean df) {
		return Caster.toBoolean(getValue(), df);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBoolean(getValue());
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(getValue(), true, null);
	}

	@Override
	public DateTime castToDateTime(DateTime df) {
		return Caster.toDate(getValue(), true, null, df);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(getValue());
	}

	@Override
	public double castToDoubleValue(double df) {
		return Caster.toDoubleValue(getValue(), false, df);
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(getValue());
	}

	@Override
	public String castToString(String df) {
		return Caster.toString(getValue(), df);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(getValue(), str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(getValue(), b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(getValue(), d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(getValue(), (Date) dt);
	}
}
