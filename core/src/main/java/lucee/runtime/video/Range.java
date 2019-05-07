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
package lucee.runtime.video;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class Range {

	public static final Range TRUE = new Range(0, -1);
	public static final Range FALSE = new Range(0, 0);
	private double from;
	private double to;

	public Range(double from, double to) {
		this.from = from;
		this.to = to;
	}

	public static Range toRange(String def) throws PageException {
		def = def.trim();
		// boolean
		if (Decision.isBoolean(def)) {
			return Caster.toBooleanValue(def) ? TRUE : FALSE;
		}

		int index = def.indexOf(',');
		// single value
		if (index == -1) {
			return new Range(toSeconds(def), -1);
		}

		// double value
		if (def.startsWith(",")) def = "0" + def;
		if (def.endsWith(",")) def += "-1";

		return new Range(toSeconds(def.substring(0, index)), toSeconds(def.substring(index + 1)));

	}

	private static double toSeconds(String str) throws PageException {
		str = str.trim().toLowerCase();

		if (str.endsWith("ms")) return Caster.toDoubleValue(str.substring(0, str.length() - 2)) / 1000D;
		else if (str.endsWith("s")) return Caster.toDoubleValue(str.substring(0, str.length() - 1));
		else return Caster.toDoubleValue(str) / 1000D;
		// TODO if(str.endsWith("f")) this.startFrame=VideoConfig.toLong(str.substring(0,str.length()-1));

	}

	/**
	 * @return the from
	 */
	public double getFrom() {
		return from;
	}

	public String getFromAsString() {
		return Caster.toString(from);
	}

	/**
	 * @return the to
	 */
	public double getTo() {
		return to;
	}

	public String getToAsString() {
		return Caster.toString(to);
	}

	/**
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Range)) return false;
		Range other = (Range) obj;
		return other.from == from && other.to == to;
	}

	/**
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + from + ":" + to + "";
	}

	public boolean show() {
		return !equals(Range.FALSE);
	}
}