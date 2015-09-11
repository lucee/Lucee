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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.JAIUtil;
import lucee.runtime.img.PSDReader;

class JRECoder extends Coder {
	

	private String[] writerFormatNames;
	private String[] readerFormatNames;
	
	protected JRECoder(){
		super();
	}
	
	/**
	 * translate a file resource to a buffered image
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(Resource res,String format) throws IOException {
		if(StringUtil.isEmpty(format))format=ImageUtil.getFormat(res);
		if("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			InputStream is=null;
			try {
				reader.read(is=res.getInputStream());
				return reader.getImage();
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		if(JAIUtil.isSupportedReadFormat(format)){
			return JAIUtil.read(res);
		}
		
		BufferedImage img=null;
		InputStream is=null;
		try {
			img = ImageIO.read(is=res.getInputStream());
		}
		finally {
			IOUtil.closeEL(is);
		}
		
		if(img==null && StringUtil.isEmpty(format)) {
			return JAIUtil.read(res);
		}
		return img;
	}

	/**
	 * translate a binary array to a buffered image
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(byte[] bytes,String format) throws IOException {
		if(StringUtil.isEmpty(format))format=ImageUtil.getFormat(bytes,null);
		if("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			reader.read(new ByteArrayInputStream(bytes));
			return reader.getImage();
		}
		if(JAIUtil.isSupportedReadFormat(format)){
			return JAIUtil.read(new ByteArrayInputStream(bytes),format);
		}
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
		if(img==null && StringUtil.isEmpty(format))
			return JAIUtil.read(new ByteArrayInputStream(bytes),null);
		return img;
	}
	
	@Override
	public final String[] getWriterFormatNames() {
		if(writerFormatNames==null)	{
			String[] iio = ImageIO.getWriterFormatNames();
			String[] jai = JAIUtil.isJAISupported()?JAIUtil.getSupportedWriteFormat():null;
			writerFormatNames=mixTogetherOrdered(iio,jai);
		}
		return writerFormatNames;
	}
	@Override
	public final String[] getReaderFormatNames() {
		if(readerFormatNames==null){
			String[] iio = ImageIO.getReaderFormatNames();
			String[] jai = JAIUtil.isJAISupported()?JAIUtil.getSupportedReadFormat():null;
			readerFormatNames=mixTogetherOrdered(iio,jai);
		}
		return readerFormatNames;
	}
	
	public static final String[] mixTogetherOrdered(String[] names1,String[] names2) {
		Set<String> set=new HashSet<String>();
		
		if(names1!=null)for(int i=0;i<names1.length;i++){
			set.add(names1[i].toLowerCase());
		}
		if(names2!=null)for(int i=0;i<names2.length;i++){
			set.add(names2[i].toLowerCase());
		}
		
		names1= set.toArray(new String[set.size()]);
		Arrays.sort(names1);
		return names1;
	}
}