/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.img;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStreamImpl;

import lucee.commons.io.res.Resource;

public class ResourceImageOutputStream extends ImageOutputStreamImpl {

    private Resource res;
	private OutputStream os;

    public ResourceImageOutputStream(Resource res) throws IOException {
    	this.res=res;
    	os=res.getOutputStream();
    }
    public ResourceImageOutputStream(OutputStream os) {
    	this.os=os;
    }

    @Override
	public int read() throws IOException {
    	throw new IOException("not supported");
    }

    @Override
	public int read(byte[] b, int off, int len) throws IOException {
    	throw new IOException("not supported");
    }

    @Override
	public void write(int b) throws IOException {
    	os.write(b);
    }

    @Override
	public void write(byte[] b, int off, int len) throws IOException {
    	os.write(b,off,len);
    }

    @Override
	public long length() {
    	if(res==null) throw new RuntimeException("not supported");
        return res.length();
    }

    /**
     * Sets the current stream position and resets the bit offset to
     * 0.  It is legal to seeking past the end of the file; an
     * <code>EOFException</code> will be thrown only if a read is
     * performed.  The file length will not be increased until a write
     * is performed.
     *
     * @exception IndexOutOfBoundsException if <code>pos</code> is smaller
     * than the flushed position.
     * @exception IOException if any other I/O error occurs.
     */
    @Override
	public void seek(long pos) throws IOException {
    	throw new IOException("not supported");
    }

    @Override
    public void close() throws IOException {
    	try {
            super.close();
    	}
    	finally {
            os.close();
    	}
    }
}