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

import lucee.commons.lang.StringUtil;

public class ImportDefintionImpl implements ImportDefintion {

	private String pack;
	private String name;
	private boolean wildcard;
	private String packAsPath;

	public ImportDefintionImpl(String pack, String name) {
		this.pack = pack;
		this.name = name;
		this.wildcard = name.equals("*");

	}

	public static ImportDefintion getInstance(String fullname, ImportDefintion defaultValue) {
		int index = fullname.lastIndexOf('.');
		if (index == -1) return defaultValue;
		String p = fullname.substring(0, index).trim();
		String n = fullname.substring(index + 1, fullname.length()).trim();
		if (StringUtil.isEmpty(p) || StringUtil.isEmpty(n)) return defaultValue;

		return new ImportDefintionImpl(p, n);
	}

	/**
	 * @return the wildcard
	 */
	@Override
	public boolean isWildcard() {
		return wildcard;
	}

	/**
	 * @return the pack
	 */
	@Override
	public String getPackage() {
		return pack;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPackageAsPath() {
		if (packAsPath == null) {
			packAsPath = pack.replace('.', '/') + "/";
		}
		return packAsPath;
	}

	@Override
	public String toString() {
		return pack + "." + name;
	}

}