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
package lucee.runtime.chart;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;

public class TickUnitWrap extends NumberTickUnit {

	private TickUnit tickUnit;
	private int labelFormat;

	public TickUnitWrap(TickUnit tickUnit, int labelFormat) {
		super(tickUnit.getSize());
		this.tickUnit=tickUnit;
		this.labelFormat=labelFormat;
	}

	@Override
	public int compareTo(Object object) {
		return tickUnit.compareTo(object);
	}

	@Override
	public boolean equals(Object obj) {
		return tickUnit.equals(obj);
	}

	@Override
	public double getSize() {
		return tickUnit.getSize();
	}

	@Override
	public int hashCode() {
		return tickUnit.hashCode();
	}

	@Override
	public String valueToString(double value) {
		return LabelFormatUtil.format(labelFormat, value);
	}
}