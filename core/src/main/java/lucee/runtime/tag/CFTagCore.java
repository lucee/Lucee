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
package lucee.runtime.tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.exp.PageException;

public class CFTagCore extends CFTag {

	private String name;
	private String filename;
	private String mappingName;
	private boolean isweb;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	public void set__name(String name) {
		this.name = name;
	}

	public void set__filename(String filename) {
		this.filename = filename;
	}

	public void set__isweb(boolean isweb) {
		this.isweb = isweb;
	}

	public void set__mapping(String mapping) {
		this.mappingName = mapping;
	}

	@Override
	public InitFile initFile(PageContext pageContext) throws PageException {
		return createInitFile(pageContext, isweb, filename, mappingName);

	}

	public static InitFile createInitFile(PageContext pageContext, boolean isweb, String filename, String mappingName) {
		ConfigWebPro config = (ConfigWebPro) pageContext.getConfig();
		if (StringUtil.isEmpty(mappingName)) mappingName = "mapping-tag";
		Mapping mapping = isweb ? config.getTagMapping(mappingName) : config.getServerTagMapping(mappingName);

		return new InitFile(pageContext, mapping.getPageSource(filename), filename);

	}
}