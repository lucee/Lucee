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
package lucee.runtime.cache.eh.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import lucee.commons.io.IOUtil;

public class Converter {
	public static Object toObject(String contentType,InputStream is) throws IOException, ClassNotFoundException {
		try	{
	    	if("application/x-java-serialized-object".equals(contentType)){
	    		ObjectInputStream ois=new ObjectInputStream(is);
	    		return ois.readObject();
		    }
		    // other
		    return IOUtil.toString(is,(Charset)null);
		}
    	finally	{
    		IOUtil.closeEL(is);
    	}
	}
	
	public static byte[] toBytes(Object value) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(); // returns
	    ObjectOutputStream oos = new ObjectOutputStream(os);
	    oos.writeObject(value);
	    oos.flush();
	    return os.toByteArray();
	}
}