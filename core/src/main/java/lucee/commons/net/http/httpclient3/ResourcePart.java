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
package lucee.commons.net.http.httpclient3;

import java.io.IOException;
import java.io.OutputStream;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.util.EncodingUtil;

public class ResourcePart extends FilePart {
	protected static final String FILE_NAME = "; filename=";

    /** Attachment's file name as a byte array */
    private static final byte[] FILE_NAME_BYTES = EncodingUtil.getAsciiBytes(FILE_NAME);
    
	private Resource resource;

	private String headerCharset;

	/*public ResourcePart(String name, ResourcePartSource partSource, String contentType, String charset) {
		super(name, partSource, contentType, charset==null?"":charset);
		this.resource=partSource.getResource();
	}*/
	
	public ResourcePart(String name, ResourcePartSource partSource, String contentType, String headerCharset) {
		super(name, partSource, contentType, "");
		this.resource=partSource.getResource();
		this.headerCharset=headerCharset;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	@Override
	public String getCharSet() {
		String cs = super.getCharSet();
		if(StringUtil.isEmpty(cs)) return null;
		return cs;
	}
	

    @Override
	protected void sendDispositionHeader(OutputStream out)  throws IOException {
		sendDispositionHeader(getName(),getSource().getFileName(),headerCharset,out);
	}
	
	
    public static void sendDispositionHeader(String name,String filename, String headerCharset, OutputStream out)  throws IOException {
    	out.write(CONTENT_DISPOSITION_BYTES);
        out.write(QUOTE_BYTES);
        if(StringUtil.isAscii(name))
        	out.write(EncodingUtil.getAsciiBytes(name));
        else
        	out.write(name.getBytes(headerCharset));
        out.write(QUOTE_BYTES);

        if (filename != null) {
        	out.write(FILE_NAME_BYTES);
            out.write(QUOTE_BYTES);
            if(StringUtil.isAscii(filename))
            	out.write(EncodingUtil.getAsciiBytes(filename));
            else
            	out.write(filename.getBytes(headerCharset));
            out.write(QUOTE_BYTES);
        }
    }

	

}