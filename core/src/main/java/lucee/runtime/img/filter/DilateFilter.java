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
 * Given a binary image, this filter performs binary dilation, setting all added pixels to the given 'new' color.
 */
public class DilateFilter extends BinaryFilter  implements DynFiltering {

	private int threshold = 2;

	public DilateFilter() {
	}

	/**
	 * Set the threshold - the number of neighbouring pixels for dilation to occur.
	 * @param threshold the new threshold
     * @see #getThreshold
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	/**
	 * Return the threshold - the number of neighbouring pixels for dilation to occur.
	 * @return the current threshold
     * @see #setThreshold
	 */
	public int getThreshold() {
		return threshold;
	}
	
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int[] outPixels = new int[width * height];

		for (int i = 0; i < iterations; i++) {
			int index = 0;

			if (i > 0) {
				int[] t = inPixels;
				inPixels = outPixels;
				outPixels = t;
			}
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = inPixels[y*width+x];
					if (!blackFunction.isBlack(pixel)) {
						int neighbours = 0;

						for (int dy = -1; dy <= 1; dy++) {
							int iy = y+dy;
							int ioffset;
							if (0 <= iy && iy < height) {
								ioffset = iy*width;
								for (int dx = -1; dx <= 1; dx++) {
									int ix = x+dx;
									if (!(dy == 0 && dx == 0) && 0 <= ix && ix < width) {
										int rgb = inPixels[ioffset+ix];
										if (blackFunction.isBlack(rgb))
											neighbours++;
									}
								}
							}
						}
						
						if (neighbours >= threshold) {
							if (colormap != null)
								pixel = colormap.getColor((float)i/iterations);
							else
								pixel = newColor;
						}
					}
					outPixels[index++] = pixel;
				}
			}
		}
		return outPixels;
	}

	@Override
	public String toString() {
		return "Binary/Dilate...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Threshold")))!=null)setThreshold(ImageFilterUtil.toIntValue(o,"Threshold"));
		if((o=parameters.removeEL(KeyImpl.init("Iterations")))!=null)setIterations(ImageFilterUtil.toIntValue(o,"Iterations"));
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("NewColor")))!=null)setNewColor(ImageFilterUtil.toColorRGB(o,"NewColor"));
		
		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Threshold, Iterations, Colormap, NewColor, BlackFunction]");
		}

		return filter(src, dst);
	}
}