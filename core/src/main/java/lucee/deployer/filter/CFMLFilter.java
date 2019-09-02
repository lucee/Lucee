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
package lucee.deployer.filter;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ListUtil;

/**
 * Die Klasse CFMLFilter implementiert das Interface Filter, die Klasse prueft bei einem
 * uebergebenen File Objekt, ob dessen Extension mit denen die dem Konstruktor mitgegeben wurden
 * uebereinstimmen.
 */
public final class CFMLFilter implements Filter {

	private String[] extensions;

	/**
	 * Konstruktor von CFMLFilter, dem Konstruktor wird ein String Array uebergeben mit Extensions die
	 * geprueft werden sollen, wie z.B. {"html","htm"}.
	 * 
	 * @param extensions Extensions die geprueft werden sollen.
	 */
	public CFMLFilter(String[] extensions) {
		this.extensions = extensions;
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = extensions[i].toLowerCase();
		}
	}

	@Override
	public boolean isValid(Resource file) {
		String[] arr;
		try {
			arr = ListUtil.toStringArray(ListUtil.listToArray(file.getName(), '.'));
		}
		catch (PageException e) {
			return false;
		}
		String ext = arr[arr.length - 1].toLowerCase();
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].equals(ext)) return true;
		}
		return false;
	}
}