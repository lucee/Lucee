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
 * A Filter to draw grids and check patterns.
 */
public class CheckFilter extends PointFilter  implements DynFiltering {

	private int xScale = 8;
	private int yScale = 8;
	private int foreground = 0xffffffff;
	private int background = 0xff000000;
	private int fuzziness = 0;
	private float angle = 0.0f;
	private float m00 = 1.0f;
	private float m01 = 0.0f;
	private float m10 = 0.0f;
	private float m11 = 1.0f;

	public CheckFilter() {
	}

	/**
     * Set the foreground color.
     * @param foreground the color.
     * @see #getForeground
     */
	public void setForeground(int foreground) {
		this.foreground = foreground;
	}

	/**
     * Get the foreground color.
     * @return the color.
     * @see #setForeground
     */
	public int getForeground() {
		return foreground;
	}

	/**
     * Set the background color.
     * @param background the color.
     * @see #getBackground
     */
	public void setBackground(int background) {
		this.background = background;
	}

	/**
     * Get the background color.
     * @return the color.
     * @see #setBackground
     */
	public int getBackground() {
		return background;
	}

	/**
     * Set the X scale of the texture.
     * @param xScale the scale.
     * @see #getXScale
     */
	public void setXScale(int xScale) {
		this.xScale = xScale;
	}

	/**
     * Get the X scale of the texture.
     * @return the scale.
     * @see #setXScale
     */
	public int getXScale() {
		return xScale;
	}

	/**
     * Set the Y scale of the texture.
     * @param yScale the scale.
     * @see #getYScale
     */
	public void setYScale(int yScale) {
		this.yScale = yScale;
	}

	/**
     * Get the Y scale of the texture.
     * @return the scale.
     * @see #setYScale
     */
	public int getYScale() {
		return yScale;
	}

	/**
     * Set the fuzziness of the texture.
     * @param fuzziness the fuzziness.
     * @see #getFuzziness
     */
	public void setFuzziness(int fuzziness) {
		this.fuzziness = fuzziness;
	}

	/**
     * Get the fuzziness of the texture.
     * @return the fuzziness.
     * @see #setFuzziness
     */
	public int getFuzziness() {
		return fuzziness;
	}

	/**
     * Set the angle of the texture.
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		m00 = cos;
		m01 = sin;
		m10 = -sin;
		m11 = cos;
	}

	/**
     * Get the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		float nx = (m00*x + m01*y) / xScale;
		float ny = (m10*x + m11*y) / yScale;
		float f = ((int)(nx+100000) % 2 != (int)(ny+100000) % 2) ? 1.0f : 0.0f;
		if (fuzziness != 0) {
			float fuzz = (fuzziness/100.0f);
			float fx = ImageMath.smoothPulse(0, fuzz, 1-fuzz, 1, ImageMath.mod(nx, 1));
			float fy = ImageMath.smoothPulse(0, fuzz, 1-fuzz, 1, ImageMath.mod(ny, 1));
			f *= fx*fy;
		}
		return ImageMath.mixColors(f, foreground, background);
	}

	@Override
	public String toString() {
		return "Texture/Checkerboard...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("XScale")))!=null)setXScale(ImageFilterUtil.toIntValue(o,"XScale"));
		if((o=parameters.removeEL(KeyImpl.init("YScale")))!=null)setYScale(ImageFilterUtil.toIntValue(o,"YScale"));
		if((o=parameters.removeEL(KeyImpl.init("Fuzziness")))!=null)setFuzziness(ImageFilterUtil.toIntValue(o,"Fuzziness"));
		if((o=parameters.removeEL(KeyImpl.init("Foreground")))!=null)setForeground(ImageFilterUtil.toColorRGB(o,"Foreground"));
		if((o=parameters.removeEL(KeyImpl.init("Background")))!=null)setBackground(ImageFilterUtil.toColorRGB(o,"Background"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Angle, XScale, YScale, Fuzziness, Foreground, Background, Dimensions]");
		}

		return filter(src, dst);
	}
}