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

public class DebugEntryTemplatePartImpl extends DebugEntrySupport implements DebugEntryTemplatePart {

	private int startPos, startLine;
	private int endPos, endLine;
	private String snippet = "";

	protected DebugEntryTemplatePartImpl(PageSource source, int startPos, int endPos) {
		super(source);
		this.startPos = startPos;
		this.endPos = endPos;
	}

	protected DebugEntryTemplatePartImpl(PageSource source, int startPos, int endPos, int startLine, int endLine, String snippet) {
		super(source);
		this.startPos = startPos;
		this.endPos = endPos;
		this.startLine = startLine;
		this.endLine = endLine;
		this.snippet = snippet;
	}

	@Override
	public String getSrc() {
		return getSrc(getPath(), startPos, endPos);
	}

	@Override
	public int getStartPosition() {
		return startPos;
	}

	@Override
	public int getEndPosition() {
		return endPos;
	}

	static String getSrc(String path, int startPos, int endPos) {
		return path + ":" + startPos + " - " + endPos;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}

	@Override
	public String getSnippet() {
		return snippet;
	}
}