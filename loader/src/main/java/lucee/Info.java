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
package lucee;

/**
 * Info to this Version
 */
public interface Info {

	public static final int STATE_ALPHA = 2 * 100000000;
	public static final int STATE_BETA = 1 * 100000000;
	public static final int STATE_RC = 3 * 100000000;
	public static final int STATE_FINAL = 0;

	/**
	 * @return the level
	 */
	public String getLevel();

	/**
	 * @return Returns the releaseTime.
	 */
	public long getRealeaseTime();

	/**
	 * @return Returns the version.
	 */
	public org.osgi.framework.Version getVersion();

	/**
	 * @return returns the state
	 */
	// public int getStateAsInt();

	/**
	 * @return returns the state
	 */
	// public String getStateAsString();

	public long getFullVersionInfo();

	public String getVersionName();

	public String getVersionNameExplanation();

	public String[] getCFMLTemplateExtensions();

	public String[] getLuceeTemplateExtensions();

	@Deprecated
	public String[] getCFMLComponentExtensions();

	@Deprecated
	public String[] getLuceeComponentExtensions();

	public String getCFMLComponentExtension();

	public String getLuceeComponentExtension();

}