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
package lucee.runtime.search;

public class AddionalAttrs {

	private static ThreadLocal addAttrs = new ThreadLocal();
	private int contextBytes;
	private String contextHighlightBegin;
	private int contextPassages;
	private String contextHighlightEnd;
	private int startrow = 1;
	private int maxrows = -1;
	private boolean hasRowHandling;

	public AddionalAttrs(int contextBytes, int contextPassages, String contextHighlightBegin, String contextHighlightEnd) {
		this.contextBytes = contextBytes;
		this.contextPassages = contextPassages;
		this.contextHighlightBegin = contextHighlightBegin;
		this.contextHighlightEnd = contextHighlightEnd;
	}

	public static AddionalAttrs getAddionlAttrs() {
		AddionalAttrs aa = (AddionalAttrs) addAttrs.get();
		if (aa == null) aa = new AddionalAttrs(300, 0, "<b>", "</b>");
		return aa;
	}

	public static void setAddionalAttrs(AddionalAttrs aa) {
		addAttrs.set(aa);
	}

	public static void setAddionalAttrs(int contextBytes, int contextPassages, String contextHighlightBegin, String contextHighlightEnd) {
		setAddionalAttrs(new AddionalAttrs(contextBytes, contextPassages, contextHighlightBegin, contextHighlightEnd));
	}

	public static void removeAddionalAttrs() {
		addAttrs.set(null);
	}

	/**
	 * @return the contextBytes
	 */
	public int getContextBytes() {
		return contextBytes;
	}

	/**
	 * @return the contextHighlightBegin
	 */
	public String getContextHighlightBegin() {
		return contextHighlightBegin;
	}

	/**
	 * @return the contextPassages
	 */
	public int getContextPassages() {
		return contextPassages;
	}

	/**
	 * @return the contextHighlightEnd
	 */
	public String getContextHighlightEnd() {
		return contextHighlightEnd;
	}

	/**
	 * @return the startrow
	 */
	public int getStartrow() {
		return startrow;
	}

	/**
	 * @param startrow the startrow to set
	 */
	public void setStartrow(int startrow) {
		this.startrow = startrow;
	}

	/**
	 * @return the maxrows
	 */
	public int getMaxrows() {
		return maxrows;
	}

	/**
	 * @param maxrows the maxrows to set
	 */
	public void setMaxrows(int maxrows) {
		this.maxrows = maxrows;
	}

	public boolean hasRowHandling() {
		return hasRowHandling;
	}

	public void setHasRowHandling(boolean hasRowHandling) {
		this.hasRowHandling = hasRowHandling;
	}
}