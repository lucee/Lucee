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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.img.coder.Coder;

import org.apache.commons.codec.binary.Base64;

public class ImageUtil {
	
	private static Coder coder;
	
	static {
		coder = Coder.getInstance();
	}
	


	public static String[] getWriterFormatNames() {
		return coder.getWriterFormatNames();
	}
	public static String[] getReaderFormatNames() {
		return coder.getReaderFormatNames();
	}
	

	/**
	 * translate a file resource to a buffered image
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage toBufferedImage(Resource res,String format) throws IOException {
		return coder.toBufferedImage(res, format);
	}

	/**
	 * translate a binary array to a buffered image
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage toBufferedImage(byte[] bytes,String format) throws IOException {
		return coder.toBufferedImage(bytes, format);
	}

	
	
	

	public static byte[] readBase64(String b64str, StringBuilder mimetype) throws IOException {
		if(StringUtil.isEmpty(b64str))
			throw new IOException("base64 string is empty");
		
		// data:image/png;base64,
		int index = b64str.indexOf("base64,");
		if(index!=-1){
			int semiIndex=b64str.indexOf(";");
			if(mimetype!=null && semiIndex<index && StringUtil.startsWithIgnoreCase(b64str, "data:")){
				mimetype.append(b64str.substring(5,semiIndex).trim());
			}
			
			b64str=b64str.substring(index + 7);
		}
		
		return Base64.decodeBase64(b64str.getBytes());
	}
	
	public static String getFormat(Resource res) throws IOException {
		String ext=getFormatFromExtension(res,null);
		if(ext!=null) return ext;
		String mt=ResourceUtil.getMimeType(res, null);
		if(mt==null) return null;//throw new IOException("can't extract mimetype from ["+res+"]");
		return getFormatFromMimeType(mt);
	}

	public static String getFormat(byte[] binary) throws IOException {
		return getFormatFromMimeType(IOUtil.getMimeType(binary, ""));
	}

	public static String getFormat(byte[] binary,String defaultValue) {
		return getFormatFromMimeType(IOUtil.getMimeType(binary, ""),defaultValue);
	}

	public static String getFormatFromExtension(Resource res, String defaultValue) {
		String ext=ResourceUtil.getExtension(res,null);
		if("gif".equalsIgnoreCase(ext))return "gif";
		if("jpg".equalsIgnoreCase(ext))return "jpg";
		if("jpe".equalsIgnoreCase(ext))return "jpg";
		if("jpeg".equalsIgnoreCase(ext))return "jpg";
		if("png".equalsIgnoreCase(ext))return "png";
		if("tiff".equalsIgnoreCase(ext))return "tiff";
		if("tif".equalsIgnoreCase(ext))return "tiff";
		if("bmp".equalsIgnoreCase(ext))return "bmp";
		if("bmp".equalsIgnoreCase(ext))return "bmp";
		if("wbmp".equalsIgnoreCase(ext))return "wbmp";
		if("ico".equalsIgnoreCase(ext))return "bmp";
		if("wbmp".equalsIgnoreCase(ext))return "wbmp";
		if("psd".equalsIgnoreCase(ext))return "psd";
		if("fpx".equalsIgnoreCase(ext))return "fpx";
		
		if("pnm".equalsIgnoreCase(ext))return "pnm";
		if("pgm".equalsIgnoreCase(ext))return "pgm";
		if("pbm".equalsIgnoreCase(ext))return "pbm";
		if("ppm".equalsIgnoreCase(ext))return "ppm";
		return defaultValue;	
	}
	
	
	
	
	public static String getFormatFromMimeType(String mt) throws IOException {
		String format = getFormatFromMimeType(mt, null);
		if(format!=null) return format;
		
		if(StringUtil.isEmpty(mt))throw new IOException("cannot find Format of given image");//31
		throw new IOException("can't find Format ("+mt+") of given image");
	}
	
	public static String getFormatFromMimeType(String mt, String defaultValue) {
		if("image/gif".equals(mt)) return "gif";
		if("image/gi_".equals(mt)) return "gif";
		
		if("image/jpeg".equals(mt)) return "jpg";
		if("image/jpg".equals(mt)) return "jpg";
		if("image/jpe".equals(mt)) return "jpg";
		if("image/pjpeg".equals(mt)) return "jpg";
		if("image/vnd.swiftview-jpeg".equals(mt)) return "jpg";
		if("image/pipeg".equals(mt)) return "jpg";
		if("application/x-jpg".equals(mt)) return "jpg";
		if("application/jpg".equals(mt)) return "jpg";
		if("image/jp_".equals(mt)) return "jpg";
		
		if("image/png".equals(mt)) return "png";
		if("image/x-png".equals(mt))return "png";
		if("application/x-png".equals(mt)) return "png";
		if("application/png".equals(mt)) return "png";
		
		if("image/tiff".equals(mt)) return "tiff";
		if("image/tif".equals(mt)) return "tiff";
		if("image/x-tif".equals(mt)) return "tiff";
		if("image/x-tiff".equals(mt)) return "tiff";
		if("application/tif".equals(mt)) return "tiff";
		if("application/x-tif".equals(mt)) return "tiff";
		if("application/tiff".equals(mt)) return "tiff";
		if("application/x-tiff".equals(mt)) return "tiff";
		
		if("image/bmp".equals(mt)) return "bmp";
		if("image/vnd.wap.wbmp".equals(mt)) return "wbmp";
		
		if("image/fpx".equals(mt)) return "fpx";
		if("image/x-fpx".equals(mt)) return "fpx";
		if("image/vnd.fpx".equals(mt)) return "fpx";
		if("image/vnd.netfpx".equals(mt)) return "fpx";
		if("image/vnd.fpx".equals(mt)) return "fpx";
		if("application/vnd.netfpx".equals(mt)) return "fpx";
		if("application/vnd.fpx".equals(mt)) return "fpx";
		
		if("image/x-portable-anymap".equals(mt)) return "pnm";
		if("image/x-portable/anymap".equals(mt)) return "pnm";
		if("image/x-pnm".equals(mt)) return "pnm";
		if("image/pnm".equals(mt)) return "pnm";
		
		if("image/x-portable-graymap".equals(mt)) return "pgm";
		if("image/x-portable/graymap".equals(mt)) return "pgm";
		if("image/x-pgm".equals(mt)) return "pgm";
		if("image/pgm".equals(mt)) return "pgm";
		
		if("image/portable bitmap".equals(mt)) return "pbm";
		if("image/x-portable-bitmap".equals(mt)) return "pbm";
		if("image/x-portable/bitmap".equals(mt)) return "pbm";
		if("image/x-pbm".equals(mt)) return "pbm";
		if("image/pbm".equals(mt)) return "pbm";
		
		if("image/x-portable-pixmap".equals(mt)) return "ppm";
		if("application/ppm".equals(mt)) return "ppm";
		if("application/x-ppm".equals(mt)) return "ppm";
		if("image/x-p".equals(mt)) return "ppm";
		if("image/x-ppm".equals(mt)) return "ppm";
		if("image/ppm".equals(mt)) return "ppm";

		if("image/ico".equals(mt)) return "ico";
		if("image/x-icon".equals(mt)) return "ico";
		if("application/ico".equals(mt)) return "ico";
		if("application/x-ico".equals(mt)) return "ico";

		if("image/photoshop".equals(mt)) return "psd";
		if("image/x-photoshop".equals(mt)) return "psd";
		if("image/psd".equals(mt)) return "psd";
		if("application/photoshop".equals(mt)) return "psd";
		if("application/psd".equals(mt)) return "psd";
		if("zz-application/zz-winassoc-psd".equals(mt)) return "psd";
		
		// can not terminate this types exactly
		// image/x-xbitmap
		// application/x-win-bitmap
		// image/x-win-bitmap
		// application/octet-stream
		return defaultValue;
	}
	
	
	public static String getMimeTypeFromFormat(String mt) throws IOException {
		if("gif".equals(mt)) return "image/gif";
		if("jpeg".equals(mt)) return "image/jpg";
		if("jpg".equals(mt)) return "image/jpg";
		if("jpe".equals(mt)) return "image/jpg";
		if("png".equals(mt)) return "image/png";
		if("tiff".equals(mt)) return "image/tiff";
		if("tif".equals(mt)) return "image/tiff";
		if("bmp".equals(mt)) return "image/bmp";
		if("bmp".equals(mt)) return "image/bmp";
		if("wbmp".equals(mt)) return "image/vnd.wap.wbmp";
		if("fpx".equals(mt)) return "image/fpx";
		
		if("pgm".equals(mt)) return "image/x-portable-graymap";
		if("pnm".equals(mt)) return "image/x-portable-anymap";
		if("pbm".equals(mt)) return "image/x-portable-bitmap";
		if("ppm".equals(mt)) return "image/x-portable-pixmap";

		if("ico".equals(mt)) return "image/ico";
		if("psd".equals(mt)) return "image/psd";
		
		if(StringUtil.isEmpty(mt))throw new IOException("can't find Format of given image");//31
		throw new IOException("can't find Format ("+mt+") of given image");
	}

	public static void closeEL(ImageInputStream iis) {
		 try {
    		 if(iis!=null)iis.close();
    	 } 
    	 catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		
		
	}

	public static BufferedImage createBufferedImage(BufferedImage image, int columns, int rows) {
        ColorModel colormodel = image.getColorModel();
        BufferedImage newImage;
        if(colormodel instanceof IndexColorModel) {
            if(colormodel.getTransparency() != 1)
                newImage = new BufferedImage(columns, rows, 2);
            else
                newImage = new BufferedImage(columns, rows, 1);
        } 
        else {
            newImage = new BufferedImage(colormodel, image.getRaster().createCompatibleWritableRaster(columns, rows), colormodel.isAlphaPremultiplied(), null);
        }
        return newImage;
    }
    
    public static BufferedImage createBufferedImage(BufferedImage image) {
        return createBufferedImage(image, image.getWidth(), image.getHeight());
    }
}