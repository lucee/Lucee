/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.type.trace;

import lucee.runtime.PageContext;
import lucee.runtime.debug.Debugger;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.dt.DateTime;

public class TODateTime extends DateTime implements TraceObject {

	private DateTime dt;
	// private Debugger debugger;
	private Query qry = new QueryImpl(new String[] { "label", "action", "params", "template", "line", "time" }, 0, "traceObjects");
	private int type;
	private String category;
	private String text;
	private Debugger debugger;

	public TODateTime(Debugger debugger, DateTime dt, int type, String category, String text) {
		this.dt = dt;
		this.debugger = debugger;
		this.type = type;
		this.category = category;
		this.text = text;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		log();
		return dt.toDumpData(pageContext, maxlevel, properties);
	}

	@Override
	public String castToString() throws PageException {
		log();
		return dt.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		log();
		return dt.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		log();
		return dt.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		log();
		return dt.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		log();
		return dt.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		log();
		return dt.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		log();
		return this;
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		log();
		return this;
	}

	@Override
	public int compareTo(String str) throws PageException {
		log();
		return dt.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		log();
		return dt.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		log();
		return dt.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		log();
		return dt.compareTo(dt);
	}

	@Override
	public double toDoubleValue() {
		log();
		return this.dt.toDoubleValue();
	}

	protected void log() {
		TraceObjectSupport.log(debugger, type, category, text, null, null);
	}

	public Query getDebugData() {
		return qry;
	}
}