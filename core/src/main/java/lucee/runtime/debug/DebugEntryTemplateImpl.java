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
package lucee.runtime.debug;

import lucee.runtime.PageSource;

/**
 * a single debug entry
 */
public final class DebugEntryTemplateImpl extends DebugEntrySupport implements DebugEntryTemplate {

	private static final long serialVersionUID = 809949164432900481L;

	private long fileLoadTime;
	private String key;
	private long queryTime;

	/**
	 * constructor of the class
	 * 
	 * @param source
	 * @param key
	 */
	protected DebugEntryTemplateImpl(PageSource source, String key) {
		super(source);
		this.key = key;
	}

	@Override
	public long getFileLoadTime() {
		return positiv(fileLoadTime);
	}

	@Override
	public void updateFileLoadTime(long fileLoadTime) {
		if (fileLoadTime > 0) this.fileLoadTime += fileLoadTime;
	}

	@Override
	public void updateQueryTime(long queryTime) {
		if (queryTime > 0) this.queryTime += queryTime;
	}

	@Override
	public String getSrc() {
		return getSrc(getPath(), key);
	}

	/**
	 * @param source
	 * @param key
	 * @return Returns the src.
	 */
	static String getSrc(String path, String key) {
		return path + (key == null ? "" : "$" + key);
	}

	@Override
	public long getQueryTime() {
		return positiv(queryTime);
	}

	@Override
	public void resetQueryTime() {
		this.queryTime = 0;
	}
}