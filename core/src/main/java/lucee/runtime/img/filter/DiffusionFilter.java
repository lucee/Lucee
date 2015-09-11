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
 * A filter which uses Floyd-Steinberg error diffusion dithering to halftone an image.
 */
public class DiffusionFilter extends WholeImageFilter  implements DynFiltering {

	private final static int[] diffusionMatrix = {
	 	 0, 0, 0,
	 	 0, 0, 7,
	 	 3, 5, 1,
	};

	private int[] matrix;
	private int sum = 3+5+7+1;
	private boolean serpentine = true;
	private boolean colorDither = true;
	private int levels = 6;

	/**
	 * Construct a DiffusionFilter.
	 */
	public DiffusionFilter() {
		setMatrix(diffusionMatrix);
	}
	
	/**
	 * Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in the output.
	 * @param serpentine true to use serpentine pattern
     * @see #getSerpentine
	 */
	public void setSerpentine(boolean serpentine) {
		this.serpentine = serpentine;
	}
	
	/**
	 * Return the serpentine setting.
	 * @return the current setting
     * @see #setSerpentine
	 */
	public boolean getSerpentine() {
		return serpentine;
	}
	
	/**
	 * Set whether to use a color dither.
	 * @param colorDither true to use a color dither
     * @see #getColorDither
	 */
	public void setColorDither(boolean colorDither) {
		this.colorDither = colorDither;
	}

	/**
	 * Get whether to use a color dither.
	 * @return true to use a color dither
     * @see #setColorDither
	 */
	public boolean getColorDither() {
		return colorDither;
	}

	/**
	 * Set the dither matrix.
	 * @param matrix the dither matrix
     * @see #getMatrix
	 */
	public void setMatrix(int[] matrix) {
		this.matrix = matrix;
		sum = 0;
		for (int i = 0; i < matrix.length; i++)
			sum += matrix[i];
	}

	/**
	 * Get the dither matrix.
	 * @return the dither matrix
     * @see #setMatrix
	 */
	public int[] getMatrix() {
		return matrix;
	}

	/**
	 * Set the number of dither levels.
	 * @param levels the number of levels
     * @see #getLevels
	 */
	public void setLevels(int levels) {
		this.levels = levels;
	}

	/**
	 * Get the number of dither levels.
	 * @return the number of levels
     * @see #setLevels
	 */
	public int getLevels() {
		return levels;
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int[] outPixels = new int[width * height];

		int index = 0;
		int[] map = new int[levels];
		for (int i = 0; i < levels; i++) {
			int v = 255 * i / (levels-1);
			map[i] = v;
		}
		int[] div = new int[256];
		for (int i = 0; i < 256; i++)
			div[i] = levels*i / 256;

		for (int y = 0; y < height; y++) {
			boolean reverse = serpentine && (y & 1) == 1;
			int direction;
			if (reverse) {
				index = y*width+width-1;
				direction = -1;
			} else {
				index = y*width;
				direction = 1;
			}
			for (int x = 0; x < width; x++) {
				int rgb1 = inPixels[index];

				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				if (!colorDither)
					r1 = g1 = b1 = (r1+g1+b1) / 3;

				int r2 = map[div[r1]];
				int g2 = map[div[g1]];
				int b2 = map[div[b1]];

				outPixels[index] = 0xff000000 | (r2 << 16) | (g2 << 8) | b2;

				int er = r1-r2;
				int eg = g1-g2;
				int eb = b1-b2;

				for (int i = -1; i <= 1; i++) {
					int iy = i+y;
					if (0 <= iy && iy < height) {
						for (int j = -1; j <= 1; j++) {
							int jx = j+x;
							if (0 <= jx && jx < width) {
								int w;
								if (reverse)
									w = matrix[(i+1)*3-j+1];
								else
									w = matrix[(i+1)*3+j+1];
								if (w != 0) {
									int k = reverse ? index - j : index + j;
									rgb1 = inPixels[k];
									r1 = (rgb1 >> 16) & 0xff;
									g1 = (rgb1 >> 8) & 0xff;
									b1 = rgb1 & 0xff;
									r1 += er * w/sum;
									g1 += eg * w/sum;
									b1 += eb * w/sum;
									inPixels[k] = (PixelUtils.clamp(r1) << 16) | (PixelUtils.clamp(g1) << 8) | PixelUtils.clamp(b1);
								}
							}
						}
					}
				}
				index += direction;
			}
		}

		return outPixels;
	}

	@Override
	public String toString() {
		return "Colors/Diffusion Dither...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Levels")))!=null)setLevels(ImageFilterUtil.toIntValue(o,"Levels"));
		if((o=parameters.removeEL(KeyImpl.init("Matrix")))!=null)setMatrix(ImageFilterUtil.toAInt(o,"Matrix"));
		if((o=parameters.removeEL(KeyImpl.init("Serpentine")))!=null)setSerpentine(ImageFilterUtil.toBooleanValue(o,"Serpentine"));
		if((o=parameters.removeEL(KeyImpl.init("ColorDither")))!=null)setColorDither(ImageFilterUtil.toBooleanValue(o,"ColorDither"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Levels, Matrix, Serpentine, ColorDither]");
		}

		return filter(src, dst);
	}
}