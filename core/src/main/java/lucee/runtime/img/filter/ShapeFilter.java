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

public class ShapeFilter extends WholeImageFilter  implements DynFiltering {

	public final static int LINEAR = 0;
	public final static int CIRCLE_UP = 1;
	public final static int CIRCLE_DOWN = 2;
	public final static int SMOOTH = 3;

	private float factor = 1.0f;
	protected Colormap colormap;
	private boolean useAlpha = true;
	private boolean invert = false;
	private boolean merge = false;
	private int type;

	private final static int one   = 41;
	private final static int sqrt2 = (int)(41*Math.sqrt(2));
	private final static int sqrt5 = (int)(41*Math.sqrt(5));

	public ShapeFilter() {
		colormap = new LinearColormap();
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public float getFactor() {
		return factor;
	}

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}

	public void setUseAlpha(boolean useAlpha) {
		this.useAlpha = useAlpha;
	}

	public boolean getUseAlpha() {
		return useAlpha;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	public boolean getInvert() {
		return invert;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public boolean getMerge() {
		return merge;
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int[] map = new int[width * height];
		makeMap(inPixels, map, width, height);
		int max = distanceMap(map, width, height);
		applyMap(map, inPixels, width, height, max);

		return inPixels;
	}

	public int distanceMap(int[] map, int width, int height) {
		int xmax = width - 3;
		int ymax = height - 3;
		int max = 0;
		int v;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = x + y * width;
				if (map[offset] > 0) {
					if (x < 2 || x > xmax || y < 2 || y > ymax)
						v = setEdgeValue(x, y, map, width, offset, xmax, ymax);
					else
						v = setValue(map, width, offset);
					if (v > max)
						max = v;
				}
			}
		}
		for (int y = height-1; y >= 0; y--) {
			for (int x = width-1; x >= 0; x--) {
				int offset = x + y * width;
				if (map[offset] > 0) {
					if (x < 2 || x > xmax || y < 2 || y > ymax)
						v = setEdgeValue(x, y, map, width, offset, xmax, ymax);
					else
						v = setValue(map, width, offset);
					if (v > max)
						max = v;
				}
			}
		}
		return max;
	}

