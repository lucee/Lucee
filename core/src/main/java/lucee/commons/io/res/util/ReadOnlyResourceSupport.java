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
import java.io.OutputStream;

public abstract class ReadOnlyResourceSupport extends ResourceSupport {

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		throw new IOException("this is a read-only resource, can't create directory [" + this + "]");
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		throw new IOException("this is a read-only resource, can't create file [" + this + "]");
	}

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public void remove(boolean force) throws IOException {
		throw new IOException("this is a read-only resource, can't remove [" + this + "]");

	}

	@Override
	public boolean setLastModified(long time) {
		return false;
	}

	@Override
	public void setMode(int mode) throws IOException {
		throw new IOException("this is a read-only resource, can't change mode of [" + this + "]");
	}

	@Override
	public boolean setReadable(boolean value) {
		// throw new IOException("this is a read-only resource, can't change access of ["+this+"]");
		return false;
	}

	@Override
	public boolean setWritable(boolean value) {
		// throw new IOException("this is a read-only resource, can't change access of ["+this+"]");
		return false;
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		throw new IOException("this is a read-only resource, can't write to it [" + this + "]");
	}

	@Override
	public int getMode() {
		return 0444;
	}
}