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
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package lucee.runtime.img.filter;import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which reduces a binary image to a skeleton.
 *
 * Based on an algorithm by Zhang and Suen (CACM, March 1984, 236-239).
 */
public class SkeletonFilter extends BinaryFilter  implements DynFiltering {

	private final static byte[] skeletonTable = {
		0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 
		0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 0, 3, 3,
		0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 
		2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 
		3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0,
		0, 1, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
		3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
		2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 
		3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0
	};
	
	public SkeletonFilter() {
		newColor = 0xffffffff;
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int[] outPixels = new int[width * height];

		int count = 0;
		int black = 0xff000000;
		//int white = 0xffffffff;
		for (int i = 0; i < iterations; i++) {
			count = 0;
			for (int pass = 0; pass < 2; pass++) {
				for (int y = 1; y < height-1; y++) {
					int offset = y*width+1;
					for (int x = 1; x < width-1; x++) {
						int pixel = inPixels[offset];
						if (pixel == black) {
							int tableIndex = 0;

							if (inPixels[offset-width-1] == black)
								tableIndex |= 1;
							if (inPixels[offset-width] == black)
								tableIndex |= 2;
							if (inPixels[offset-width+1] == black)
								tableIndex |= 4;
							if (inPixels[offset+1] == black)
								tableIndex |= 8;
							if (inPixels[offset+width+1] == black)
								tableIndex |= 16;
							if (inPixels[offset+width] == black)
								tableIndex |= 32;
							if (inPixels[offset+width-1] == black)
								tableIndex |= 64;
							if (inPixels[offset-1] == black)
								tableIndex |= 128;
							int code = skeletonTable[tableIndex];
							if (pass == 1) {
								if (code == 2 || code == 3) {
									if (colormap != null)
										pixel = colormap.getColor((float)i/iterations);
									else
										pixel = newColor;
									count++;
								}
							} else {
								if (code == 1 || code == 3) {
									if (colormap != null)
										pixel = colormap.getColor((float)i/iterations);
									else
										pixel = newColor;
									count++;
								}
							}
						}
						outPixels[offset++] = pixel;
					}
				}
				if (pass == 0) {
					inPixels = outPixels;
					outPixels = new int[width * height];
				}
			}
			if (count == 0)
				break;
		}
		return outPixels;
	}

	@Override
	public String toString() {
		return "Binary/Skeletonize...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Iterations")))!=null)setIterations(ImageFilterUtil.toIntValue(o,"Iterations"));
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("NewColor")))!=null)setNewColor(ImageFilterUtil.toColorRGB(o,"NewColor"));
		//if((o=parameters.removeEL(KeyImpl.init("BlackFunction")))!=null)setBlackFunction(ImageFilterUtil.toBinaryFunction(o,"BlackFunction"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Iterations, Colormap, NewColor, BlackFunction]");
		}

		return filter(src, dst);
	}
}