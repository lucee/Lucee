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

import javax.media.jai.operator.TransposeDescriptor;
import javax.media.jai.operator.TransposeType;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;

public class ImageFlip {
	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc,name,"vertical");
	}
	public static String call(PageContext pc, Object name, String strTranspose) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		strTranspose=strTranspose.toLowerCase().trim();
		TransposeType transpose = TransposeDescriptor.FLIP_VERTICAL;
		if("vertical".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_VERTICAL;
		else if("horizontal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_HORIZONTAL;
		else if("diagonal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_DIAGONAL;
		else if("antidiagonal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_ANTIDIAGONAL;
		else if("anti diagonal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_ANTIDIAGONAL;
		else if("anti-diagonal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_ANTIDIAGONAL;
		else if("anti_diagonal".equals(strTranspose)) transpose=TransposeDescriptor.FLIP_ANTIDIAGONAL;
		else if("90".equals(strTranspose)) transpose=TransposeDescriptor.ROTATE_90;
		else if("180".equals(strTranspose)) transpose=TransposeDescriptor.ROTATE_180;
		else if("270".equals(strTranspose)) transpose=TransposeDescriptor.ROTATE_270;
		else throw new FunctionException(pc,"ImageFlip",2,"transpose","invalid transpose definition ["+strTranspose+"], " +
				"valid transpose values are [vertical,horizontal,diagonal,90,180,270]");
		
		img.flip(transpose);
		return null;
	}
	
}