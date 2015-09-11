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
package lucee.runtime.img;


import java.io.PrintWriter;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.SystemOut;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Struct;

public class ImageMeta {

	/**
	 */
	/**
	 * adds information about a image to the given struct
	 * @param format
	 * @param res
	 * @param info
	 */
	public static void addInfo(String format, Resource res, Struct info)  {
		try{
			ImageMetaDrew.test();
		}
		catch(Throwable t) { // TODO log to regular logger
			PrintWriter pw = ThreadLocalPageContext.getConfig().getErrWriter();
			SystemOut.printDate(pw, "cannot load addional pic info, library metadata-extractor.jar is missed"); 
		}
		ImageMetaDrew.addInfo(format, res, info);
	}

	

}