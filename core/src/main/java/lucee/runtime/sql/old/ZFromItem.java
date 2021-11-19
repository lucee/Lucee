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

package lucee.runtime.sql.old;

// Referenced classes of package Zql:
//            ZAliasedName

public final class ZFromItem extends ZAliasedName {

	private String fullName;

	public ZFromItem() {
	}

	public ZFromItem(String s) {
		super(s, ZAliasedName.FORM_TABLE);
		fullName = s;
	}

	/**
	 * @return
	 */
	public String getFullName() {
		return fullName;
	}
}