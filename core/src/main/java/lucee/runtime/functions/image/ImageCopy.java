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

public class ImageCopy {

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw new FunctionException(pc,"ImageCopy",3,"width","width must contain a none negative value");
		if (height < 0)
		    throw new FunctionException(pc,"ImageCopy",4,"height","width must contain a none negative value");
		
		return img.copy((float)x, (float)y, (float)width, (float)height);
	}

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height,double dx) throws PageException {
		throw new FunctionException(pc,"ImageCopy",7,"dy","when you define dx, you have also to define dy");
	}

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height, double dx, double dy) throws PageException {
		if(dx==-999 && dy==-999){// -999 == default value for named argument
			return call(pc, name, x, y, width, height);
		}
		if(dx==-999){// -999 == default value for named argument
			throw new FunctionException(pc,"ImageCopy",6,"dx","when you define dy, you have also to define dx");
		}
		if(dy==-999){// -999 == default value for named argument
			throw new FunctionException(pc,"ImageCopy",7,"dy","when you define dx, you have also to define dy");
		}
		
		
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw new FunctionException(pc,"ImageCopy",3,"width","width must contain a none negative value");
		if (height < 0)
		    throw new FunctionException(pc,"ImageCopy",4,"height","width must contain a none negative value");
		
		return img.copy((float)x, (float)y, (float)width, (float)height, (float)dx, (float)dy);
		//return null;
	}	
}