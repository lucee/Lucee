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
package lucee.runtime.writer;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.jsp.JspWriter;

import lucee.runtime.cache.legacy.CacheItem;

public abstract class CFMLWriter extends JspWriter {

	protected CFMLWriter(int bufferSize, boolean autoFlush) {
		super(bufferSize, autoFlush);
	}

	public abstract OutputStream getResponseStream() throws IOException;

	public abstract void setClosed(boolean closed); // do not change used in p d f extension

	public abstract void setBufferConfig(int interval, boolean b) throws IOException;

	public abstract void appendHTMLBody(String text) throws IOException;

	public abstract void writeHTMLBody(String text) throws IOException;

	public abstract void flushHTMLBody() throws IOException;

	public abstract String getHTMLBody() throws IOException;

	public abstract void resetHTMLBody() throws IOException;

	public abstract void appendHTMLHead(String text) throws IOException;

	public abstract void writeHTMLHead(String text) throws IOException;

	public abstract void flushHTMLHead() throws IOException;

	public abstract String getHTMLHead() throws IOException;

	public abstract void resetHTMLHead() throws IOException;

	/**
	 * write the given string without removing whitespace.
	 * 
	 * @param str
	 * @throws IOException
	 */
	public abstract void writeRaw(String str) throws IOException;

	public abstract void setAllowCompression(boolean allowCompression);

	public abstract void doCache(lucee.runtime.cache.legacy.CacheItem ci);

	/**
	 * @return the cacheResource
	 */
	public abstract CacheItem getCacheItem();

}