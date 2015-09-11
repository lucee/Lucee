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
package lucee.runtime.functions.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

import lucee.commons.color.ColorCaster;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;

public class ImageNew {


	public static Object call(PageContext pc) {
		return new Image();
	}
	
	public static Object call(PageContext pc, Object source) throws PageException {
		if(StringUtil.isEmpty(source))
			return call(pc);//throw new FunctionException(pc,"ImageNew",1,"source","missing argument");
		return Image.createImage(pc, source, true,true,true,null);
	}
	
	public static Object call(PageContext pc,Object source, String width) throws PageException {
		return call(pc, source, width, null, null, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height) throws PageException {
		return call(pc, source, width, height, null, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height, String strImageType) throws PageException {
		return call(pc, source, width, height, strImageType, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height, String strImageType, String strCanvasColor) throws PageException {
		if(source==null)
			return call(pc);
		if(StringUtil.isEmpty(width) && StringUtil.isEmpty(height))
			return call(pc,source);
		
		if(StringUtil.isEmpty(width))
			throw new FunctionException(pc,"ImageNew",2,"width","missing argument");
		if(StringUtil.isEmpty(height))
			throw new FunctionException(pc,"ImageNew",3,"height","missing argument");
			
		if(!StringUtil.isEmpty(source))
			throw new FunctionException(pc,"ImageNew",1,"source","if you define width and height, source has to be empty");
		
		// image type
		int imageType;
		if(StringUtil.isEmpty(strImageType,true)) imageType=BufferedImage.TYPE_INT_RGB;
		else {
			strImageType=strImageType.trim().toLowerCase();
			if("rgb".equals(strImageType)) imageType=BufferedImage.TYPE_INT_RGB;
			else if("argb".equals(strImageType)) imageType=BufferedImage.TYPE_INT_ARGB;
			else if("gray".equals(strImageType)) imageType=BufferedImage.TYPE_BYTE_GRAY;
			else if("grayscale".equals(strImageType)) imageType=BufferedImage.TYPE_BYTE_GRAY;
			else throw new FunctionException(pc,"ImageNew",4,"imageType","imageType has an invalid value ["+strImageType+"]," +
				"valid values are [rgb,argb,grayscale]");
		}
		// canvas color
		Color canvasColor;
		if(StringUtil.isEmpty(strCanvasColor,true)) canvasColor=null;
		else canvasColor=ColorCaster.toColor(strCanvasColor);
		
		return new Image(Caster.toIntValue(width),Caster.toIntValue(height), imageType,canvasColor);
	}
}