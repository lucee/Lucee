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

import java.io.Serializable;

import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

public class TickUnitsImpl implements TickUnitSource, Cloneable, Serializable {

	private TickUnitSource tus;
	private int labelFormat;

	/**
	 * Constructor of the class
	 * @param tus
	 */
	public TickUnitsImpl(TickUnitSource tus, int labelFormat) {
		this.tus=tus;
		this.labelFormat=labelFormat;
	}
	
	@Override
	public TickUnit getCeilingTickUnit(TickUnit unit) {
		return new TickUnitWrap(tus.getCeilingTickUnit(unit),labelFormat);
	}

	@Override
	public TickUnit getCeilingTickUnit(double size) {
		return new TickUnitWrap(tus.getCeilingTickUnit(size),labelFormat);
	}

	@Override
	public TickUnit getLargerTickUnit(TickUnit unit) {
		return new TickUnitWrap(tus.getLargerTickUnit(unit),labelFormat);
	}

}