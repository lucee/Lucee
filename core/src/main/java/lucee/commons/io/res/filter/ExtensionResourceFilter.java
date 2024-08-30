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
package lucee.commons.io.res.filter;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Filter fuer die <code>listFiles</code> Methode des FIle Objekt, zum filtern von FIles mit einer
 * bestimmten Extension.
 */
public final class ExtensionResourceFilter implements ResourceFilter {

	private String[] extensions;
	private final boolean allowDir;
	private final boolean ignoreCase;
	private final boolean mustExists;
	// private int extLen;

	public static final ExtensionResourceFilter EXTENSION_JAR_NO_DIR = new ExtensionResourceFilter(".jar");
	public static final ExtensionResourceFilter EXTENSION_CLASS_DIR = new ExtensionResourceFilter(true, ".class");

	public ExtensionResourceFilter(String... extension) {
		this(false, true, false, extension);
	}

	public ExtensionResourceFilter(boolean allowDir, String... extension) {
		this(allowDir, true, false, extension);
	}

	public ExtensionResourceFilter(boolean allowDir, boolean ignoreCase, boolean mustExists, String... extensions) {
		String[] tmp = new String[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			if (!StringUtil.startsWith(extensions[i], '.')) tmp[i] = "." + extensions[i];
			else tmp[i] = extensions[i];
		}
		this.extensions = tmp;
		this.allowDir = allowDir;
		this.ignoreCase = ignoreCase;
		this.mustExists = mustExists;
	}

	public void addExtension(String extension) {
		String[] tmp = new String[extensions.length + 1];
		// add existing
		for (int i = 0; i < extensions.length; i++) {
			tmp[i] = extensions[i];
		}
		// add the new one
		if (!StringUtil.startsWith(extension, '.')) tmp[extensions.length] = "." + extension;
		else tmp[extensions.length] = extension;

		this.extensions = tmp;
	}

	@Override
	public boolean accept(Resource res) {
		// check files
		String name = res.getName();
		for (String ext: extensions) {
			if (ignoreCase) {
				if (StringUtil.endsWithIgnoreCase(name, ext)) return !mustExists || res.exists();
			}
			else {
				if (name.endsWith(ext)) return !mustExists || res.exists();
			}
		}

		// check directory
		if (!allowDir) return false;
		return res.isDirectory();
	}

	public boolean accept(String name) {
		for (int i = 0; i < extensions.length; i++) {
			if (ignoreCase) {
				if (StringUtil.endsWithIgnoreCase(name, extensions[i])) return true;
			}
			else {
				if (name.endsWith(extensions[i])) return true;
			}
		}

		return false;
	}

	/**
	 * @return Returns the extension.
	 */
	public String[] getExtensions() {
		return extensions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String ext: getExtensions()) {
			if (sb.length() > 0) sb.append(',');
			sb.append(ListUtil.trim(ext, "."));
		}
		return sb.toString();
	}
}