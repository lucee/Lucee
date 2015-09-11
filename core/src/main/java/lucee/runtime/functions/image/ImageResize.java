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


import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.img.Image;

public class ImageResize implements Function {

	public static String call(PageContext pc, Object name,String width, String height) throws PageException {
		return call(pc, name, width, height, "highestQuality", 1.0);
	}

	public static String call(PageContext pc, Object name,String width, String height,String interpolation) throws PageException {
		return call(pc, name, width, height, interpolation, 1.0);
	}
	
	
	public static String call(PageContext pc, Object name,String width, String height,String interpolation, double blurFactor) throws PageException {
		// image
		//if(name instanceof String)name=pc.getVariable(Caster.toString(name));
		Image image=Image.toImage(pc,name);
		
		interpolation = interpolation.toLowerCase().trim();
		
		
		if (blurFactor <= 0.0 || blurFactor > 10.0) 
			throw new FunctionException(pc,"ImageResize",5,"blurFactor","argument blurFactor must be between 0 and 10");
			
		
		// MUST interpolation/blur
		//if(!"highestquality".equals(interpolation) || blurFactor!=1.0)throw new ExpressionException("argument interpolation and blurFactor are not supported for function ImageResize");
		
		image.resize(width,height,interpolation,blurFactor);
		return null;
	}
	
	/*private static int toDimension(String label, String dimension) throws PageException {
		if(StringUtil.isEmpty(dimension)) return -1;
		dimension=dimension.trim();
		// int value
		int i=Caster.toIntValue(dimension,-1);
		if(i>-1) return i;
		throw new ExpressionException("attribute ["+label+"] value has an invalid value ["+dimension+"]"); 
	}*/
}