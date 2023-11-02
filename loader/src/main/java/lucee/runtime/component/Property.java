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

import lucee.runtime.type.Struct;

/**
 * 
 */
public interface Property extends Serializable, Member {

	/**
	 * @return the _default
	 */
	public String getDefault();

	/**
	 * @return the displayname
	 */
	public String getDisplayname();

	/**
	 * @return the hint
	 */
	public String getHint();

	/**
	 * @return the name
	 */
	public String getName();

	/**
	 * @return the required
	 */
	public boolean isRequired();

	/**
	 * @return the type
	 */
	public String getType();

	/**
	 * @return the setter
	 */
	public boolean getSetter();

	/**
	 * @return the getter
	 */
	public boolean getGetter();

	public Object getMetaData();

	public Struct getMeta();

	public Class<?> getClazz();

	public boolean isPeristent();

	public String getOwnerName();

	public Struct getDynamicAttributes();

}