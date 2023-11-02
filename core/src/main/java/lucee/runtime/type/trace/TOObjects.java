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
package lucee.runtime.type.trace;

import lucee.runtime.PageContext;
import lucee.runtime.debug.Debugger;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.util.VariableUtilImpl;

public class TOObjects extends TraceObjectSupport implements Objects {

	private static final long serialVersionUID = -2011026266467450312L;

	protected TOObjects(Debugger debugger, Object obj, int type, String category, String text) {
		super(debugger, obj, type, category, text);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		log();
		return DumpUtil.toDumpData(o, pageContext, maxlevel, properties);
	}

	@Override
	public String castToString() throws PageException {
		log();
		return Caster.toString(o);
	}

	@Override
	public String castToString(String defaultValue) {
		log();
		return Caster.toString(o, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		log();
		return Caster.toBooleanValue(o);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		log();
		return Caster.toBoolean(o, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		log();
		return Caster.toDoubleValue(o);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		log();
		return Caster.toDoubleValue(o, true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		log();
		return new TODateTime(debugger, Caster.toDate(o, false, null), type, category, text);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		log();
		return new TODateTime(debugger, Caster.toDate(o, false, null, defaultValue), type, category, text);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		log();
		return Operator.compare(o, b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		log();
		return Operator.compare(o, (Object) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		log();
		return Operator.compare(o, d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		log();
		return Operator.compare(o, str);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		log(key.getString());
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.get(pc, o, key);
		// return TraceObjectSupport.toTraceObject(debugger,var.get(pc, o, key),type,category,text);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		log(key.getString());
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.get(pc, o, key, defaultValue);
		// return TraceObjectSupport.toTraceObject(debugger,var.get(pc, o, key,
		// defaultValue),type,category,text);
	}

	@Override
	public Object set(PageContext pc, Key key, Object value) throws PageException {
		log(key, value);
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.set(pc, o, key, value);
		// return TraceObjectSupport.toTraceObject(debugger,var.set(pc, o, key, value),type,category,text);
	}

	@Override
	public Object setEL(PageContext pc, Key key, Object value) {
		log(key, value);
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.setEL(pc, o, key, value);
		// return TraceObjectSupport.toTraceObject(debugger,var.setEL(pc, o, key,
		// value),type,category,text);
	}

	@Override
	public Object call(PageContext pc, Key key, Object[] args) throws PageException {
		log(key.getString());
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.callFunctionWithoutNamedValues(pc, o, key, args);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key key, Struct args) throws PageException {
		log(key.getString());
		VariableUtilImpl var = (VariableUtilImpl) pc.getVariableUtil();
		return var.callFunctionWithNamedValues(pc, o, key, args);
	}

	public boolean isInitalized() {
		log();
		return true;
	}
}