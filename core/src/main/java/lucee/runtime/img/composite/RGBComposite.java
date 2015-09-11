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
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public abstract class RGBComposite implements Composite {

	protected float extraAlpha;

	public RGBComposite() {
		this( 1.0f );
	}

	public RGBComposite( float alpha ) {
		if ( alpha < 0.0f || alpha > 1.0f )
			throw new IllegalArgumentException("RGBComposite: alpha must be between 0 and 1");
		this.extraAlpha = alpha;
	}

	public float getAlpha() {
		return extraAlpha;
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(extraAlpha);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RGBComposite))
			return false;
		RGBComposite c = (RGBComposite)o;

		if ( extraAlpha != c.extraAlpha )
			return false;
		return true;
	}

    public abstract static class RGBCompositeContext implements CompositeContext {

        private float alpha;
        private ColorModel srcColorModel;
        private ColorModel dstColorModel;

        public RGBCompositeContext( float alpha, ColorModel srcColorModel, ColorModel dstColorModel ) {
            this.alpha = alpha;
            this.srcColorModel = srcColorModel;
            this.dstColorModel = dstColorModel;
        }

        @Override
		public void dispose() {
        }
        
        // Multiply two numbers in the range 0..255 such that 255*255=255
        static int multiply255( int a, int b ) {
            int t = a * b + 0x80;
            return ((t >> 8) + t) >> 8;
        }
        
        static int clamp( int a ) {
            return a < 0 ? 0 : a > 255 ? 255 : a;
        }
	
        public abstract void composeRGB( int[] src, int[] dst, float alpha );

        @Override
		public void compose( Raster src, Raster dstIn, WritableRaster dstOut ) {
            float alpha = this.alpha;

            int[] srcPix = null;
            int[] dstPix = null;

            int x = dstOut.getMinX();
            int w = dstOut.getWidth();
            int y0 = dstOut.getMinY();
            int y1 = y0 + dstOut.getHeight();

            for ( int y = y0; y < y1; y++ ) {
                srcPix = src.getPixels( x, y, w, 1, srcPix );
                dstPix = dstIn.getPixels( x, y, w, 1, dstPix );
                composeRGB( srcPix, dstPix, alpha );
                dstOut.setPixels( x, y, w, 1, dstPix );
            }
        }

    }
}