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


import javax.media.jai.BorderExtender;

import lucee.commons.color.ColorCaster;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.img.Image;

public class ImageAddBorder implements Function {
	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc,name,1D,"black","constant");
	}
	
	public static String call(PageContext pc, Object name, double thickness) throws PageException {
		return call(pc,name,thickness,"black","constant");
	}
	
	public static String call(PageContext pc, Object name, double thickness, String color) throws PageException {
		return call(pc,name,thickness,color,"constant");
	}

	public static String call(PageContext pc, Object name, double thickness, String color, String strBorderType) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		strBorderType=strBorderType.trim().toLowerCase();
		int borderType=Image.BORDER_TYPE_CONSTANT;
		if("zero".equals(strBorderType))			borderType=BorderExtender.BORDER_ZERO;
		else if("constant".equals(strBorderType))	borderType=Image.BORDER_TYPE_CONSTANT;
		else if("copy".equals(strBorderType))		borderType=BorderExtender.BORDER_COPY;
		else if("reflect".equals(strBorderType))	borderType=BorderExtender.BORDER_REFLECT;
		else if("wrap".equals(strBorderType))		borderType=BorderExtender.BORDER_WRAP;
    	
		Image image=Image.toImage(pc,name);
		image.addBorder((int)thickness,ColorCaster.toColor(color),borderType);
		
		
		return null;
	}
}