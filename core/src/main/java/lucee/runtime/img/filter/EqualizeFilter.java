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
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter to perform auto-equalization on an image.
 */
public class EqualizeFilter extends WholeImageFilter  implements DynFiltering {

    private int[][] lut;

	public EqualizeFilter() {
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		Histogram histogram = new Histogram(inPixels, width, height, 0, width);

		int i, j;

		if (histogram.getNumSamples() > 0) {
			float scale = 255.0f / histogram.getNumSamples();
			lut = new int[3][256];
			for (i = 0; i < 3; i++) {
				lut[i][0] = histogram.getFrequency(i, 0);
				for (j = 1; j < 256; j++)
					lut[i][j] = lut[i][j-1] + histogram.getFrequency(i, j);
				for (j = 0; j < 256; j++)
					lut[i][j] = Math.round(lut[i][j]*scale);
			}
		} else
			lut = null;

		i = 0;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				inPixels[i] = filterRGB(x, y, inPixels[i]);
				i++;
			}
		lut = null;
		
		return inPixels;
	}

	private int filterRGB(int x, int y, int rgb) {
		if (lut != null) {
			int a = rgb & 0xff000000;
			int r = lut[Histogram.RED][(rgb >> 16) & 0xff];
			int g = lut[Histogram.GREEN][(rgb >> 8) & 0xff];
			int b = lut[Histogram.BLUE][rgb & 0xff];

			return a | (r << 16) | (g << 8) | b;
		}
		return rgb;
	}

	@Override
	public String toString() {
		return "Colors/Equalize";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		//Object o;

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported []");
		}

		return filter(src, dst);
	}
}