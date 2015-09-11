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
 * A filter which draws contours on an image at given brightness levels.
 */
public class ContourFilter extends WholeImageFilter  implements DynFiltering {

	private float levels = 5;
	private float scale = 1;
	private float offset = 0;
	private int contourColor = 0xff000000;
	
	public ContourFilter() {
	}

	public void setLevels( float levels ) {
		this.levels = levels;
	}
	
	public float getLevels() {
		return levels;
	}
	
	/**
     * Specifies the scale of the contours.
     * @param scale the scale of the contours.
     * @min-value 0
     * @max-value 1
     * @see #getScale
     */
	public void setScale( float scale ) {
		this.scale = scale;
	}
	
	/**
     * Returns the scale of the contours.
     * @return the scale of the contours.
     * @see #setScale
     */
	public float getScale() {
		return scale;
	}
	
	public void setOffset( float offset ) {
		this.offset = offset;
	}
	
	public float getOffset() {
		return offset;
	}
	
	public void setContourColor( int contourColor ) {
		this.contourColor = contourColor;
	}
	
	public int getContourColor() {
		return contourColor;
	}
	
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int index = 0;
		short[][] r = new short[3][width];
		int[] outPixels = new int[width * height];

		short[] table = new short[256];
		int offsetl = (int)(offset * 256 / levels);
		for ( int i = 0; i < 256; i++ )
			table[i] = (short)PixelUtils.clamp( (int)(255 * Math.floor(levels*(i+offsetl) / 256) / (levels-1) - offsetl) );

		for (int x = 0; x < width; x++) {
			int rgb = inPixels[x];
			r[1][x] = (short)PixelUtils.brightness( rgb );
		}
		for (int y = 0; y < height; y++) {
			boolean yIn = y > 0 && y < height-1;
			int nextRowIndex = index+width;
			if ( y < height-1) {
				for (int x = 0; x < width; x++) {
					int rgb = inPixels[nextRowIndex++];
					r[2][x] = (short)PixelUtils.brightness( rgb );
				}
			}
			for (int x = 0; x < width; x++) {
				boolean xIn = x > 0 && x < width-1;
				int w = x-1;
				//int e = x+1;
				int v = 0;
				
				if ( yIn && xIn ) {
					short nwb = r[0][w];
					short neb = r[0][x];
					short swb = r[1][w];
					short seb = r[1][x];
					short nw = table[nwb];
					short ne = table[neb];
					short sw = table[swb];
					short se = table[seb];

					if (nw != ne || nw != sw || ne != se || sw != se) {
						v = (int)(scale * (Math.abs(nwb - neb) + Math.abs(nwb - swb) + Math.abs(neb - seb) + Math.abs(swb - seb)));
//						v /= 255;
						if (v > 255)
							v = 255;
					}
				}

				if ( v != 0 )
					outPixels[index] = PixelUtils.combinePixels( inPixels[index], contourColor, PixelUtils.NORMAL, v );
//					outPixels[index] = PixelUtils.combinePixels( (contourColor & 0xff)|(v << 24), inPixels[index], PixelUtils.NORMAL );
				else
					outPixels[index] = inPixels[index];
				index++;
			}
			short[] t;
			t = r[0];
			r[0] = r[1];
			r[1] = r[2];
			r[2] = t;
		}
	
		return outPixels;
	}

	@Override
	public String toString() {
		return "Stylize/Contour...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Levels")))!=null)setLevels(ImageFilterUtil.toFloatValue(o,"Levels"));
		if((o=parameters.removeEL(KeyImpl.init("ContourColor")))!=null)setContourColor(ImageFilterUtil.toColorRGB(o,"ContourColor"));
		if((o=parameters.removeEL(KeyImpl.init("Offset")))!=null)setOffset(ImageFilterUtil.toFloatValue(o,"Offset"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Levels, ContourColor, Offset, Scale]");
		}

		return filter(src, dst);
	}
}