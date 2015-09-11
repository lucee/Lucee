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
import lucee.runtime.img.Image;

public class ImageDrawRoundRect {

	public static String call(PageContext pc, Object name, double x, double y, double width,double height,
			double arcWidth, double arcHeight) throws PageException {
		return call(pc, name, x, y, width, height,arcWidth,arcHeight,false);
	}
	public static String call(PageContext pc, Object name, double x, double y, double width,double height, 
			double arcWidth, double arcHeight, boolean filled) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw new FunctionException(pc,"ImageDrawRoundRect",3,"width","width must contain a none negative value");
		if (height < 0)
		    throw new FunctionException(pc,"ImageDrawRoundRect",4,"height","width must contain a none negative value");
		if (arcWidth < 0)
		    throw new FunctionException(pc,"ImageDrawRoundRect",5,"arcWidth","arcWidth must contain a none negative value");
		if (arcHeight < 0)
		    throw new FunctionException(pc,"ImageDrawRoundRect",6,"arcHeight","arcHeight must contain a none negative value");
		
		img.drawRoundRect((int)x, (int)y, (int)width, (int)height,(int)arcWidth,(int)arcHeight, filled);
		return null;
	}
	
}