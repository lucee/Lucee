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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;

public class ImageRotate implements Function {
	
	public static String call(PageContext pc, Object name, String angle) throws PageException {
		return _call(pc, name,-1F,-1F,Caster.toFloatValue(angle),"nearest");
	}
	
	public static String call(PageContext pc, Object name, String angle, String strInterpolation) throws PageException {
		return _call(pc, name,-1F,-1F,Caster.toFloatValue(angle),strInterpolation);
	}
	
	public static String call(PageContext pc, Object name, String x, String y, String angle) throws PageException {
		return _call(pc, name,Caster.toFloatValue(x),Caster.toFloatValue(y),Caster.toFloatValue(angle),"nearest");
	}

	public static String call(PageContext pc, Object name, String x, String y, String angle, String strInterpolation) throws PageException {
		return _call(pc, name,Caster.toFloatValue(x),Caster.toFloatValue(y),Caster.toFloatValue(angle),strInterpolation);
	}

	private static String _call(PageContext pc, Object name, float x, float y, float angle, String strInterpolation) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		strInterpolation=strInterpolation.trim().toLowerCase();
		int interpolation;
		if("nearest".equals(strInterpolation)) interpolation=lucee.runtime.img.Image.INTERPOLATION_NEAREST;
		else if("bilinear".equals(strInterpolation)) interpolation=lucee.runtime.img.Image.INTERPOLATION_BILINEAR;
		else if("bicubic".equals(strInterpolation)) interpolation=lucee.runtime.img.Image.INTERPOLATION_BICUBIC;
		else if("none".equals(strInterpolation)) interpolation=lucee.runtime.img.Image.INTERPOLATION_NONE;
		else throw new ExpressionException("invalid interpolation definition ["+strInterpolation+"]," +
				" valid values are [nearest,bilinear,bicubic]");
		
		img.rotate(x,y,angle,interpolation);
		return null;
		
	}
}