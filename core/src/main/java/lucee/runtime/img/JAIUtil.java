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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.IDGenerator;
import lucee.commons.lang.StringUtil;

public class JAIUtil {

	private static Class _RenderedOp;
	private static Method getAsBufferedImage;
	private static Class _JAI;
	private static Method create1;
	private static Method create3;
	private static Boolean supported=null;
	private static String[] readFormats=new String[]{"tiff","pnm","fpx"};
	private static String[] writeFormats=new String[]{"tiff","pnm"};
	

	public static boolean isSupportedWriteFormat(String format) {
		return "tiff".equalsIgnoreCase(format) || "pnm".equalsIgnoreCase(format);
	}
	
	public static boolean isSupportedReadFormat(String format) {
		return "tiff".equalsIgnoreCase(format) || "pnm".equalsIgnoreCase(format) || "fpx".equalsIgnoreCase(format);
	}

	public static String[] getSupportedReadFormat() {
		return readFormats;
	}

	public static String[] getSupportedWriteFormat() {
		return writeFormats;
	}
	
	public static boolean isJAISupported() {
		if(supported==null) {
			supported=ClassUtil.loadClass("javax.media.jai.JAI",null)!=null?Boolean.TRUE:Boolean.FALSE;
		}
		return supported.booleanValue();
	}



	public static BufferedImage read(Resource res) throws IOException {
		Resource tmp=null;
		try{
			if(!(res instanceof File)) {
				tmp=SystemUtil.getTempDirectory().getRealResource(IDGenerator.intId()+"-"+res.getName());
				IOUtil.copy(res, tmp);
				res=tmp;
			}
			//Object im = JAI.create("fileload", res.getAbsolutePath());
			return getAsBufferedImage(create("fileload", res.getAbsolutePath()));
		}
		finally {
			if(tmp!=null) ResourceUtil.removeEL(tmp, false);
		}
	}
	public static BufferedImage read(InputStream is,String format) throws IOException {
		Resource tmp=null;
		try{
			tmp=SystemUtil.getTempDirectory().getRealResource(IDGenerator.intId()+(StringUtil.isEmpty(format)?"":"."+format));
			IOUtil.copy(is, tmp,false);
			//Object im = JAI.create("fileload", tmp.getAbsolutePath());
			return getAsBufferedImage(create("fileload", tmp.getAbsolutePath()));
		}
		finally {
			if(tmp!=null) ResourceUtil.removeEL(tmp, false);
		}
	}
	
	public static void write(BufferedImage img, Resource res,String format) throws IOException {
		Resource tmp=res;
		try{
			if(!(res instanceof File)) {
				tmp=SystemUtil.getTempDirectory().getRealResource(IDGenerator.intId()+"-"+res.getName());
			}
			//JAI.create("filestore", img, tmp.getAbsolutePath(),format);
			create("filestore", img, tmp.getAbsolutePath(),format);
		}
		finally {
			if(tmp!=res) {
				IOUtil.copy(tmp, res);
				ResourceUtil.removeEL(tmp, false);
			}
		}
	}
	
	public static void write(BufferedImage img, OutputStream os,String format) throws IOException {
		Resource tmp=null;
		try{
			tmp=SystemUtil.getTempDirectory().getRealResource(IDGenerator.intId()+"."+format);
			create("filestore", img, tmp.getAbsolutePath(),format);
			IOUtil.copy(tmp, os,false);
		}
		finally {
			if(tmp!=null) ResourceUtil.removeEL(tmp, false);
		}
	}
	
////////////////////////////////////////////////////////////////////

	private static Object create(String name, Object param) throws IOException {
		try {
			return create1().invoke(null, new Object[]{name,param});
		} catch (Exception e) {
			throw toIOException(e);
		}
	}
	
	private static Object create(String name, Object img, Object param1, Object param2) throws IOException {
		try {
			return create3().invoke(null, new Object[]{name,img,param1,param2});
		}
		catch (Exception e) {
			throw toIOException(e);
		}
	}

	private static BufferedImage getAsBufferedImage(Object im) throws IOException {
		//RenderedOp.getAsBufferedImage();
		try {
			return (BufferedImage) getAsBufferedImage().invoke(im, new Object[0]);
		} 
		catch (Exception e) {
			throw toIOException(e);
		}
	}

	private static Method getAsBufferedImage() throws IOException {
		if(getAsBufferedImage==null) {
			try {
				getAsBufferedImage = getRenderedOp().getMethod("getAsBufferedImage", new Class[0]);
			}
			catch (Exception e) {
				throw toIOException(e);
			}
		}
		return getAsBufferedImage;
	}

	private static Method create1() throws IOException {
		if(create1==null) {
			try {
				create1 = getJAI().getMethod("create", new Class[]{String.class,Object.class});
			} 
			catch (Exception e) {
				throw toIOException(e);
			}
		}
		return create1;
	}

	private static Method create3() throws IOException {
		if(create3==null) {
			try {
				create3 = getJAI().getMethod("create", new Class[]{String.class,RenderedImage.class,Object.class,Object.class});
			} catch (Exception e) {
				throw toIOException(e);
			}
		}
		return create3;
	}
	
	private static Class getRenderedOp() throws IOException {
		if(_RenderedOp==null) {
			_RenderedOp = ClassUtil.loadClass("javax.media.jai.RenderedOp",null);
			if(_RenderedOp==null)
				throw new IOException("JAI is not installed on the system but needed for this extension");
		}
		return _RenderedOp;
	}
	
	private static Class getJAI() throws IOException {
		if(_JAI==null) {
			_JAI = ClassUtil.loadClass("javax.media.jai.JAI",null);
			if(_JAI==null)
				throw new IOException("JAI is not installed on the system but needed for this extension");
		}
		return _JAI;
	}

	

	
	private static IOException toIOException(Throwable e) {
		if(e instanceof InvocationTargetException)
			e=((InvocationTargetException)e).getTargetException();
		
		if(e instanceof IOException) return (IOException) e;
		IOException ioe = new IOException(e.getMessage());
		ioe.setStackTrace(e.getStackTrace());
		return ioe;
	}
}