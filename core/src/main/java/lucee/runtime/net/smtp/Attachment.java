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
package lucee.runtime.net.smtp;

import java.io.Serializable;
import java.net.URL;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public class Attachment implements Serializable {

	private String absolutePath;
	private URL url;
	private String type;
	private String disposition;
	private String contentID;
	private String fileName;
	private boolean removeAfterSend;

	public Attachment(Resource resource, String fileName, String type, String disposition, String contentID, boolean removeAfterSend) {
		this.absolutePath = resource.getAbsolutePath();// do not store resource, this is pehrhaps not serialiable
		this.fileName = StringUtil.isEmpty(fileName, true) ? resource.getName() : fileName.trim();
		this.removeAfterSend = removeAfterSend;
		this.disposition = disposition;
		this.contentID = contentID;

		// type
		this.type = type;
		if (StringUtil.isEmpty(type)) {
			type = IOUtil.getMimeType(resource, null);
		}
	}

	public Attachment(URL url) {
		this.url = url;

		// filename
		this.fileName = ListUtil.last(url.toExternalForm(), '/');
		if (StringUtil.isEmpty(this.fileName)) this.fileName = "url.txt";
		type = IOUtil.getMimeType(url, null);
	}

	/**
	 * @return the url
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/*
	 * *
	 * 
	 * @return the resource / public Resource getResourcex() { return resource; }
	 */

	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @return the removeAfterSend
	 */
	public boolean isRemoveAfterSend() {
		return removeAfterSend;
	}

	/**
	 * @param removeAfterSend the removeAfterSend to set
	 */
	public void setRemoveAfterSend(boolean removeAfterSend) {
		this.removeAfterSend = removeAfterSend;
	}

	// resource.getAbsolutePath()
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the disposition
	 */
	public String getDisposition() {
		return disposition;
	}

	/**
	 * @return the contentID
	 */
	public String getContentID() {
		return contentID;
	}
}