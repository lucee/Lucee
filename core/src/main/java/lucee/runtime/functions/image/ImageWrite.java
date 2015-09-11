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


import java.io.IOException;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;

public class ImageWrite implements Function {

	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc, name, null, 0.75,true);
	}

	public static String call(PageContext pc, Object name, String destination) throws PageException {
		return call(pc, name, destination, 0.75,true);
	}
	
	public static String call(PageContext pc, Object name, String destination, double quality) throws PageException {
		return call(pc, name,destination,quality,true);
	}
	
	public static String call(PageContext pc, Object name, String destination, double quality, boolean overwrite) throws PageException {
		//if(name instanceof String)name=pc.getVariable(Caster.toString(name));
		Image image=Image.toImage(pc,name);
		
		if(quality<0 || quality>1)
			throw new FunctionException(pc,"ImageWrite",3,"quality","value have to be between 0 and 1");
		
		// MUST beide boolschen argumente checken
		if(destination==null) return null;
		try {
			image.writeOut(ResourceUtil.toResourceNotExisting(pc, destination), overwrite , (float)quality);
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return null;
	}
}