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
package lucee.transformer.bytecode.util;

import org.objectweb.asm.Type;

import lucee.runtime.exp.PageException;

public final class ASMPropertyImpl implements ASMProperty {

	private Type type;
	private String name;
	private Class clazz;

	public ASMPropertyImpl(Class type, String name) throws PageException {
		this.type = ASMUtil.toType(type, true);
		this.name = name;
		this.clazz = type;
	}

	public ASMPropertyImpl(String type, String name) throws PageException {
		this.type = ASMUtil.toType(type, true);
		this.name = name;
	}

	public ASMPropertyImpl(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	@Override
	public Type getASMType() {
		return type;
	}

	/**
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "class:" + (clazz == null ? null : clazz.getName()) + ";name:" + name + ";type:" + type.getClassName();
	}

	/**
	 * @return the clazz
	 */
	@Override
	public Class getClazz() {
		return clazz;
	}
}