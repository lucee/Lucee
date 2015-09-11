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

import lucee.commons.color.ColorCaster;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.filter.GrayscaleColormap;
import lucee.runtime.img.filter.LinearColormap;
import lucee.runtime.img.filter.SpectrumColormap;

public class ImageFilterColorMap {
	public static Object call(PageContext pc, String type) throws PageException {
		return call(pc, type, null, null);
	}
	public static Object call(PageContext pc, String type, String lineColor1) throws PageException {
		return call(pc, type, lineColor1, null);
		
	}
	public static Object call(PageContext pc, String type, String lineColor1,String lineColor2) throws PageException {
		type=type.toLowerCase().trim();
		
		if("grayscale".equals(type)) return new GrayscaleColormap();
		else if("spectrum".equals(type)) return new SpectrumColormap();
		else if("linear".equals(type)) {
			boolean isEmpty1=StringUtil.isEmpty(lineColor1);
			boolean isEmpty2=StringUtil.isEmpty(lineColor2);
			
			if(isEmpty1 && isEmpty2) return new LinearColormap();
			else if(!isEmpty1 && !isEmpty2) {
				Color color1 = ColorCaster.toColor(lineColor1);
				Color color2 = ColorCaster.toColor(lineColor2);
				return new LinearColormap(color1.getRGB(),color2.getRGB());
			}
			else 
				throw new FunctionException(pc, "ImageFilterColorMap", 2, "lineColor1", "when you define linecolor1 you have to define linecolor2 as well");
				
		}
		else throw new FunctionException(pc, "ImageFilterColorMap", 1, "type", "invalid type defintion, valid types are [grayscale,spectrum,linear]");
		
		
		
		
	}
}