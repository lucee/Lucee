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
package lucee.runtime.converter.bin;

import java.io.IOException;
import java.io.OutputStream;

import lucee.runtime.PageContext;
import lucee.runtime.converter.BinaryConverter;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.ConverterSupport;
import lucee.runtime.img.Image;

public class ImageConverter implements BinaryConverter {
	
	private final String format;

	public ImageConverter(String format){
		this.format=format;
	}
	
	@Override
	public void writeOut(PageContext pc,Object source, OutputStream os) throws ConverterException, IOException {
		try {
			Image img = Image.createImage(pc, source, false, true,true,format);
			img.writeOut(os, format, 1, false);
		} 
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			throw ConverterSupport.toConverterException(e);
		}
		
	}
	
	
}