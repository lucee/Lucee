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

import java.util.Arrays;
import java.util.HashSet;

import lucee.runtime.PageContext;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.util.ListUtil;

public class GetReadableImageFormats {

	public static String call(PageContext pc) {
		return format(ImageUtil.getReaderFormatNames());
		
	}

	public static String format(String[] formats) {
		HashSet set=new HashSet();
		for(int i=0;i<formats.length;i++) {
			set.add(formats[i].toUpperCase());
		}
		formats=(String[]) set.toArray(new String[set.size()]);
		Arrays.sort(formats);
		return ListUtil.arrayToList(formats, ",").toUpperCase();
	}
}