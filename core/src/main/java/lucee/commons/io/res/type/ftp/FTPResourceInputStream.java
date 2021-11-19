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
package lucee.commons.io.res.type.ftp;

import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.res.util.ResourceInputStream;

public final class FTPResourceInputStream extends ResourceInputStream {

	private final FTPResourceClient client;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 * @param is
	 */
	public FTPResourceInputStream(FTPResourceClient client, FTPResource res, InputStream is) {
		super(res, is);
		// print.ln("is:"+is);
		this.client = client;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		}
		finally {
			client.completePendingCommand();
			((FTPResourceProvider) getResource().getResourceProvider()).returnClient(client);
		}
	}

}