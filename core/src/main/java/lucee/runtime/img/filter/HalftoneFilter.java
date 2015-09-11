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
 * A filter which can be used to produce wipes by transferring the luma of a mask image into the alpha channel of the source.
 */
public class HalftoneFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private float density = 0;
	private float softness = 0;
	private boolean invert;
	private BufferedImage mask;

	public HalftoneFilter() {
	}

	/**
	 * Set the density of the image in the range 0..1.
	 * *arg density The density
	 */
	public void setDensity( float density ) {
		this.density = density;
	}
	
	public float getDensity() {
		return density;
	}
	
	/**
	 * Set the softness of the effect in the range 0..1.
	 * @param softness the softness
     * @min-value 0
     * @max-value 1
     * @see #getSoftness
	 */
	public void setSoftness( float softness ) {
		this.softness = softness;
	}
	
	/**
	 * Get the softness of the effect.
	 * @return the softness
     * @see #setSoftness
	 */
	public float getSoftness() {
		return softness;
	}
	
	public void setMask( BufferedImage mask ) {
		this.mask = mask;
	}
	
	public BufferedImage getMask() {
		return mask;
	}
	
	public void setInvert( boolean invert ) {
		this.invert = invert;
	}
	
	public boolean getInvert() {
		return invert;
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		if ( mask == null )
			return dst;

        int maskWidth = mask.getWidth();
        int maskHeight = mask.getHeight();

		//float d = density * (1+softness);
		//float lower = 255 * (d-softness);
		//float upper = 255 * d;
        float s = 255*softness;

		int[] inPixels = new int[width];
		int[] maskPixels = new int[maskWidth];

        for ( int y = 0; y < height; y++ ) {
			getRGB( src, 0, y, width, 1, inPixels );
			getRGB( mask, 0, y % maskHeight, maskWidth, 1, maskPixels );

			for ( int x = 0; x < width; x++ ) {
				int maskRGB = maskPixels[x % maskWidth];
				int inRGB = inPixels[x];
				int v = PixelUtils.brightness( maskRGB );
				int iv = PixelUtils.brightness( inRGB );
				float f = ImageMath.smoothStep( iv-s, iv+s, v );
				int a = (int)(255 * f);

				if ( invert )
					a = 255-a;
//				inPixels[x] = (a << 24) | (inRGB & 0x00ffffff);
				inPixels[x] = (inRGB & 0xff000000) | (a << 16) | (a << 8) | a;
			}

			setRGB( dst, 0, y, width, 1, inPixels );
        }

        return dst;
    }

	@Override
	public String toString() {
		return "Stylize/Halftone...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Density")))!=null)setDensity(ImageFilterUtil.toFloatValue(o,"Density"));
		if((o=parameters.removeEL(KeyImpl.init("Softness")))!=null)setSoftness(ImageFilterUtil.toFloatValue(o,"Softness"));
		if((o=parameters.removeEL(KeyImpl.init("Invert")))!=null)setInvert(ImageFilterUtil.toBooleanValue(o,"Invert"));
		if((o=parameters.removeEL(KeyImpl.init("Mask")))!=null)setMask(ImageFilterUtil.toBufferedImage(o,"Mask"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Density, Softness, Invert, Mask]");
		}

		return filter(src, dst);
	}
}