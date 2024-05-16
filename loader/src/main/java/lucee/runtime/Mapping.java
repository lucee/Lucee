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
package lucee.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import lucee.commons.io.res.Resource;
import lucee.runtime.config.Config;

/**
 * interface of the mapping definition
 */
public interface Mapping extends Serializable {

	public Class<?> getArchiveClass(String className) throws ClassNotFoundException;

	public Class<?> getArchiveClass(String className, Class<?> defaultValue);

	public InputStream getArchiveResourceAsStream(String string);

	public Class<?> getPhysicalClass(String className) throws ClassNotFoundException, IOException;

	public Class<?> getPhysicalClass(String className, byte[] code) throws IOException;

	/**
	 * @return Returns the physical.
	 */
	public abstract Resource getPhysical();

	/**
	 * @return Returns the virtual lower case.
	 */
	public abstract String getVirtualLowerCase();

	/**
	 * @return Returns the virtual lower case with slash at the end.
	 */
	public abstract String getVirtualLowerCaseWithSlash();

	/**
	 * @return return the archive file
	 */
	public abstract Resource getArchive();

	/**
	 * @return returns if mapping has an archive
	 */
	public abstract boolean hasArchive();

	/**
	 * @return return if mapping has a physical path
	 */
	public abstract boolean hasPhysical();

	/**
	 * @return class root directory
	 */
	public abstract Resource getClassRootDirectory();

	/**
	 * pagesource matching given realpath
	 * 
	 * @param realPath path
	 * @return matching pagesource
	 */
	public abstract PageSource getPageSource(String realPath);

	/**
	 * @param path path
	 * @param isOut is out
	 * @return matching pagesource
	 */
	public abstract PageSource getPageSource(String path, boolean isOut);

	/**
	 * checks the mapping
	 */
	public abstract void check();

	/**
	 * @return Returns the hidden.
	 */
	public abstract boolean isHidden();

	/**
	 * @return Returns the physicalFirst.
	 */
	public abstract boolean isPhysicalFirst();

	/**
	 * @return Returns the readonly.
	 */
	public abstract boolean isReadonly();

	/**
	 * @return Returns the strArchive.
	 */
	public abstract String getStrArchive();

	/**
	 * @return Returns the strPhysical.
	 */
	public abstract String getStrPhysical();

	/**
	 * @return Returns the trusted.
	 * @deprecated use instead <code>public short getInspectTemplate();</code>
	 */
	@Deprecated
	public abstract boolean isTrusted();

	public short getInspectTemplate();

	public abstract boolean isTopLevel();

	/**
	 * @return Returns the virtual.
	 */
	public abstract String getVirtual();

	/**
	 * returns config of the mapping
	 * 
	 * @return config
	 */
	public Config getConfig();

	/**
	 * mapping can have a specific listener mode to overwrite the listener mode coming from the
	 * Application Context
	 * 
	 * @return Listener mode
	 */
	public int getListenerMode();

	/**
	 * mapping can have a specific listener type to overwrite the listener mode coming from the
	 * Application Context
	 * 
	 * @return Listener type
	 */
	public int getListenerType();

	// public void flush(); FUTURE
}