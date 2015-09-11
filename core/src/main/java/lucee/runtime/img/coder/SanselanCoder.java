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
package lucee.runtime.img.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

class SanselanCoder extends Coder {
	
	private String[] writerFormatNames=new String[]{"PNG","GIF","TIFF","JPEG","BMP","PNM","PGM","PBM","PPM","XMP"};
	private String[] readerFormatNames=new String[]{"PNG","GIF","TIFF","JPEG","BMP","PNM","PGM","PBM","PPM","XMP" ,"ICO","PSD"};
	
	protected SanselanCoder(){
		super();
		Sanselan.hasImageFileExtension("lucee.gif");// to make sure Sanselan exist when load this class
	}
	
	/**
	 * translate a file resource to a buffered image
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(Resource res,String format) throws IOException {
		InputStream is=null;
		try {
			return Sanselan.getBufferedImage(is=res.getInputStream());
		} 
		catch (ImageReadException e) {
			throw ExceptionUtil.toIOException(e);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	/**
	 * translate a binary array to a buffered image
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(byte[] bytes,String format) throws IOException {
		try {
			return Sanselan.getBufferedImage(new ByteArrayInputStream(bytes));
		} 
		catch (ImageReadException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public String[] getWriterFormatNames() {
		return writerFormatNames;
	}

	@Override
	public String[] getReaderFormatNames() {
		return readerFormatNames;
	}
}