	private void makeMap(int[] pixels, int[] map, int width, int height) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = x + y * width;
				int b = useAlpha ? (pixels[offset] >> 24) & 0xff : PixelUtils.brightness(pixels[offset]);
//				map[offset] = b * one;
				map[offset] = b * one / 10;
			}
		}
	}

	private void applyMap(int[] map, int[] pixels, int width, int height, int max) {
		if (max == 0)
			max = 1;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = x + y * width;
				int m = map[offset];
				float v = 0;
				int sa = 0, sr = 0, sg = 0, sb = 0;

				if (m == 0) {
					// default color
					sa = sr = sg = sb = 0;
					sa = (pixels[offset] >> 24) & 0xff;
				} else {
					// get V from map
					v = ImageMath.clamp(factor * m / max, 0, 1);
					switch (type) {
					case CIRCLE_UP :
						v = (ImageMath.circleUp(v));
						break;
					case CIRCLE_DOWN :
						v = (ImageMath.circleDown(v));
						break;
					case SMOOTH :
						v = (ImageMath.smoothStep(0, 1, v));
						break;
					}

					if (colormap == null) {
						sr = sg = sb = (int)(v*255);
					} else {
						int c = (colormap.getColor(v));

						sr = (c >> 16) & 0xFF;
						sg = (c >> 8) & 0xFF;
						sb = (c) & 0xFF;
					}
					
					sa = useAlpha ? (pixels[offset] >> 24) & 0xff : PixelUtils.brightness(pixels[offset]);
					
					// invert v if necessary
					if (invert) {
						sr = 255-sr;
						sg = 255-sg;
						sb = 255-sb;
					}
				}

				// write results
				if (merge) {
					// merge with source
					int transp = 255;
					int col = pixels[offset];

					int a = (col & 0xFF000000) >> 24;
					int	r = (col & 0xFF0000) >> 16;
					int	g = (col & 0xFF00) >> 8;
					int	b = (col & 0xFF);

					r = ((sr*r/transp));
					g = ((sg*g/transp));
					b = ((sb*b/transp));
				
					// clip colors
					if (r < 0)
						r = 0;
					if (r > 255)
						r = 255; 
					if (g < 0)
						g = 0;
					if (g > 255)
						g = 255; 
					if (b < 0)
						b = 0;
					if (b > 255)
						b = 255;
					
					pixels[offset] = (a << 24) | (r << 16) | (g << 8) | b;
				} else {
					// write gray shades
					pixels[offset] = (sa << 24) | (sr << 16) | (sg << 8) | sb;
				}
			}
		}
	}

	private int setEdgeValue(int x, int y, int[] map, int width, int offset, int xmax, int ymax) {
		int min, v;
		int r1, r2, r3, r4, r5;

		r1 = offset - width - width - 2;
		r2 = r1 + width;
		r3 = r2 + width;
		r4 = r3 + width;
		r5 = r4 + width;

		if (y == 0 || x == 0 || y == ymax+2 || x == xmax+2)
			return map[offset] = one;

		v = map[r2 + 2] + one;
		min = v;
		
		v = map[r3 + 1] + one;
		if (v < min)
			min = v;
		
		v = map[r3 + 3] + one;
		if (v < min)
			min = v;
		
		v = map[r4 + 2] + one;
		if (v < min)
			min = v;
		
		v = map[r2 + 1] + sqrt2;
		if (v < min)
			min = v;
			
		v = map[r2 + 3] + sqrt2;
		if (v < min)
			min = v;
			
		v = map[r4 + 1] + sqrt2;
		if (v < min)
			min = v;
			
		v = map[r4 + 3] + sqrt2;
		if (v < min)
			min = v;
		
		if (y == 1 || x == 1 || y == ymax+1 || x == xmax+1)
			return map[offset] = min;

		v = map[r1 + 1] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r1 + 3] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r2 + 4] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r4 + 4] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r5 + 3] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r5 + 1] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r4] + sqrt5;
		if (v < min)
			min = v;
			
		v = map[r2] + sqrt5;
		if (v < min)
			min = v;

		return map[offset] = min;
	}

	private int setValue(int[] map, int width, int offset) {
		int min, v;
		int r1, r2, r3, r4, r5;

		r1 = offset - width - width - 2;
		r2 = r1 + width;
		r3 = r2 + width;
		r4 = r3 + width;
		r5 = r4 + width;

		v = map[r2 + 2] + one;
		min = v;
		v = map[r3 + 1] + one;
		if (v < min)
			min = v;
		v = map[r3 + 3] + one;
		if (v < min)
			min = v;
		v = map[r4 + 2] + one;
		if (v < min)
			min = v;
		
		v = map[r2 + 1] + sqrt2;
		if (v < min)
			min = v;
		v = map[r2 + 3] + sqrt2;
		if (v < min)
			min = v;
		v = map[r4 + 1] + sqrt2;
		if (v < min)
			min = v;
		v = map[r4 + 3] + sqrt2;
		if (v < min)
			min = v;
		
		v = map[r1 + 1] + sqrt5;
		if (v < min)
			min = v;
		v = map[r1 + 3] + sqrt5;
		if (v < min)
			min = v;
		v = map[r2 + 4] + sqrt5;
		if (v < min)
			min = v;
		v = map[r4 + 4] + sqrt5;
		if (v < min)
			min = v;
		v = map[r5 + 3] + sqrt5;
		if (v < min)
			min = v;
		v = map[r5 + 1] + sqrt5;
		if (v < min)
			min = v;
		v = map[r4] + sqrt5;
		if (v < min)
			min = v;
		v = map[r2] + sqrt5;
		if (v < min)
			min = v;

		return map[offset] = min;
	}
	
	@Override
	public String toString() {
		return "Stylize/Shapeburst...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("UseAlpha")))!=null)setUseAlpha(ImageFilterUtil.toBooleanValue(o,"UseAlpha"));
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Invert")))!=null)setInvert(ImageFilterUtil.toBooleanValue(o,"Invert"));
		if((o=parameters.removeEL(KeyImpl.init("Factor")))!=null)setFactor(ImageFilterUtil.toFloatValue(o,"Factor"));
		if((o=parameters.removeEL(KeyImpl.init("Merge")))!=null)setMerge(ImageFilterUtil.toBooleanValue(o,"Merge"));
		if((o=parameters.removeEL(KeyImpl.init("Type")))!=null)setType(ImageFilterUtil.toIntValue(o,"Type"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [UseAlpha, Colormap, Invert, Factor, Merge, Type]");
		}

		return filter(src, dst);
	}
}