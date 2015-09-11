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
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;

public class ImageScaleToFit {
	
	public static String call(PageContext pc, Object name,String fitWidth, String fitHeight) throws PageException {
		return call(pc, name, fitWidth, fitHeight, "highestQuality",1.0);
	}
	
	public static String call(PageContext pc, Object name,String fitWidth, String fitHeight, String interpolation) throws PageException {
		return call(pc, name, fitWidth, fitHeight, interpolation,1.0);
	}
	
	public static String call(PageContext pc, Object name, String fitWidth, String fitHeight, String strInterpolation, double blurFactor) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		img.scaleToFit(fitWidth, fitHeight, strInterpolation, blurFactor);
		return null;
	}
}