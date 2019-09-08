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
package lucee.runtime.customtag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Constants;

public class InitFile {

	private PageSource ps;
	private String filename;
	private boolean isCFC;

	public InitFile(PageContext pc, PageSource ps, String filename) {
		this.ps = ps;
		this.filename = filename;

		// the lucee dialect has not different extension for component and templates, but this dialect also
		// only supports components
		isCFC = false;
		String[] extensions = Constants.getComponentExtensions();// CustomTagUtil.getComponentExtension(pc,ps);
		for (int i = 0; i < extensions.length; i++) {
			if (StringUtil.endsWithIgnoreCase(filename, '.' + extensions[i])) {
				isCFC = true;
				break;
			}
		}

	}

	public PageSource getPageSource() {
		return ps;
	}

	public String getFilename() {
		return filename;
	}

	public boolean isCFC() {
		return isCFC;
	}
}