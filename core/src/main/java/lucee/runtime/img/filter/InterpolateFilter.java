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
 * A filter which interpolates between two images. You can set the interpolation factor outside the range 0 to 1
 * to extrapolate images.
 */
public class InterpolateFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private BufferedImage destination;
	private float interpolation;

	public InterpolateFilter() {
	}

    /**
     * Set the destination image.
     * @param destination the destination image
     * @see #getDestination
     */
	public void setDestination( BufferedImage destination ) {
		this.destination = destination;
	}
	
    /**
     * Get the destination image.
     * @return the destination image
     * @see #setDestination
     */
	public BufferedImage getDestination() {
		return destination;
	}
	
    /**
     * Set the interpolation factor.
     * @param interpolation the interpolation factor
     * @see #getInterpolation
     */
	public void setInterpolation( float interpolation ) {
		this.interpolation = interpolation;
	}
	
    /**
     * Get the interpolation factor.
     * @return the interpolation factor
     * @see #setInterpolation
     */
	public float getInterpolation() {
		return interpolation;
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();
		src.getType();
		src.getRaster();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		dst.getRaster();

        if ( destination != null ) {
			width = Math.min( width, destination.getWidth() );
			height = Math.min( height, destination.getWidth() );
			int[] pixels1 = null;
			int[] pixels2 = null;

			for (int y = 0; y < height; y++) {
				pixels1 = getRGB( src, 0, y, width, 1, pixels1 );
				pixels2 = getRGB( destination, 0, y, width, 1, pixels2 );
				for (int x = 0; x < width; x++) {
					int rgb1 = pixels1[x];
					int rgb2 = pixels2[x];
					int a1 = (rgb1 >> 24) & 0xff;
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = rgb1 & 0xff;
					//int a2 = (rgb2 >> 24) & 0xff;
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = rgb2 & 0xff;
					r1 = PixelUtils.clamp( ImageMath.lerp( interpolation, r1, r2 ) );
					g1 = PixelUtils.clamp( ImageMath.lerp( interpolation, g1, g2 ) );
					b1 = PixelUtils.clamp( ImageMath.lerp( interpolation, b1, b2 ) );
					pixels1[x] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				}
				setRGB( dst, 0, y, width, 1, pixels1 );
			}
        }

        return dst;
    }

	@Override
	public String toString() {
		return "Effects/Interpolate...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toFloatValue(o,"Interpolation"));
		if((o=parameters.removeEL(KeyImpl.init("destination")))!=null)setDestination(ImageFilterUtil.toBufferedImage(o,"destination"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Interpolation]");
		}

		return filter(src, dst);
	}
}