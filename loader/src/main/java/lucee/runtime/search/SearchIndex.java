/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.search;

/**
 */
public interface SearchIndex {

	/**
	 * Field <code>TYPE_FILE</code>
	 */
	public static final short TYPE_FILE = 0;
	/**
	 * Field <code>TYPE_PATH</code>
	 */
	public static final short TYPE_PATH = 1;
	/**
	 * Field <code>TYPE_CUSTOM</code>
	 */
	public static final short TYPE_CUSTOM = 2;
	/**
	 * Field <code>TYPE_URL</code>
	 */
	public static final short TYPE_URL = 3;

	/**
	 * @return Returns the custom1.
	 */
	public String getCustom1();

	/**
	 * @return Returns the custom2.
	 */
	public String getCustom2();

	/**
	 * @return Returns the custom3.
	 */
	public String getCustom3();

	/**
	 * @return Returns the custom4.
	 */
	public String getCustom4();

	/**
	 * @return Returns the extensions.
	 */
	public String[] getExtensions();

	/**
	 * @return Returns the key.
	 */
	public String getKey();

	/**
	 * @return Returns the language.
	 */
	public String getLanguage();

	/**
	 * @return Returns the title.
	 */
	public String getTitle();

	/**
	 * @return Returns the type.
	 */
	public short getType();

	/**
	 * @return Returns the id.
	 */
	public String getId();

	/**
	 * @param id The id to set. / public void setId(String id) { this.id = id; }
	 */

	/**
	 * @return Returns the urlpath.
	 */
	public String getUrlpath();

	/**
	 * @return Returns the query.
	 */
	public String getQuery();

	/**
	 * @return the categories
	 */
	public String[] getCategories();

	/**
	 * @return the categoryTree
	 */
	public String getCategoryTree();
}