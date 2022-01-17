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
package lucee.runtime.exp;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Exception thrown when missing include
 */
public final class MissingIncludeException extends PageExceptionImpl {

	private static final Collection.Key MISSING_FILE_NAME = KeyImpl.getInstance("MissingFileName");
	private static final Collection.Key MISSING_FILE_NAME_REL = KeyImpl.getInstance("MissingFileName_rel");
	private static final Collection.Key MISSING_FILE_NAME_ABS = KeyImpl.getInstance("MissingFileName_abs");

	private PageSource pageSource;

	/**
	 * constructor of the exception
	 * 
	 * @param pageSource
	 */
	public MissingIncludeException(PageSource pageSource) {
		super(createMessage(pageSource), "missinginclude");
		setDetail(pageSource);
		this.pageSource = pageSource;

	}

	public MissingIncludeException(PageSource pageSource, String msg) {
		super(msg, "missinginclude");
		setDetail(pageSource);
		this.pageSource = pageSource;

	}

	private void setDetail(PageSource ps) {
		setAdditional(KeyConstants._Mapping, ps.getMapping().getVirtual());
	}

	/**
	 * @return the pageSource
	 */
	public PageSource getPageSource() {
		return pageSource;
	}

	private static String createMessage(PageSource pageSource) {
		String dsp = pageSource.getDisplayPath();
		if (dsp == null) return "Page " + pageSource.getRealpathWithVirtual() + " not found";
		return "Page " + pageSource.getRealpathWithVirtual() + " [" + dsp + "] not found";
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock sct = super.getCatchBlock(config);
		String mapping = "";
		if (StringUtil.startsWith(pageSource.getRealpath(), '/')) {
			mapping = pageSource.getMapping().getVirtual();
			if (StringUtil.endsWith(mapping, '/')) mapping = mapping.substring(0, mapping.length() - 1);
		}
		sct.setEL(MISSING_FILE_NAME, mapping + pageSource.getRealpath());

		sct.setEL(MISSING_FILE_NAME_REL, mapping + pageSource.getRealpath());
		sct.setEL(MISSING_FILE_NAME_ABS, pageSource.getDisplayPath());
		return sct;
	}

	@Override
	public boolean typeEqual(String type) {
		if (super.typeEqual(type)) return true;
		type = type.toLowerCase().trim();
		return type.equals("template");
	}
}