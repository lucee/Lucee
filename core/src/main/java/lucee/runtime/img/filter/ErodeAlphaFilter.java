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

public class ErodeAlphaFilter extends PointFilter  implements DynFiltering {

	private float threshold;
	private float softness = 0;
    protected float radius = 5;
	private float lowerThreshold;
	private float upperThreshold;

	public ErodeAlphaFilter() {
		this( 3, 0.75f, 0 );
	}

	public ErodeAlphaFilter( float radius, float threshold, float softness ) {
		this.radius = radius;
		this.threshold = threshold;
		this.softness = softness;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public float getRadius() {
		return radius;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	
	public float getThreshold() {
		return threshold;
	}
	
	public void setSoftness(float softness) {
		this.softness = softness;
	}

	public float getSoftness() {
		return softness;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        dst = new GaussianFilter( (int)radius ).filter( src, (BufferedImage)null );
        lowerThreshold = 255*(threshold - softness*0.5f);
        upperThreshold = 255*(threshold + softness*0.5f);
		return super.filter(dst, dst);
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		int a = (rgb >> 24) & 0xff;
		//int r = (rgb >> 16) & 0xff;
		//int g = (rgb >> 8) & 0xff;
		//int b = rgb & 0xff;
		if ( a == 255 )
            return 0xffffffff;
        float f = ImageMath.smoothStep(lowerThreshold, upperThreshold, a);
        a = (int)(f * 255);
        if ( a < 0 )
            a = 0;
        else if ( a > 255 )
            a = 255;
        return (a << 24) | 0xffffff;
	}

	@Override
	public String toString() {
		return "Alpha/Erode...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("Softness")))!=null)setSoftness(ImageFilterUtil.toFloatValue(o,"Softness"));
		if((o=parameters.removeEL(KeyImpl.init("Threshold")))!=null)setThreshold(ImageFilterUtil.toFloatValue(o,"Threshold"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Softness, Threshold, Dimensions]");
		}

		return filter(src, dst);
	}
}