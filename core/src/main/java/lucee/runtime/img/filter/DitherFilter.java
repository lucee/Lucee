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

package lucee.runtime.img.filter;import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;



/**
 * A filter which performs ordered dithering on an image.
 */
public class DitherFilter extends PointFilter  implements DynFiltering {
	
	/**
     * 2x2 magic square.
     */
	protected final static int[] ditherMagic2x2Matrix = {
	 	 0, 2,
	 	 3, 1
	};

	/**
     * 4x4 magic square.
     */
	protected final static int[] ditherMagic4x4Matrix = {
	 	 0, 14,  3, 13,
		11,  5,  8,  6,
		12,  2, 15,  1,
		 7,  9,  4, 10
	};

	/**
     * 4x4 ordered dither.
     */
	public final static int[] ditherOrdered4x4Matrix = {
	 	 0,  8,  2, 10,
		12,  4, 14,  6,
		 3, 11,  1,  9,
		15,  7, 13,  5
	};

	/**
     * 4x4 lines.
     */
	public final static int[] ditherLines4x4Matrix = {
	 	 0,  1,  2,  3,
		 4,  5,  6,  7,
		 8,  9, 10, 11,
		12, 13, 14, 15
	};

	/**
     * 6x6 90 degree halftone.
     */
	public final static int[] dither90Halftone6x6Matrix = {
	 	29, 18, 12, 19, 30, 34,
		17,  7,  4,  8, 20, 28,
		11,  3,  0,  1,  9, 27,
		16,  6,  2,  5, 13, 26,
		25, 15, 10, 14, 21, 31,
		33, 25, 24, 23, 33, 36
	};

	/*
	 * The following dithering matrices are taken from "Digital Halftoning" 
	 * by Robert Ulichney, MIT Press, ISBN 0-262-21009-6.
	 */

	/**
     * Order-6 ordered dither.
     */
	public final static int[] ditherOrdered6x6Matrix = {
		 1, 59, 15, 55,  2, 56, 12, 52,
		33, 17, 47, 31, 34, 18, 44, 28,
		 9, 49,  5, 63, 10, 50,  6, 60,
		41, 25, 37, 21, 42, 26, 38, 22,
		 3, 57, 13, 53,  0, 58, 14, 54,
		35, 19, 45, 29, 32, 16, 46, 30,
		11, 51,  7, 61,  8, 48,  4, 62,
		43, 27, 39, 23, 40, 24, 36, 20 
	};

	/**
     * Order-8 ordered dither.
     */
	public final static int[] ditherOrdered8x8Matrix = {
		  1,235, 59,219, 15,231, 55,215,  2,232, 56,216, 12,228, 52,212,
		129, 65,187,123,143, 79,183,119,130, 66,184,120,140, 76,180,116,
		 33,193, 17,251, 47,207, 31,247, 34,194, 18,248, 44,204, 28,244,
		161, 97,145, 81,175,111,159, 95,162, 98,146, 82,172,108,156, 92,
		  9,225, 49,209,  5,239, 63,223, 10,226, 50,210,  6,236, 60,220,
		137, 73,177,113,133, 69,191,127,138, 74,178,114,134, 70,188,124,
		 41,201, 25,241, 37,197, 21,255, 42,202, 26,242, 38,198, 22,252,
		169,105,153, 89,165,101,149, 85,170,106,154, 90,166,102,150, 86,
		  3,233, 57,217, 13,229, 53,213,  0,234, 58,218, 14,230, 54,214,
		131, 67,185,121,141, 77,181,117,128, 64,186,122,142, 78,182,118,
		 35,195, 19,249, 45,205, 29,245, 32,192, 16,250, 46,206, 30,246,
		163, 99,147, 83,173,109,157, 93,160, 96,144, 80,174,110,158, 94,
		 11,227, 51,211,  7,237, 61,221,  8,224, 48,208,  4,238, 62,222,
		139, 75,179,115,135, 71,189,125,136, 72,176,112,132, 68,190,126,
		 43,203, 27,243, 39,199, 23,253, 40,200, 24,240, 36,196, 20,254,
		171,107,155, 91,167,103,151, 87,168,104,152, 88,164,100,148, 84 };

	/**
     * Order-3 clustered dither.
     */
	public final static int[] ditherCluster3Matrix = {
		 9,11,10, 8, 6, 7,
		12,17,16, 5, 0, 1,
		13,14,15, 4, 3, 2,
		 8, 6, 7, 9,11,10,
		 5, 0, 1,12,17,16,
		 4, 3, 2,13,14,15 };

	/**
     * Order-4 clustered dither.
     */
	public final static int[] ditherCluster4Matrix = {
		18,20,19,16,13,11,12,15,
		27,28,29,22, 4, 3, 2, 9,
		26,31,30,21, 5, 0, 1,10,
		23,25,24,17, 8, 6, 7,14,
		13,11,12,15,18,20,19,16,
		 4, 3, 2, 9,27,28,29,22,
		 5, 0, 1,10,26,31,30,21,
		 8, 6, 7,14,23,25,24,17 };

