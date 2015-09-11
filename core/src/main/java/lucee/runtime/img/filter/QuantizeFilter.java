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
 * A filter which quantizes an image to a set number of colors - useful for producing
 * images which are to be encoded using an index color model. The filter can perform
 * Floyd-Steinberg error-diffusion dithering if required. At present, the quantization
 * is done using an octtree algorithm but I eventually hope to add more quantization
 * methods such as median cut. Note: at present, the filter produces an image which
 * uses the RGB color model (because the application it was written for required it).
 * I hope to extend it to produce an IndexColorModel by request.
 */
public class QuantizeFilter extends WholeImageFilter  implements DynFiltering {

	/**
	 * Floyd-Steinberg dithering matrix.
	 */
	protected final static int[] matrix = {
	 	 0, 0, 0,
	 	 0, 0, 7,
	 	 3, 5, 1,
	};
	private int sum = 3+5+7+1;

	private boolean dither;
	private int numColors = 256;
	private boolean serpentine = true;

	/**
	 * Set the number of colors to quantize to.
	 * @param numColors the number of colors. The default is 256.
	 */
	public void setNumColors(int numColors) {
		this.numColors = Math.min(Math.max(numColors, 8), 256);
	}

	/**
	 * Get the number of colors to quantize to.
	 * @return the number of colors.
	 */
	public int getNumColors() {
		return numColors;
	}

	/**
	 * Set whether to use dithering or not. If not, the image is posterized.
	 * @param dither true to use dithering
	 */
	public void setDither(boolean dither) {
		this.dither = dither;
	}

	/**
	 * Return the dithering setting
	 * @return the current setting
	 */
	public boolean getDither() {
		return dither;
	}

	/**
	 * Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in the output.
	 * @param serpentine true to use serpentine pattern
	 */
	public void setSerpentine(boolean serpentine) {
		this.serpentine = serpentine;
	}
	
	/**
	 * Return the serpentine setting
	 * @return the current setting
	 */
	public boolean getSerpentine() {
		return serpentine;
	}
	
	public void quantize(int[] inPixels, int[] outPixels, int width, int height, int numColors, boolean dither, boolean serpentine) {
		int count = width*height;
		Quantizer quantizer = new OctTreeQuantizer();
		quantizer.setup(numColors);
		quantizer.addPixels(inPixels, 0, count);
		int[] table =  quantizer.buildColorTable();

		if (!dither) {
			for (int i = 0; i < count; i++)
				outPixels[i] = table[quantizer.getIndexForColor(inPixels[i])];
		} else {
			int index = 0;
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
					int rgb2 = table[quantizer.getIndexForColor(rgb1)];

					outPixels[index] = rgb2;

					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = rgb1 & 0xff;

					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = rgb2 & 0xff;

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
		}
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int[] outPixels = new int[width*height];
		
		quantize(inPixels, outPixels, width, height, numColors, dither, serpentine);

		return outPixels;
	}

	@Override
	public String toString() {
		return "Colors/Quantize...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Serpentine")))!=null)setSerpentine(ImageFilterUtil.toBooleanValue(o,"Serpentine"));
		if((o=parameters.removeEL(KeyImpl.init("NumColors")))!=null)setNumColors(ImageFilterUtil.toIntValue(o,"NumColors"));
		if((o=parameters.removeEL(KeyImpl.init("Dither")))!=null)setDither(ImageFilterUtil.toBooleanValue(o,"Dither"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Serpentine, NumColors, Dither]");
		}

		return filter(src, dst);
	}
}