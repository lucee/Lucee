/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.cfx.customtag;

import org.osgi.framework.BundleException;

import com.allaire.cfx.CustomTag;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.cfx.CFXTagException;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.reflection.Reflector;

/**
 * 
 */
public final class JavaCFXTagClass implements CFXTagClass {

	private String name;
	private ClassDefinition cd;
	private boolean readOnly = false;

	public JavaCFXTagClass(String name, ClassDefinition cd) {
		name = name.toLowerCase();
		if (name.startsWith("cfx_")) name = name.substring(4);
		this.name = name;
		this.cd = cd;
	}

	private JavaCFXTagClass(String name, ClassDefinition cd, boolean readOnly) {

		this.name = name;
		this.cd = cd;
		this.readOnly = readOnly;
	}

	@Override
	public CustomTag newInstance() throws CFXTagException {
		try {
			return _newInstance();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw new CFXTagException(e);
		}
	}

	public CustomTag _newInstance() throws InstantiationException, IllegalAccessException, ClassException, BundleException {

		Object o = getClazz().newInstance();
		return (CustomTag) o;
	}

	/**
	 * @return Returns the clazz.
	 * @throws BundleException
	 * @throws ClassException
	 */
	public Class<CustomTag> getClazz() throws ClassException, BundleException {
		return cd.getClazz();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the strClass.
	 */
	public ClassDefinition getClassDefinition() {
		return cd;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public CFXTagClass cloneReadOnly() {
		return new JavaCFXTagClass(name, cd, true);
	}

	@Override
	public String getDisplayType() {
		return "Java";
	}

	@Override
	public String getSourceName() {
		return cd.getClassName();
	}

	@Override
	public boolean isValid() {
		try {
			return Reflector.isInstaneOf(getClazz(), CustomTag.class, false);
		}
		catch (Exception e) {
			return false;
		}
	}
}