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

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

public class PDF2ImageJPedal extends PDF2Image {
	
	
	@Override
	public void writeImages(byte[] input,Set pages,Resource outputDirectory, String prefix,String format, int scale,
			 boolean overwrite, boolean goodQuality,boolean transparent) throws  PageException, IOException {
		 PdfDecoder dec = createPdfDecoder(input);
		 Resource res;
		 int count = dec.getPageCount();
		 
		 for(int page=1;page<=count;page++) {
			 if(pages!=null && !pages.contains(Integer.valueOf(page)))continue;
			 //res=outputDirectory.getRealResource(prefix+"_page_"+page+"."+format);
			 res=createDestinationResource(outputDirectory,prefix,page,format,overwrite);
			 writeImage(dec,page,res,format,scale,overwrite,goodQuality, transparent);
		 }

	 }



	private static void writeImage(PdfDecoder dec, int page, Resource destination,String format, int scale,
			boolean overwrite, boolean goodQuality, boolean transparent) throws PageException, IOException {
		if(scale<1 || scale>100) 
			throw new ExpressionException("invalid scale definition ["+Caster.toString(scale)+"], value should be in range from 1 to 100");
		
		
		Image img=null;
		try {
			img = new Image(transparent?dec.getPageAsTransparentImage(page):dec.getPageAsImage(page));
		} catch (PdfException e) {
			throw Caster.toPageException(e);
		}
		if(scale!=100)
			img.resize(scale, goodQuality?"highestquality":"highperformance", 1);
		img.writeOut(destination,format, overwrite, 1f);
	}


	@Override
	public Image toImage(byte[] input,int page) throws IOException, PageException {
		 try {
			return new Image(createPdfDecoder(input).getPageAsImage(page));
		} catch (PdfException e) {
			throw Caster.toPageException(e);
		}
	}

	private static PdfDecoder createPdfDecoder(Resource res) throws PageException,IOException {
		return createPdfDecoder(IOUtil.toBytes(res));
	}
	 
	private static PdfDecoder createPdfDecoder(byte[] input) throws PageException  {
		 PdfDecoder decoder = new PdfDecoder(true);
		 decoder.useHiResScreenDisplay(true);
		 try {
			decoder.openPdfArray(input);
		} catch (PdfException e) {
			throw Caster.toPageException(e);
		}
		 return decoder;
	}

}