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
package lucee.runtime.img;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

public class ImageMetaDrew {

	/**
	 * adds information about a image to the given struct
	 * @param info
	 * @throws PageException 
	 * @throws IOException 
	 * @throws MetadataException 
	 * @throws JpegProcessingException 
	 */
	public static void addInfo(String format, Resource res, Struct info) {
		if("jpg".equalsIgnoreCase(format))jpg(res, info);
		else if("tiff".equalsIgnoreCase(format))tiff(res, info);
		
	}

	private static void jpg(Resource res,Struct info) {
		InputStream is=null;
		try {
			is = res.getInputStream();
			fill(info,JpegMetadataReader.readMetadata(is));
		}
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			//throw Caster.toPageException(t);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}
	
	private static void tiff(Resource res,Struct info) {
		InputStream is=null;
		try {
			is = res.getInputStream();
			fill(info,TiffMetadataReader.readMetadata(is));
		}
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			//throw Caster.toPageException(t);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	private static void fill(Struct info,Metadata metadata) {
		Iterator<Directory> directories = metadata.getDirectories().iterator();
		while (directories.hasNext()) {
		    Directory directory = directories.next();
		    Struct sct=new StructImpl();
		    info.setEL(KeyImpl.init(directory.getName()), sct);
		    
		    Iterator<Tag> tags = directory.getTags().iterator();
		    while (tags.hasNext()) {
		        Tag tag = tags.next();
		        sct.setEL(KeyImpl.init(tag.getTagName()), tag.getDescription());
		    }
		}
	}

	public static void test() {
		// to not delete, this methd is called to test if the jar exists
		
	}

	

}