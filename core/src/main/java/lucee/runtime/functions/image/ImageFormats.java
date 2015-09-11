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

import java.util.HashSet;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ImageFormats {

	private static Collection.Key DECODER=KeyImpl.getInstance("decoder");
	private static Collection.Key ENCODER=KeyImpl.getInstance("encoder");
	
	public static Struct call(PageContext pc) throws PageException {
		Struct sct=new StructImpl();
		sct.set(DECODER, toArray(ImageUtil.getReaderFormatNames()));
		sct.set(ENCODER, toArray(ImageUtil.getWriterFormatNames()));
		
		return sct;
	}

	private static Object toArray(String[] arr) {
		HashSet set=new HashSet();
		for(int i=0;i<arr.length;i++) {
			set.add(arr[i].toUpperCase());
		}
		
		return set.toArray(new String[set.size()]);
	}
}