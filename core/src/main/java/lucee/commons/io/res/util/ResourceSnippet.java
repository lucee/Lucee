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
package lucee.commons.io.res.util;

import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.res.Resource;

/**
 * this class holds information about a snippet from text with its start and end line numbers
 */
public class ResourceSnippet implements java.io.Serializable {

	private String text = null;
	private int startLine = 0, endLine = 0;

	public final static ResourceSnippet Empty = new ResourceSnippet("", 0, 0);

	public ResourceSnippet(String text, int startLine, int endLine) {

		this.text = text;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	/** returns the actual text of the snippet */
	public String getContent() {

		return text;
	}

	/** returns the start line number */
	public int getStartLine() {

		return startLine;
	}

	/** returns the end line number */
	public int getEndLine() {

		return endLine;
	}

	public static String getContents(InputStream is, String charset) {

		String result;

		java.util.Scanner scanner = new java.util.Scanner(is, charset).useDelimiter("\\A");
		result = scanner.hasNext() ? scanner.next() : "";

		if (is != null) try {

			is.close();
		}
		catch (IOException ex) {
		}

		return result;
	}

	public static String getContents(Resource res, String charset) {

		try {

			return getContents(res.getInputStream(), charset);
		}
		catch (IOException ex) {

			return "";
		}
	}

	public static ResourceSnippet createResourceSnippet(String src, int startChar, int endChar) {

		String text = "";
		if (endChar > startChar && endChar <= src.length()) text = src.substring(startChar, endChar);

		return new ResourceSnippet(text, getLineNumber(src, startChar), getLineNumber(src, endChar));
	}

	/**
	 * extract a ResourceSnippet from InputStream at the given char positions
	 *
	 * @param is - InputStream of the Resource
	 * @param startChar - start position of the snippet
	 * @param endChar - end position of the snippet
	 * @param charset - use server's charset, default should be UTF-8
	 * @return
	 */
	public static ResourceSnippet createResourceSnippet(InputStream is, int startChar, int endChar, String charset) {

		return createResourceSnippet(getContents(is, charset), startChar, endChar);
	}

	/**
	 * extract a ResourceSnippet from a Resource at the given char positions
	 *
	 * @param res - Resource from which to extract the snippet
	 * @param startChar - start position of the snippet
	 * @param endChar - end position of the snippet
	 * @param charset - use server's charset, default should be UTF-8
	 * @return
	 */
	public static ResourceSnippet createResourceSnippet(Resource res, int startChar, int endChar, String charset) {

		try {

			return createResourceSnippet(res.getInputStream(), startChar, endChar, charset);
		}
		catch (IOException ex) {

			return ResourceSnippet.Empty;
		}
	}

	/** returns the line number of the given char in the text */
	public static int getLineNumber(String text, int posChar) {

		int len = Math.min(posChar, text.length());
		int result = 1;

		for (int i = 0; i < len; i++) {

			if (text.charAt(i) == '\n') result++;
		}

		return result;
	}
}