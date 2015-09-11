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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDF2ImagePDFRenderer extends PDF2Image {
	
	public PDF2ImagePDFRenderer(){
		PDFFile.class.getName();// this is needed, that the class throws a error when the PDFRenderer.jar is not in the enviroment
	}

	@Override
	public Image toImage(byte[] input, int pageNumber) throws IOException {
		
		File file = File.createTempFile("pdf2img", "pdf");
		
		try{
			copy(input,file);
			PDFFile pdffile = toPDFFile(file);
			
	        // draw a single page to an image
			PDFPage page = pdffile.getPage(pageNumber);
	        
	        return toImage(page,(int)page.getBBox().getWidth(),(int)page.getBBox().getHeight(),false);
	      
		
		}
		finally{
			deleteEL(file);
		}
	}

	private static Image toImage(PDFPage page, int width, int height,boolean transparent) {
		int w = (int)page.getBBox().getWidth();
		int h = (int)page.getBBox().getHeight();
		java.awt.Rectangle rect = new java.awt.Rectangle(0,0,w,h);
        
        BufferedImage bi = Image.toBufferedImage(page.getImage(
                width, height, //width & height
                rect, // clip rect
                null, // null for the ImageObserver
                !transparent, // fill background with white
                true  // block until drawing is done
                ));
        
        return new Image(bi);
	}

	@Override
	public void writeImages(byte[] input, Set pages, Resource outputDirectory,
			String prefix, String format, int scale, boolean overwrite,
			boolean goodQuality, boolean transparent) throws PageException,
			IOException {
		

		File file = File.createTempFile("pdf2img", "pdf");
		try{
			copy(input,file);
			PDFFile pdf = toPDFFile(file);
		
			Resource res;
			int count = pdf.getNumPages();
			 
			for(int page=1;page<=count;page++) {
				if(pages!=null && !pages.contains(Integer.valueOf(page)))continue;
				res=createDestinationResource(outputDirectory,prefix,page,format,overwrite);
				//res=outputDirectory.getRealResource(prefix+"_page_"+page+"."+format);
				writeImage(pdf,page,res,format,scale,overwrite,goodQuality, transparent);
			}
		}
		finally{
			deleteEL(file);
		}
	}
	
	private static void writeImage(PDFFile pdf, int pageNumber, Resource destination,String format, int scale,
			boolean overwrite, boolean goodQuality, boolean transparent) throws PageException, IOException {
		
		PDFPage page = pdf.getPage(pageNumber);
		
		if(scale<1) throw new ExpressionException("scale ["+scale+"] should be at least 1");
		
		int width = (int)page.getBBox().getWidth();
		int height = (int)page.getBBox().getHeight();
		if(scale!=100){
			double s=(scale)/100d;
			width=	(int)((width)*s);
			height=	(int)((height)*s);
			
			
		}
		Image img=toImage(page, width, height,transparent);
		img.writeOut(destination,format, overwrite, 1f);
	}

	private PDFFile toPDFFile(File file) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        return new PDFFile(buf);

	}

	private void deleteEL(File file) {
		try{
		if(!file.delete())file.deleteOnExit();
		}
		catch(Throwable t){}
	}

	private void copy(byte[] input, File file) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		FileOutputStream fos = new FileOutputStream(file);
		IOUtil.copy(bais, fos, true,true);
		
	}

}