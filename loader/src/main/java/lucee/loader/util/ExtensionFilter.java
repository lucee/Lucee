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
package lucee.loader.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter fuer die <code>listFiles</code> Methode des FIle Objekt, zum filtern von FIles mit einer
 * bestimmten Extension.
 */
public final class ExtensionFilter implements FileFilter {

	private final String[] extensions;
	private final boolean allowDir;
	private final boolean ignoreCase;

	// private int extLen;

	/**
	 * Konstruktor des Filters
	 * 
	 * @param extension Endung die geprueft werden soll.
	 */
	public ExtensionFilter(final String extension) {
		this(new String[] { extension }, false, true);
	}

	public ExtensionFilter(final String extension, final boolean allowDir) {
		this(new String[] { extension }, allowDir, true);
	}

	public ExtensionFilter(final String[] extensions) {
		this(extensions, false, true);
	}

	public ExtensionFilter(final String[] extensions, final boolean allowDir) {
		this(extensions, allowDir, true);
	}

	public ExtensionFilter(final String[] extensions, final boolean allowDir, final boolean ignoreCase) {
		for (int i = 0; i < extensions.length; i++) {
			if (!extensions[i].startsWith(".")) extensions[i] = "." + extensions[i];
			if (ignoreCase) extensions[i] = extensions[i].toLowerCase();
		}
		this.extensions = extensions;
		this.allowDir = allowDir;
		this.ignoreCase = ignoreCase;
	}

	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File res) {
		if (res.isDirectory()) return allowDir;
		if (res.exists()) {
			final String name = ignoreCase ? res.getName().toLowerCase() : res.getName();
			for (final String extension: extensions)
				if (name.endsWith(extension)) return true;
		}
		return false;
	}

	/**
	 * @return Returns the extension.
	 */
	public String[] getExtensions() {
		return extensions;
	}
}