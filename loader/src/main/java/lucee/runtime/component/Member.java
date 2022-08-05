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
package lucee.runtime.component;

import java.io.Serializable;

public interface Member extends Serializable {

	public static final int MODIFIER_NONE = 0;
	public static final int MODIFIER_FINAL = 1;
	public static final int MODIFIER_ABSTRACT = 2;

	/**
	 * return the access modifier of this member
	 * 
	 * @return the access
	 */
	public int getAccess();

	/**
	 * return the value itself
	 * 
	 * @return value
	 */
	public Object getValue();

	/**
	 * return Member.MODIFIER_FINAL, Member.MODIFIER_ABSTRACT or Member.MODIFIER_NONE
	 * 
	 * @return the modifier.
	 */
	public int getModifier();

}