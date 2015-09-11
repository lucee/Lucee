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

public class ImagePaste {

	public static String call(PageContext pc, Object src1, Object src2) throws PageException {
		return call(pc, src1, src2, 0,0);
	}
	
	public static String call(PageContext pc, Object src1, Object src2, double x) throws PageException {
		return call(pc, src1, src2, x,0);
	}
	
	public static String call(PageContext pc, Object src1, Object src2, double x, double y) throws PageException {
		//if(src1 instanceof String) src1=pc.getVariable(Caster.toString(src1));
		//if(src2 instanceof String) src2=pc.getVariable(Caster.toString(src2));
		
		Image.toImage(pc,src1).paste(Image.createImage(pc, src2,true,false,true,null),(int)x,(int)y);
		return null;
	}
}