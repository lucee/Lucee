package lucee.commons.lang.types;

import java.util.Date;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.dt.DateTime;

public class RefStringImpl implements RefString, Castable {

	private String value;

	public RefStringImpl(String value) {
		this.value = value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() {
		try {
			return Caster.toBooleanValue(value);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(value, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return Caster.toDate(value, false, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(value);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(value, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return value;
	}

	@Override
	public String castToString(String defaultValue) {
		return value;
	}

	@Override
	public int compareTo(String other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
	}

	@Override
	public int compareTo(boolean other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, other ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(double other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(other));
	}

	@Override
	public int compareTo(DateTime other) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (Date) castToDateTime(), (Date) other);
	}

}
