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
package lucee.runtime.text.pdf;

import java.io.IOException;
import java.util.Set;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.SystemOut;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;

public abstract class PDF2Image {
	
	private static PDF2Image instance;
	
	public static PDF2Image getInstance()	{
		if(instance==null){
			try{
				try{
					instance=new PDF2ImageICEpdf();
					SystemOut.printDate("using ICEpdf PDF2Image  Library");
				}
				catch(Throwable t){
					instance=new PDF2ImagePDFRenderer();
					SystemOut.printDate("using PDFRenderer PDF2Image  Library");
				}
			}
			catch(Throwable t){
				instance=new PDF2ImageJPedal();
				SystemOut.printDate("using JPedal PDF2Image  Library");
			}
		}
		//return new PDF2ImageJPedal();
		return instance;
	}
	

	protected static Resource createDestinationResource(Resource dir,String prefix,int page,String format, boolean overwrite) throws ExpressionException {
		Resource res = dir.getRealResource(prefix+"_page_"+page+"."+format);
		if(res.exists()) {
			if(!overwrite)throw new ExpressionException("can't overwrite existing image ["+res+"], attribute [overwrite] is false");
		}
		return res;
	}

	
	public abstract Image toImage(byte[] input,int page) throws IOException, PageException;
	public abstract void writeImages(byte[] input,Set pages,Resource outputDirectory, String prefix,String format, int scale, boolean overwrite, boolean goodQuality,boolean transparent) throws PageException, IOException;
}