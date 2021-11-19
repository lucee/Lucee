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
package lucee.runtime.security;

import lucee.commons.io.res.Resource;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;

/**
 * interface for Security Manager
 */
public interface SecurityManager {

	public final static short ACCESS_OPEN = 1;
	public final static short ACCESS_PROTECTED = 2;
	public final static short ACCESS_CLOSE = 3;

	/**
	 * Field <code>TYPE_SETTING</code>
	 */
	public final static int TYPE_SETTING = 0;

	/**
	 * Field <code>TYPE_FILE</code>
	 */
	public final static int TYPE_FILE = 1;

	/**
	 * Field <code>TYPE_DIRECT_JAVA_ACCESS</code>
	 */
	public final static int TYPE_DIRECT_JAVA_ACCESS = 2;

	/**
	 * Field <code>TYPE_MAIL</code>
	 */
	public final static int TYPE_MAIL = 3;

	/**
	 * Field <code>TYPE_DATASOURCE</code>
	 */
	public final static int TYPE_DATASOURCE = 4;

	/**
	 * Field <code>TYPE_MAPPING</code>
	 */
	public final static int TYPE_MAPPING = 5;

	/**
	 * Field <code>TYPE_CUSTOM_TAG</code>
	 */
	public final static int TYPE_CUSTOM_TAG = 6;

	/**
	 * Field <code>TYPE_CFX_SETTING</code>
	 */
	public final static int TYPE_CFX_SETTING = 7;

	/**
	 * Field <code>TYPE_CFX_USAGE</code>
	 */
	public final static int TYPE_CFX_USAGE = 8;

	/**
	 * Field <code>TYPE_DEBUGGING</code>
	 */
	public final static int TYPE_DEBUGGING = 9;

	/**
	 * Field <code>TYPE_TAG_EXECUTE</code>
	 */
	public static final int TYPE_TAG_EXECUTE = 10;

	/**
	 * Field <code>TYPE_TAG_IMPORT</code>
	 */
	public static final int TYPE_TAG_IMPORT = 11;

	/**
	 * Field <code>TYPE_TAG_OBJECT</code>
	 */
	public static final int TYPE_TAG_OBJECT = 12;

	/**
	 * Field <code>TYPE_TAG_REGISTRY</code>
	 */
	public static final int TYPE_TAG_REGISTRY = 13;

	/**
	 * Field <code>TYPE_SEARCH</code>
	 */
	public static final int TYPE_SEARCH = 14;

	/**
	 * Field <code>TYPE_SCHEDULED_TASK</code>
	 */
	public static final int TYPE_SCHEDULED_TASK = 15;

	public static final int TYPE_ACCESS_READ = 16;
	public static final int TYPE_ACCESS_WRITE = 17;
	public static final int TYPE_REMOTE = 18;

	public static final int TYPE_CACHE = 19;
	public static final int TYPE_GATEWAY = 20;
	public static final int TYPE_ORM = 21;

	/**
	 * Field <code>VALUE_NO</code>
	 */
	public final static short VALUE_NO = 0;

	/**
	 * Field <code>VALUE_NONE</code>
	 */
	public final static short VALUE_NONE = 0;

	/**
	 * Field <code>VALUE_LOCAL</code>
	 */
	public final static short VALUE_LOCAL = 1;

	/**
	 * Field <code>VALUE_YES</code>
	 */
	public final static short VALUE_YES = 2;

	/**
	 * Field <code>VALUE_ALL</code>
	 */
	public final static short VALUE_ALL = 2;

	public final static short VALUE_1 = 11;
	public final static short VALUE_2 = 12;
	public final static short VALUE_3 = 13;
	public final static short VALUE_4 = 14;
	public final static short VALUE_5 = 15;
	public final static short VALUE_6 = 16;
	public final static short VALUE_7 = 17;
	public final static short VALUE_8 = 18;
	public final static short VALUE_9 = 19;
	public final static short VALUE_10 = 20;

	public final static short NUMBER_OFFSET = 10;

	/**
	 * @param access
	 * @return return access value (all,local,none ...) for given type (cfx,file ...)
	 */
	public abstract short getAccess(int access);

	/**
	 * @param access
	 * @return return access value (all,local,none ...) for given type (cfx,file ...)
	 * @throws PageException
	 */
	public abstract short getAccess(String access) throws PageException;

	/**
	 * @param res
	 * @throws PageException
	 */
	public abstract void checkFileLocation(Resource res) throws PageException;

	/**
	 * @param config
	 * @param res
	 * @param serverPassword
	 * @throws PageException
	 */
	public abstract void checkFileLocation(ConfigWeb config, Resource res, String serverPassword) throws PageException;

	/**
	 * @return clone the security Manager
	 */
	public abstract SecurityManager cloneSecurityManager();

}