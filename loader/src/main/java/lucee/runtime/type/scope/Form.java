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
package lucee.runtime.type.scope;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletInputStream;

import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;

/**
 * interface fro scope form
 */
public interface Form extends Scope {

	/**
	 * @return Returns the encoding.
	 */
	public abstract String getEncoding();

	/**
	 * @param ac current ApplicationContext
	 * @param encoding The encoding to set.
	 * @throws UnsupportedEncodingException Unsupported Encoding Exception
	 */
	public abstract void setEncoding(ApplicationContext ac, String encoding) throws UnsupportedEncodingException;

	/**
	 * @return return the exception when initialized
	 */
	public abstract PageException getInitException();

	public abstract void setScriptProtecting(ApplicationContext ac, boolean b);

	public FormItem getUploadResource(String key);

	public FormItem[] getFileItems();

	public ServletInputStream getInputStream();

	public void reinitialize(ApplicationContext ac);
}