	/**
     * Order-8 clustered dither.
     */
	public final static int[] ditherCluster8Matrix = {
		 64, 69, 77, 87, 86, 76, 68, 67, 63, 58, 50, 40, 41, 51, 59, 60,
		 70, 94,100,109,108, 99, 93, 75, 57, 33, 27, 18, 19, 28, 34, 52,
		 78,101,114,116,115,112, 98, 83, 49, 26, 13, 11, 12, 15, 29, 44,
		 88,110,123,124,125,118,107, 85, 39, 17,  4,  3,  2,  9, 20, 42,
		 89,111,122,127,126,117,106, 84, 38, 16,  5,  0,  1, 10, 21, 43,
		 79,102,119,121,120,113, 97, 82, 48, 25,  8,  6,  7, 14, 30, 45,
		 71, 95,103,104,105, 96, 92, 74, 56, 32, 24, 23, 22, 31, 35, 53,
		 65, 72, 80, 90, 91, 81, 73, 66, 62, 55, 47, 37, 36, 46, 54, 61,
		 63, 58, 50, 40, 41, 51, 59, 60, 64, 69, 77, 87, 86, 76, 68, 67,
		 57, 33, 27, 18, 19, 28, 34, 52, 70, 94,100,109,108, 99, 93, 75,
		 49, 26, 13, 11, 12, 15, 29, 44, 78,101,114,116,115,112, 98, 83,
		 39, 17,  4,  3,  2,  9, 20, 42, 88,110,123,124,125,118,107, 85,
		 38, 16,  5,  0,  1, 10, 21, 43, 89,111,122,127,126,117,106, 84,
		 48, 25,  8,  6,  7, 14, 30, 45, 79,102,119,121,120,113, 97, 82,
		 56, 32, 24, 23, 22, 31, 35, 53, 71, 95,103,104,105, 96, 92, 74,
		 62, 55, 47, 37, 36, 46, 54, 61, 65, 72, 80, 90, 91, 81, 73, 66 };

	private int[] matrix;
	private int rows, cols, levels;
	private int[] mod;
	private int[] div;
	private int[] map;
	private boolean colorDither;
	private boolean initialized = false;

	/**
     * Constuct a DitherFilter.
     */
    public DitherFilter() {
		rows = 2;
		cols = 2;
		matrix = ditherMagic4x4Matrix;
		levels = 6;
		colorDither = true;
	}
	
	/**
	 * Set the dither matrix.
	 * @param matrix the dither matrix
     * @see #getMatrix
	 */
	public void setMatrix(int[] matrix) {
		this.matrix = matrix;
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

	/**
	 * Set whether to use a color dither.
	 * @param colorDither whether to use a color dither
     * @see #getColorDither
	 */
	public void setColorDither(boolean colorDither) {
		this.colorDither = colorDither;
	}

	/**
	 * Get whether to use a color dither.
	 * @return whether to use a color dither
     * @see #getColorDither
	 */
	public boolean getColorDither() {
		return colorDither;
	}

	/**
     * Initialize the filter.
     */
    protected void initialize() {
		rows = cols = (int)Math.sqrt(matrix.length);
		map = new int[levels];
		for (int i = 0; i < levels; i++) {
			int v = 255 * i / (levels-1);
			map[i] = v;
		}
		div = new int[256];
		mod = new int[256];
		int rc = (rows*cols+1);
		for (int i = 0; i < 256; i++) {
			div[i] = (levels-1)*i / 256;
			mod[i] = i*rc/256;
		}
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		if (!initialized) {
			initialized = true;
			initialize();
		}
		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		int col = x % cols;
		int row = y % rows;
		int v = matrix[row*cols+col];
		if (colorDither) {
			r = map[mod[r] > v ? div[r] + 1 : div[r]];
			g = map[mod[g] > v ? div[g] + 1 : div[g]];
			b = map[mod[b] > v ? div[b] + 1 : div[b]];
		} else {
			int value = (r+g+b)/3;
			r = g = b = map[mod[value] > v ? div[value] + 1 : div[value]];
		}
		return a | (r << 16) | (g << 8) | b;
	}

	@Override
	public String toString() {
		return "Colors/Dither...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Levels")))!=null)setLevels(ImageFilterUtil.toIntValue(o,"Levels"));
		if((o=parameters.removeEL(KeyImpl.init("Matrix")))!=null)setMatrix(ImageFilterUtil.toAInt(o,"Matrix"));
		if((o=parameters.removeEL(KeyImpl.init("ColorDither")))!=null)setColorDither(ImageFilterUtil.toBooleanValue(o,"ColorDither"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Levels, Matrix, ColorDither, Dimensions]");
		}

		return filter(src, dst);
	}
}