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
 * A filter which produces a rubber-stamp type of effect by performing a thresholded blur.
 */
public class StampFilter extends PointFilter  implements DynFiltering {

	private float threshold;
	private float softness = 0;
    private float radius = 5;
	private float lowerThreshold3;
	private float upperThreshold3;
	private int white = 0xffffffff;
	private int black = 0xff000000;

	/**
     * Construct a StampFilter.
     */
	public StampFilter() {
		this(0.5f);
	}

	/**
     * Construct a StampFilter.
     * @param threshold the threshold value
     */
	public StampFilter( float threshold ) {
		setThreshold( threshold );
	}

	/**
	 * Set the radius of the effect.
	 * @param radius the radius
     * @min-value 0
     * @see #getRadius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	/**
	 * Get the radius of the effect.
	 * @return the radius
     * @see #setRadius
	 */
	public float getRadius() {
		return radius;
	}

	/**
     * Set the threshold value.
     * @param threshold the threshold value
     * @see #getThreshold
     */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	
	/**
     * Get the threshold value.
     * @return the threshold value
     * @see #setThreshold
     */
	public float getThreshold() {
		return threshold;
	}
	
	/**
	 * Set the softness of the effect in the range 0..1.
	 * @param softness the softness
     * @min-value 0
     * @max-value 1
     * @see #getSoftness
	 */
	public void setSoftness(float softness) {
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

	/**
     * Set the color to be used for pixels above the upper threshold.
     * @param white the color
     * @see #getWhite
     */
	public void setWhite(Color white) {
		this.white = white.getRGB();
	}

	/**
     * Get the color to be used for pixels above the upper threshold.
     * @return the color
     * @see #setWhite
     */
	public int getWhite() {
		return white;
	}

	/**
     * Set the color to be used for pixels below the lower threshold.
     * @param black the color
     * @see #getBlack
     */
	public void setBlack(Color black) {
		this.black = black.getRGB();
	}

	/**
     * Set the color to be used for pixels below the lower threshold.
     * @return the color
     * @see #setBlack
     */
	public int getBlack() {
		return black;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        dst = new GaussianFilter( (int)radius ).filter( src, (BufferedImage)null );
        lowerThreshold3 = 255*3*(threshold - softness*0.5f);
        upperThreshold3 = 255*3*(threshold + softness*0.5f);
		return super.filter(dst, dst);
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		//int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		int l = r + g + b;
		float f = ImageMath.smoothStep(lowerThreshold3, upperThreshold3, l);
        return ImageMath.mixColors(f, black, white);
	}

	@Override
	public String toString() {
		return "Stylize/Stamp...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("Softness")))!=null)setSoftness(ImageFilterUtil.toFloatValue(o,"Softness"));
		if((o=parameters.removeEL(KeyImpl.init("White")))!=null)setWhite(ImageFilterUtil.toColor(o,"White"));
		if((o=parameters.removeEL(KeyImpl.init("Black")))!=null)setBlack(ImageFilterUtil.toColor(o,"Black"));
		if((o=parameters.removeEL(KeyImpl.init("Threshold")))!=null)setThreshold(ImageFilterUtil.toFloatValue(o,"Threshold"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Softness, White, Black, Threshold, Dimensions]");
		}

		return filter(src, dst);
	}
}