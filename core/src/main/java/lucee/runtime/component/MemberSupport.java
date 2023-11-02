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

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.type.Duplicable;
import lucee.runtime.type.util.ComponentUtil;

public abstract class MemberSupport implements Serializable, Member, Duplicable {

	private int access;
	private final int modifier;

	/**
	 * Constructor of the class
	 * 
	 * @param access
	 * @param value
	 */
	public MemberSupport(int access) {
		this.access = access;
		this.modifier = Member.MODIFIER_NONE;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param access
	 * @param value
	 */
	public MemberSupport(int access, int modifier) {
		this.access = access;
		this.modifier = modifier;
	}

	@Override
	public int getModifier() {
		return modifier;
	}

	@Override
	public int getAccess() {
		return access;
	}

	/**
	 * @param access
	 */
	public void setAccess(int access) {
		this.access = access;
	}

	/**
	 * @param access the access to set
	 * @throws ExpressionException
	 */
	public void setAccess(String access) throws ApplicationException {
		this.access = ComponentUtil.toIntAccess(access);
	}

}