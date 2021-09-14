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
package lucee.runtime.type;

import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.ref.Reference;

/**
 * Simple Value Array, an Array that can't cast to a Simple Value
 */
public final class SVArray extends ArrayImpl implements Reference {

	private int position = 1;

	/**
	 * Constructor of the class
	 */
	public SVArray() {
		super();
	}

	/**
	 * Constructor of the class
	 * 
	 * @param objects
	 */
	public SVArray(Object[] objects) {
		super(objects);
	}

	/**
	 * @return Returns the position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public Collection.Key getKey() {
		return KeyImpl.init(Caster.toString(position));
	}

	@Override
	public String getKeyAsString() {
		return Caster.toString(position);
	}

	@Override
	public Object get(PageContext pc) throws PageException {
		return getE(position);
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return get(position, defaultValue);
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		Object o = get(position, null);
		if (o != null) return o;
		return setE(position, new StructImpl());
	}

	@Override
	public Object touchEL(PageContext pc) {
		Object o = get(position, null);
		if (o != null) return o;
		return setEL(position, new StructImpl());
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return setE(position, value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		return setEL(position, value);
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return removeE(position);
	}

	@Override
	public Object removeEL(PageContext pc) {
		return removeEL(position);
	}

	@Override
	public Object getParent() {
		return this;
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(getE(position));
	}

	@Override
	public String castToString(String defaultValue) {
		Object value = get(position, null);
		if (value == null) return defaultValue;
		return Caster.toString(value, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(getE(position));
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		Object value = get(position, defaultValue);
		if (value == null) return defaultValue;
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(getE(position));
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		Object value = get(position, null);
		if (value == null) return defaultValue;
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(getE(position), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		Object value = get(position, defaultValue);
		if (value == null) return defaultValue;
		return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (Date) castToDateTime(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d));
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);
		table.setTitle("SV Array");
		return table;
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		SVArray sva = new SVArray();
		duplicate(sva, deepCopy);
		sva.position = position;
		return sva;
	}
}