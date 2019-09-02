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
package lucee.runtime.listener;

import lucee.commons.io.res.Resource;

public interface JavaSettings {

	/**
	 * pathes to the directories that contain Java classes or JAR files.
	 * 
	 * @return resource pathes
	 */
	public Resource[] getResources();

	public Resource[] getResourcesTranslated();

	/**
	 * Indicates whether to load the classes from the default lib directory. The default value is false
	 * 
	 * @return
	 */
	public boolean loadCFMLClassPath();

	/**
	 * Indicates whether to reload the updated classes and JARs dynamically, without restarting
	 * ColdFusion. The default value is false
	 * 
	 * @return
	 */
	public boolean reloadOnChange();

	/**
	 * Specifies the time interval in seconds after which to verify any change in the class files or JAR
	 * files. The default value is 60seconds
	 * 
	 * @return
	 */
	public int watchInterval();

	/**
	 * Specifies the extensions of the files to monitor for changes. By default, only .class and .jar
	 * files aremonitored.
	 * 
	 * @return
	 */
	public String[] watchedExtensions();

}