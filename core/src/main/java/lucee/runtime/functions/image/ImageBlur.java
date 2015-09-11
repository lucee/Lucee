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
import lucee.runtime.op.Caster;

public class ImageBlur {

	@Deprecated
	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc,name,3d);
	}

	@Deprecated
	public static String call(PageContext pc, Object name, double blurFactor) throws PageException {
		return call(pc, Image.toImage(pc,name), blurFactor);
	}
	
	

	public static String call(PageContext pc, Image img) throws PageException {
		return call(pc,img,3d);
	}
	
	public static String call(PageContext pc, Image img, double blurFactor) throws PageException {
		if(blurFactor<3 || blurFactor>10)
			throw new FunctionException(pc,"ImageBlur",2,"blurFactor","invalid value ["+Caster.toString(blurFactor)+"], value have to be between 3 and 10");
		img.blur((int)blurFactor);
		return null;
	}
}