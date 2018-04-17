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

/**
 * a single result item
 */
public interface SearchResulItem {

	/**
	 * @return Returns the recordsSearched.
	 */
	public abstract int getRecordsSearched();

	/**
	 * @return Returns the score.
	 */
	public abstract float getScore();

	/**
	 * @return Returns the summary.
	 */
	public abstract String getSummary();

	/**
	 * @return Returns the title.
	 */
	public abstract String getTitle();

	/**
	 * @return Returns the id.
	 */
	public abstract String getId();

	/**
	 * @return Returns the key
	 */
	public abstract String getKey();

	/**
	 * @return Returns the url
	 */
	public abstract String getUrl();

	/**
	 * @deprecated use instead <code>getCustom(int index)</code>
	 * @return Returns the custom1.
	 */
	@Deprecated
	public abstract String getCustom1();

	/**
	 * @deprecated use instead <code>getCustom(int index)</code>
	 * @return Returns the custom2.
	 */
	@Deprecated
	public abstract String getCustom2();

	/**
	 * @deprecated use instead <code>getCustom(int index)</code>
	 * @return Returns the custom3.
	 */
	@Deprecated
	public abstract String getCustom3();

	/**
	 * @deprecated use instead <code>getCustom(int index)</code>
	 * @return Returns the custom4.
	 */
	@Deprecated
	public abstract String getCustom4();

	public abstract String getCustom(int index) throws SearchException;

	/**
	 * @return the category
	 */
	public String getCategory();

	/**
	 * @return the categoryTree
	 */
	public String getCategoryTree();

	/**
	 * @return the mimeType
	 */
	public String getMimeType();

	/**
	 * @return the author
	 */
	public String getAuthor();

	/**
	 * @return the size
	 */
	public String getSize();

	/**
	 * @return the contextSummary
	 */
	public String getContextSummary();

}