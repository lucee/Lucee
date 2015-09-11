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

package lucee.runtime.img.filter;import java.awt.Color;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
/**
 * A filter which can be used to produce wipes by transferring the luma of a Destination image into the alpha channel of the source.
 */
public class ChromaKeyFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private float hTolerance = 0;
	private float sTolerance = 0;
	private float bTolerance = 0;
	private int color;

	public ChromaKeyFilter() {
	}

	/**
	 * Set the tolerance of the image in the range 0..1.
	 * *arg tolerance The tolerance
	 */
	public void setHTolerance( float hTolerance ) {
		this.hTolerance = hTolerance;
	}
	
	public float getHTolerance() {
		return hTolerance;
	}
	
	public void setSTolerance( float sTolerance ) {
		this.sTolerance = sTolerance;
	}
	
	public float getSTolerance() {
		return sTolerance;
	}
	
	public void setBTolerance( float bTolerance ) {
		this.bTolerance = bTolerance;
	}
	
	public float getBTolerance() {
		return bTolerance;
	}
	
	public void setColor( int color ) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
		
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();
		//int type = src.getType();
		//WritableRaster srcRaster = 
        src.getRaster();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		//WritableRaster dstRaster = 
        dst.getRaster();

		float[] hsb1 = null;
		float[] hsb2 = null;
		int rgb2 = color;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;
		hsb2 = Color.RGBtoHSB( r2, b2, g2, hsb2 );
		int[] inPixels = null;
		for ( int y = 0; y < height; y++ ) {
			inPixels = getRGB( src, 0, y, width, 1, inPixels );
			for ( int x = 0; x < width; x++ ) {
				int rgb1 = inPixels[x];

				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				hsb1 = Color.RGBtoHSB( r1, b1, g1, hsb1 );
//                    int tolerance = (int)(255*tolerance);
//                    return Math.abs(r1-r2) <= tolerance && Math.abs(g1-g2) <= tolerance && Math.abs(b1-b2) <= tolerance;

//                   if ( PixelUtils.nearColors( in, clean, (int)(255*tolerance) ) )
				if ( Math.abs( hsb1[0] - hsb2[0] ) < hTolerance && Math.abs( hsb1[1] - hsb2[1] ) < sTolerance && Math.abs( hsb1[2] - hsb2[2] ) < bTolerance )
					inPixels[x] = rgb1 & 0xffffff;
				else
					inPixels[x] = rgb1;
			}
			setRGB( dst, 0, y, width, 1, inPixels );
		}

        return dst;
    }

	@Override
	public String toString() {
		return "Keying/Chroma Key...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("HTolerance")))!=null)setHTolerance(ImageFilterUtil.toFloatValue(o,"HTolerance"));
		if((o=parameters.removeEL(KeyImpl.init("STolerance")))!=null)setSTolerance(ImageFilterUtil.toFloatValue(o,"STolerance"));
		if((o=parameters.removeEL(KeyImpl.init("BTolerance")))!=null)setBTolerance(ImageFilterUtil.toFloatValue(o,"BTolerance"));
		if((o=parameters.removeEL(KeyImpl.init("Color")))!=null)setColor(ImageFilterUtil.toIntValue(o,"Color"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [HTolerance, STolerance, BTolerance, Color]");
		}

		return filter(src, dst);
	}
}