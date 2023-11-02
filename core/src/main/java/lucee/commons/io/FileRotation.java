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
package lucee.commons.io;

import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;

public class FileRotation {
	public static void checkFile(Resource res, long maxFileSize, int maxFiles, byte[] header) throws IOException {
		boolean writeHeader = false;
		// create file
		if (!res.exists()) {
			res.createFile(true);
			writeHeader = true;
		}
		else if (res.length() == 0) {
			writeHeader = true;
		}

		// create new file
		else if (res.length() > maxFileSize) {
			Resource parent = res.getParentResource();
			String name = res.getName();
			int lenMaxFileSize = ("" + maxFiles).length();
			for (int i = maxFiles; i > 0; i--) {

				Resource to = parent.getRealResource(name + "." + StringUtil.addZeros(i, lenMaxFileSize) + ".bak");
				Resource from = parent.getRealResource(name + "." + StringUtil.addZeros(i - 1, lenMaxFileSize) + ".bak");
				if (from.exists()) {
					if (to.exists()) to.delete();
					from.renameTo(to);
				}
			}
			res.renameTo(parent.getRealResource(name + "." + StringUtil.addZeros(1, lenMaxFileSize) + ".bak"));
			res = parent.getRealResource(name);// new File(parent,name);
			res.createNewFile();
			writeHeader = true;
		}
		else if (header != null && header.length > 0) {
			byte[] buffer = new byte[header.length];
			int len;
			InputStream in = null;
			try {
				in = res.getInputStream();
				boolean headerOK = true;
				len = in.read(buffer);
				if (len == header.length) {
					for (int i = 0; i < header.length; i++) {
						if (header[i] != buffer[i]) {
							headerOK = false;
							break;
						}
					}
				}
				else headerOK = false;
				if (!headerOK) writeHeader = true;
			}
			finally {
				IOUtil.close(in);
			}
		}

		if (writeHeader) {
			if (header == null) header = new byte[0];
			IOUtil.write(res, header, false);

		}
	}
}