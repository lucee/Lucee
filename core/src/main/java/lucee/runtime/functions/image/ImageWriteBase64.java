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

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;

public class ImageWriteBase64 {
	
	public static String call(PageContext pc, Object name, String destination, String format) throws PageException {
		return call(pc, name, destination, format,false);
	}
	
	public static String call(PageContext pc, Object name, String destination, String format, boolean inHTMLFormat) throws PageException {
		//if(name instanceof String)name=pc.getVariable(Caster.toString(name));
		Image image=Image.toImage(pc,name);
		
		Resource res=StringUtil.isEmpty(destination)?
				image.getSource():
				ResourceUtil.toResourceNotExisting(pc, destination);

		try {
			return image.writeBase64(res, format, inHTMLFormat);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		
	}
}