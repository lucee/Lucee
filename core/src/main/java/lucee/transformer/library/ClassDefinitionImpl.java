/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.library;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.osgi.OSGiUtil;

public class ClassDefinitionImpl<T> implements ClassDefinition<T>, Externalizable {

	/**
	 * do not use to load class!!!
	 */
	private String className;
	private String name;
	private Version version;
	private Identification id;

	private transient Class<T> clazz;

	public ClassDefinitionImpl(String className, String name, String version, Identification id) {
		this.className = className == null ? null : className.trim();
		this.name = StringUtil.isEmpty(name, true) ? null : name.trim();
		this.version = OSGiUtil.toVersion(version, null);
		this.id = id;
	}

	public ClassDefinitionImpl(Identification id, String className, String name, Version version) {
		this.className = className == null ? null : className.trim();
		this.name = StringUtil.isEmpty(name, true) ? null : name.trim();
		this.version = version;
		this.id = id;
	}

	public ClassDefinitionImpl(String className) {
		this.className = className == null ? null : className.trim();
		this.name = null;
		this.version = null;
		this.id = null;
	}

	public ClassDefinitionImpl(Class<T> clazz) {
		this.className = clazz.getName();
		this.clazz = clazz;
		this.name = null;
		this.version = null;
		this.id = null;
	}

	/**
	 * only used by deserializer!
	 */
	public ClassDefinitionImpl() {
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(className);
		out.writeObject(name);
		out.writeObject(version == null ? null : version.toString());
		out.writeObject(id);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = (String) in.readObject();
		name = (String) in.readObject();
		String tmp = (String) in.readObject();
		if (tmp != null) this.version = OSGiUtil.toVersion(tmp, null);
		id = (Identification) in.readObject();
	}

	@Override
	public Class<T> getClazz() throws ClassException, BundleException {
		return getClazz(false);
	}

	public Class<T> getClazz(boolean forceLoadingClass) throws ClassException, BundleException {
		if (!forceLoadingClass && clazz != null) return clazz;

		// regular class definition
		if (name == null) return clazz = ClassUtil.loadClass(className);

		return clazz = ClassUtil.loadClassByBundle(className, name, version, id, JavaSettingsImpl.getBundleDirectories(null));
	}

	@Override
	public Class<T> getClazz(Class<T> defaultValue) {
		try {
			return getClazz();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	@Override
	public boolean hasClass() {
		return !StringUtil.isEmpty(className, true);
	}

	@Override
	public boolean isBundle() {
		return !StringUtil.isEmpty(name, true);
	}

	@Override
	public boolean hasVersion() {
		return version != null;
	}

	@Override
	public boolean isClassNameEqualTo(String otherClassName) {
		return isClassNameEqualTo(otherClassName, false);
	}

	@Override
	public boolean isClassNameEqualTo(String otherClassName, boolean ignoreCase) {
		if (otherClassName == null) return false;
		otherClassName = otherClassName.trim();
		return ignoreCase ? otherClassName.equalsIgnoreCase(className) : otherClassName.equals(className);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassDefinition)) return false;
		ClassDefinition other = (ClassDefinition) obj;
		return StringUtil.emptyIfNull(other.getClassName()).equals(StringUtil.emptyIfNull(className))
				&& StringUtil.emptyIfNull(other.getName()).equals(StringUtil.emptyIfNull(name))
				&& (other.getVersion() != null ? other.getVersion().equals(version) : version == null);
	}

	@Override
	public String toString() { // do not remove, this is used as key in ConfigWebFactory
		if (isBundle()) return "class:" + className + ";name:" + name + ";version:" + version + ";";
		return className;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public static ClassDefinition toClassDefinition(String className, Identification id, Attributes attributes) {
		if (StringUtil.isEmpty(className, true)) return null;

		String bn = null, bv = null;
		if (attributes != null) {
			// name
			bn = attributes.getValue("name");
			if (StringUtil.isEmpty(bn)) bn = attributes.getValue("bundle-name");

			// version
			bv = attributes.getValue("version");
			if (StringUtil.isEmpty(bv)) bv = attributes.getValue("bundle-version");
		}
		return new ClassDefinitionImpl(className, bn, bv, id);
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public String getVersionAsString() {
		return version == null ? null : version.toString();
	}

	@Override
	public String getId() {
		return HashUtil.create64BitHashAsString(toString());
	}
}