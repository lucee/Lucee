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
 * A filter which produces a "oil-painting" effect.
 */
public class OilFilter extends WholeImageFilter  implements DynFiltering {
	
	private int range = 3;
	private int levels = 256;
	
	public OilFilter() {
	}

    /**
     * Set the range of the effect in pixels.
     * @param range the range
     * @see #getRange
     */
	public void setRange( int range ) {
		this.range = range;
	}
	
    /**
     * Get the range of the effect in pixels.
     * @return the range
     * @see #setRange
     */
	public int getRange() {
		return range;
	}
	
    /**
     * Set the number of levels for the effect.
     * @param levels the number of levels
     * @see #getLevels
     */
	public void setLevels( int levels ) {
		this.levels = levels;
	}
	
    /**
     * Get the number of levels for the effect.
     * @return the number of levels
     * @see #setLevels
     */
	public int getLevels() {
		return levels;
	}
	
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int index = 0;
		int[] rHistogram = new int[levels];
		int[] gHistogram = new int[levels];
		int[] bHistogram = new int[levels];
		int[] rTotal = new int[levels];
		int[] gTotal = new int[levels];
		int[] bTotal = new int[levels];
		int[] outPixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int i = 0; i < levels; i++)
				    rHistogram[i] = gHistogram[i] = bHistogram[i] = rTotal[i] = gTotal[i] = bTotal[i] = 0;

				for (int row = -range; row <= range; row++) {
					int iy = y+row;
					int ioffset;
					if (0 <= iy && iy < height) {
						ioffset = iy*width;
						for (int col = -range; col <= range; col++) {
							int ix = x+col;
							if (0 <= ix && ix < width) {
								int rgb = inPixels[ioffset+ix];
								int r = (rgb >> 16) & 0xff;
								int g = (rgb >> 8) & 0xff;
								int b = rgb & 0xff;
								int ri = r*levels/256;
								int gi = g*levels/256;
								int bi = b*levels/256;
								rTotal[ri] += r;
								gTotal[gi] += g;
								bTotal[bi] += b;
								rHistogram[ri]++;
								gHistogram[gi]++;
								bHistogram[bi]++;
							}
						}
					}
				}
				
				int r = 0, g = 0, b = 0;
				for (int i = 1; i < levels; i++) {
					if (rHistogram[i] > rHistogram[r])
						r = i;
					if (gHistogram[i] > gHistogram[g])
						g = i;
					if (bHistogram[i] > bHistogram[b])
						b = i;
				}
				r = rTotal[r] / rHistogram[r];
				g = gTotal[g] / gHistogram[g];
				b = bTotal[b] / bHistogram[b];
				outPixels[index++] = 0xff000000 | ( r << 16 ) | ( g << 8 ) | b;
			}
		}
		return outPixels;
	}

	@Override
	public String toString() {
		return "Stylize/Oil...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Levels")))!=null)setLevels(ImageFilterUtil.toIntValue(o,"Levels"));
		if((o=parameters.removeEL(KeyImpl.init("Range")))!=null)setRange(ImageFilterUtil.toIntValue(o,"Range"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Levels, Range]");
		}

		return filter(src, dst);
	}
}