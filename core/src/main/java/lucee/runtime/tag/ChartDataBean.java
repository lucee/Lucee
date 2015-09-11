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
package lucee.runtime.tag;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class ChartDataBean implements Serializable,Comparable {

	private Object item;
	private String strItem;
	private double value;
	/**
	 * @return the item
	 */
	public Object getItem() {
		return item;
	}
	public String getItemAsString() {
		return strItem;
	}
	/**
	 * @param item the item to set
	 * @throws PageException 
	 */
	public void setItem(PageContext pc,Object obj) throws PageException {
		this.strItem = itemToString(pc, obj);
		this.item=obj;
	}
	public void setItem(String str)  {
		this.strItem = str;
		this.item=str;
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "item:"+item+";"+"value;"+value+";";
	}
	@Override
	public int compareTo(Object o) {
		if(!(o instanceof ChartDataBean)) return 0;
		ChartDataBean other=(ChartDataBean) o;
		return getItemAsString().compareTo(other.getItemAsString());
	}
	

	private String itemToString(PageContext pc,Object obj) throws PageException {
		if(obj instanceof Date) {
			TimeZone tz = pc.getTimeZone();
			return new lucee.runtime.format.DateFormat(pc.getLocale()).format(Caster.toDate(obj, tz),"short",tz)+" "+
			new lucee.runtime.format.TimeFormat(pc.getLocale()).format(Caster.toDate(obj, tz),"short",tz);
		}
		return Caster.toString(obj);
	}
}