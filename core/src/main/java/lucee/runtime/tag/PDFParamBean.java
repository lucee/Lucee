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

public class PDFParamBean {
	private String pages;
	private String password;
	private Object source;
	/**
	 * @return the pages
	 */
	public String getPages() {
		return pages;
	}
	/**
	 * @param pages the pages to set
	 */
	public void setPages(String pages) {
		this.pages = pages;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(Object source) {
		this.source = source;
	}
}