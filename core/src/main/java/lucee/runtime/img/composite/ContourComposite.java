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

package lucee.runtime.img.composite;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * A special Composite used for drawing "marching ants". It draws the ants at the 127 contour of the alpha channel of the source.
 * This can only be used on TYPE_INT_RGBA images.
 */
public final class ContourComposite implements Composite {

	private int offset;

	public ContourComposite( int offset ) {
		this.offset = offset;
	}

	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
		return new ContourCompositeContext( offset, srcColorModel, dstColorModel );
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ContourComposite))
			return false;
		return true;
	}

}

class ContourCompositeContext implements CompositeContext {

	private int offset;

	public ContourCompositeContext( int offset, ColorModel srcColorModel, ColorModel dstColorModel ) {
		this.offset = offset;
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		int x = src.getMinX();
		int y = src.getMinY();
		int w = src.getWidth();
		int h = src.getHeight();

		int[] srcPix = null;
		int[] srcPix2 = null;
		int[] dstInPix = null;
		int[] dstOutPix = new int[w*4];

		for ( int i = 0; i < h; i++ ) {
			srcPix = src.getPixels(x, y, w, 1, srcPix);
			dstInPix = dstIn.getPixels(x, y, w, 1, dstInPix);

			int lastAlpha = 0;
			int k = 0;
			for ( int j = 0; j < w; j++ ) {
				int alpha = srcPix[k+3];
				int alphaAbove = i != 0 ? srcPix2[k+3] : alpha;

				if ( i != 0 && j != 0 && ((alpha ^ lastAlpha) & 0x80) != 0 || ((alpha ^ alphaAbove) & 0x80) != 0 ) {
					if ((offset+i+j)%10 > 4) {
						dstOutPix[k] = 0x00;
						dstOutPix[k+1] = 0x00;
						dstOutPix[k+2] = 0x00;
					} else {
						dstOutPix[k] = 0xff;
						dstOutPix[k+1] = 0xff;
						dstOutPix[k+2] = 0x7f;
					}
					dstOutPix[k+3] = 0xff;
				} else {
					dstOutPix[k] = dstInPix[k];
					dstOutPix[k+1] = dstInPix[k+1];
					dstOutPix[k+2] = dstInPix[k+2];
//					if ( dstOut == dstIn )
						dstOutPix[k] = 0xff;
						dstOutPix[k+1] = 0;
						dstOutPix[k+2] = 0;
						dstOutPix[k+3] = 0;
//					else
//						dstOutPix[k+3] = dstInPix[k+3];
				}

				lastAlpha = alpha;
				k += 4;
			}

			dstOut.setPixels(x, y, w, 1, dstOutPix);

			int[] t = srcPix;
			srcPix = srcPix2;
			srcPix2 = t;
			y++;
		}
	}

}