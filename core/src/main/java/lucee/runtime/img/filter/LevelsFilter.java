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
 * A filter which allows levels adjustment on an image.
 */
public class LevelsFilter extends WholeImageFilter  implements DynFiltering {

	private int[][] lut;
    private float lowLevel = 0;
    private float highLevel = 1;
    private float lowOutputLevel = 0;
    private float highOutputLevel = 1;

	public LevelsFilter() {
	}

    public void setLowLevel( float lowLevel ) {
        this.lowLevel = lowLevel;
    }
    
    public float getLowLevel() {
        return lowLevel;
    }
    
    public void setHighLevel( float highLevel ) {
        this.highLevel = highLevel;
    }
    
    public float getHighLevel() {
        return highLevel;
    }
    
    public void setLowOutputLevel( float lowOutputLevel ) {
        this.lowOutputLevel = lowOutputLevel;
    }
    
    public float getLowOutputLevel() {
        return lowOutputLevel;
    }
    
    public void setHighOutputLevel( float highOutputLevel ) {
        this.highOutputLevel = highOutputLevel;
    }
    
    public float getHighOutputLevel() {
        return highOutputLevel;
    }
    
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		Histogram histogram = new Histogram(inPixels, width, height, 0, width);

		int i, j;

		if (histogram.getNumSamples() > 0) {
			//float scale = 255.0f / histogram.getNumSamples();
			lut = new int[3][256];

            float low = lowLevel * 255;
            float high = highLevel * 255;
            if ( low == high )
                high++;
			for (i = 0; i < 3; i++) {
				for (j = 0; j < 256; j++)
					lut[i][j] = PixelUtils.clamp( (int)(255 * (lowOutputLevel + (highOutputLevel-lowOutputLevel) * (j-low)/(high-low))) );
			}
		} else
			lut = null;

		i = 0;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				inPixels[i] = filterRGB(x, y, inPixels[i]);
				i++;
			}
		lut = null;
		
		return inPixels;
	}

	public int filterRGB(int x, int y, int rgb) {
		if (lut != null) {
			int a = rgb & 0xff000000;
			int r = lut[Histogram.RED][(rgb >> 16) & 0xff];
			int g = lut[Histogram.GREEN][(rgb >> 8) & 0xff];
			int b = lut[Histogram.BLUE][rgb & 0xff];

			return a | (r << 16) | (g << 8) | b;
		}
		return rgb;
	}

	@Override
	public String toString() {
		return "Colors/Levels...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("LowLevel")))!=null)setLowLevel(ImageFilterUtil.toFloatValue(o,"LowLevel"));
		if((o=parameters.removeEL(KeyImpl.init("HighLevel")))!=null)setHighLevel(ImageFilterUtil.toFloatValue(o,"HighLevel"));
		if((o=parameters.removeEL(KeyImpl.init("LowOutputLevel")))!=null)setLowOutputLevel(ImageFilterUtil.toFloatValue(o,"LowOutputLevel"));
		if((o=parameters.removeEL(KeyImpl.init("HighOutputLevel")))!=null)setHighOutputLevel(ImageFilterUtil.toFloatValue(o,"HighOutputLevel"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [LowLevel, HighLevel, LowOutputLevel, HighOutputLevel]");
		}

		return filter(src, dst);
	}
}