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
package lucee.commons.io.res.util;

import lucee.commons.io.ModeUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.dt.DateTime;

public final class ModeObjectWrap implements ObjectWrap, Castable {

	private static final long serialVersionUID = -1630745501422006978L;

	private final Resource res;
	private String mode = null;

	public ModeObjectWrap(Resource res) {
		this.res = res;
	}

	@Override
	public Object getEmbededObject() {
		return toString();
	}

	@Override
	public Object getEmbededObject(Object def) {
		return toString();
	}

	@Override
	public String toString() {
		// print.dumpStack();
		if (mode == null) mode = ModeUtil.toStringMode(res.getMode());
		return mode;
	}

	public String castString() {
		return toString();
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(toString());
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(toString(), defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDatetime(toString(), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return DateCaster.toDateAdvanced(toString(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(toString());
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(toString(), defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return toString();
	}

	@Override
	public String castToString(String defaultValue) {
		return toString();
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), toString(), str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d));
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), toString(), dt.castToString());
	}

}