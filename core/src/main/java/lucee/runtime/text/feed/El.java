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
package lucee.runtime.text.feed;

public class El {

	public static short QUANTITY_0_1 = 0;
	public static short QUANTITY_0_N = 4;
	public static short QUANTITY_1 = 8;
	public static short QUANTITY_1_N = 16;
	public static final short QUANTITY_AUTO = QUANTITY_0_1;

	private Attr[] attrs;
	private short quantity;
	private boolean hasChildren;

	public El(short quantity, Attr[] attrs, boolean hasChildren) {
		this.quantity = quantity;
		this.attrs = attrs;
		this.hasChildren = hasChildren;
	}

	public El(short quantity, Attr[] attrs) {
		this(quantity, attrs, false);
	}

	public El(short quantity, Attr attr, boolean hasChildren) {
		this(quantity, new Attr[] { attr }, hasChildren);
	}

	public El(short quantity, Attr attr) {
		this(quantity, new Attr[] { attr });
	}

	public El(short quantity, boolean hasChildren) {
		this(quantity, (Attr[]) null, hasChildren);
	}

	public El(short quantity) {
		this(quantity, (Attr[]) null);
	}

	/**
	 * @return the hasChildren
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}

	/**
	 * @return the attrs
	 */
	public Attr[] getAttrs() {
		return attrs;
	}

	/**
	 * @return the quantity
	 */
	public short getQuantity() {
		return quantity;
	}

	public boolean isQuantity(short quantity) {
		return this.quantity == quantity;
	}

}