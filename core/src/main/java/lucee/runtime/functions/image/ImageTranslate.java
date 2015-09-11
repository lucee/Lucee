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

import java.awt.RenderingHints;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;

public class ImageTranslate {

	public static String call(PageContext pc, Object name, double xTrans, double yTrans) throws PageException {
		return call(pc, name, xTrans, yTrans,"nearest");
	}
	
	public static String call(PageContext pc, Object name, double xTrans, double yTrans, String strInterpolation) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		strInterpolation=strInterpolation.toLowerCase().trim();
		Object interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		
		if("nearest".equals(strInterpolation)) 			interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		else if("bilinear".equals(strInterpolation)) 	interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		else if("bicubic".equals(strInterpolation)) 	interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		
		else throw new FunctionException(pc,"ImageTranslate",4,"interpolation","invalid interpolation definition ["+strInterpolation+"], " +
				"valid interpolation values are [nearest,bilinear,bicubic]");
		
		img.translate((int)xTrans, (int)yTrans,interpolation);
		return null;
	}
}