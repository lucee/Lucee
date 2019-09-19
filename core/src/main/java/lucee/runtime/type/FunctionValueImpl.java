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
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;

/**
 * represent a named function value for a functions
 */
public final class FunctionValueImpl implements FunctionValue, Dumpable {

	private final Collection.Key name;
	private final String[] names;
	private final Object value;

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public static FunctionValue newInstance(String name, Object value) {
		return new FunctionValueImpl(name, value);
	}

	public static FunctionValue newInstance(String[] name, Object value) {
		return new FunctionValueImpl(name, value);
	}

	public static FunctionValue newInstance(Collection.Key name, Object value) {
		return new FunctionValueImpl(name, value);
	}

	/**
	 * constructor of the class
	 * 
	 * @param name name of the value
	 * @param value value himself
	 */
	public FunctionValueImpl(String name, Object value) {
		this.name = KeyImpl.init(name);
		this.value = value;
		names = null;
	}

	public FunctionValueImpl(Collection.Key name, Object value) {
		this.name = name;
		this.value = value;
		names = null;
	}

	public FunctionValueImpl(String[] names, Object value) {
		this.names = names;
		this.value = value;
		name = null;
	}

	@Override
	public String getName() {
		return getNameAsString();
	}

	@Override
	public String getNameAsString() {
		if (name == null) {
			return ListUtil.arrayToList(names, ".");
		}
		return name.getString();
	}

	@Override
	public Collection.Key getNameAsKey() {
		if (name == null) {
			return KeyImpl.init(ListUtil.arrayToList(names, "."));
		}
		return name;
	}

	public String[] getNames() {
		return names;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(value);
	}

	@Override
	public String castToString(String defaultValue) {
		return Caster.toString(value, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(value);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(value);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return DateCaster.toDateSimple(value, DateCaster.CONVERTING_TYPE_OFFSET, true, null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return DateCaster.toDateSimple(value, DateCaster.CONVERTING_TYPE_OFFSET, true, null, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(value, b ? 1D : 0D);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(value, (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(value, d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(value, str);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return DumpUtil.toDumpData(value, pageContext, maxlevel, properties);
	}

	@Override
	public String toString() {
		return name + ":" + value;
	}

	public static Struct toStruct(FunctionValueImpl fv1) {
		StructImpl sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(fv1.getNameAsKey(), fv1);
		return sct;
	}

	public static Struct toStruct(FunctionValueImpl fv1, FunctionValueImpl fv2) {
		StructImpl sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(fv1.getNameAsKey(), fv1);
		sct.setEL(fv2.getNameAsKey(), fv2);
		return sct;
	}

	public static Struct toStruct(FunctionValueImpl fv1, FunctionValueImpl fv2, FunctionValueImpl fv3) {
		StructImpl sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(fv1.getNameAsKey(), fv1);
		sct.setEL(fv2.getNameAsKey(), fv2);
		sct.setEL(fv3.getNameAsKey(), fv3);
		return sct;
	}

	public static Struct toStruct(FunctionValueImpl fv1, FunctionValueImpl fv2, FunctionValueImpl fv3, FunctionValueImpl fv4) {
		StructImpl sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(fv1.getNameAsKey(), fv1);
		sct.setEL(fv2.getNameAsKey(), fv2);
		sct.setEL(fv3.getNameAsKey(), fv3);
		sct.setEL(fv4.getNameAsKey(), fv4);
		return sct;
	}

}