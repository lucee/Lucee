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
import java.util.Random;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
/**
 * A filter which simulates underwater caustics. This can be animated to get a bottom-of-the-swimming-pool effect.
 */
public class CausticsFilter extends WholeImageFilter  implements DynFiltering {

	private float scale = 32;
	//private float angle = 0.0f;
	private int brightness = 10;
	private float amount = 1.0f;
	private float turbulence = 1.0f;
	private float dispersion = 0.0f;
	private float time = 0.0f;
	private int samples = 2;
	private int bgColor = 0xff799fff;

	private float s, c;

	public CausticsFilter() {
	}

	/**
     * Specifies the scale of the texture.
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 300+
     * @see #getScale
     */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
     * Returns the scale of the texture.
     * @return the scale of the texture.
     * @see #setScale
     */
	public float getScale() {
		return scale;
	}

	/**
     * Set the brightness.
     * @param brightness the brightness.
     * @min-value 0
     * @max-value 1
     * @see #getBrightness
     */
	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	/**
     * Get the brightness.
     * @return the brightness.
     * @see #setBrightness
     */
	public int getBrightness() {
		return brightness;
	}

	/**
     * Specifies the turbulence of the texture.
     * @param turbulence the turbulence of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getTurbulence
     */
	public void setTurbulence(float turbulence) {
		this.turbulence = turbulence;
	}

	/**
     * Returns the turbulence of the effect.
     * @return the turbulence of the effect.
     * @see #setTurbulence
     */
	public float getTurbulence() {
		return turbulence;
	}

	/**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	/**
	 * Get the amount of effect.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}
	
	/**
	 * Set the dispersion.
	 * @param dispersion the dispersion
     * @min-value 0
     * @max-value 1
     * @see #getDispersion
	 */
	public void setDispersion(float dispersion) {
		this.dispersion = dispersion;
	}
	
	/**
	 * Get the dispersion.
	 * @return the dispersion
     * @see #setDispersion
	 */
	public float getDispersion() {
		return dispersion;
	}
	
	/**
	 * Set the time. Use this to animate the effect.
	 * @param time the time
     * @see #getTime
	 */
	public void setTime(float time) {
		this.time = time;
	}
	
	/**
	 * Set the time.
	 * @return the time
     * @see #setTime
	 */
	public float getTime() {
		return time;
	}
	
	/**
	 * Set the number of samples per pixel. More samples means better quality, but slower rendering.
	 * @param samples the number of samples
     * @see #getSamples
	 */
	public void setSamples(int samples) {
		this.samples = samples;
	}
	
	/**
	 * Get the number of samples per pixel.
	 * @return the number of samples
     * @see #setSamples
	 */
	public int getSamples() {
		return samples;
	}
	
	/**
	 * Set the background color.
	 * @param c the color
     * @see #getBgColor
	 */
	public void setBgColor(int c) {
		bgColor = c;
	}

	/**
	 * Get the background color.
	 * @return the color
     * @see #setBgColor
	 */
	public int getBgColor() {
		return bgColor;
	}

	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		Random random = new Random(0);

		s = (float)Math.sin(0.1);
		c = (float)Math.cos(0.1);

		//int srcWidth = originalSpace.width;
		//int srcHeight = originalSpace.height;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int index = 0;
		int[] pixels = new int[outWidth * outHeight];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				pixels[index++] = bgColor;
			}
		}
		
		int v = brightness/samples;
		if (v == 0)
			v = 1;

		float rs = 1.0f/scale;
		float d = 0.95f;
		index = 0;
		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				for (int s = 0; s < samples; s++) {
					float sx = x+random.nextFloat();
					float sy = y+random.nextFloat();
					float nx = sx*rs;
					float ny = sy*rs;
					float xDisplacement, yDisplacement;
					float focus = 0.1f+amount;
					xDisplacement = evaluate(nx-d, ny) - evaluate(nx+d, ny);
					yDisplacement = evaluate(nx, ny+d) - evaluate(nx, ny-d);

					if (dispersion > 0) {
						for (int c = 0; c < 3; c++) {
							float ca = (1+c*dispersion);
							float srcX = sx + scale*focus * xDisplacement*ca;
							float srcY = sy + scale*focus * yDisplacement*ca;

							if (srcX < 0 || srcX >= outWidth-1 || srcY < 0 || srcY >= outHeight-1) {
							} else {
								int i = ((int)srcY)*outWidth+(int)srcX;
								int rgb = pixels[i];
								int r = (rgb >> 16) & 0xff;
								int g = (rgb >> 8) & 0xff;
								int b = rgb & 0xff;
								if (c == 2)
									r += v;
								else if (c == 1)
									g += v;
								else
									b += v;
								if (r > 255)
									r = 255;
								if (g > 255)
									g = 255;
								if (b > 255)
									b = 255;
								pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
							}
						}
					} else {
						float srcX = sx + scale*focus * xDisplacement;
						float srcY = sy + scale*focus * yDisplacement;

						if (srcX < 0 || srcX >= outWidth-1 || srcY < 0 || srcY >= outHeight-1) {
						} else {
							int i = ((int)srcY)*outWidth+(int)srcX;
							int rgb = pixels[i];
							int r = (rgb >> 16) & 0xff;
							int g = (rgb >> 8) & 0xff;
							int b = rgb & 0xff;
							r += v;
							g += v;
							b += v;
							if (r > 255)
								r = 255;
							if (g > 255)
								g = 255;
							if (b > 255)
								b = 255;
							pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
						}
					}
				}
			}
		}
		return pixels;
	}

	/*private static int add(int rgb, float brightness) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r += brightness;
		g += brightness;
		b += brightness;
		if (r > 255)
			r = 255;
		if (g > 255)
			g = 255;
		if (b > 255)
			b = 255;
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}*/
	
	/*private static int add(int rgb, float brightness, int c) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		if (c == 2)
			r += brightness;
		else if (c == 1)
			g += brightness;
		else
			b += brightness;
		if (r > 255)
			r = 255;
		if (g > 255)
			g = 255;
		if (b > 255)
			b = 255;
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}*/
	
	private static float turbulence2(float x, float y, float time, float octaves) {
		float value = 0.0f;
		float remainder;
		float lacunarity = 2.0f;
		float f = 1.0f;
		int i;
		
		// to prevent "cascading" effects
		x += 371;
		y += 529;
		
		for (i = 0; i < (int)octaves; i++) {
			value += Noise.noise3(x, y, time) / f;
			x *= lacunarity;
			y *= lacunarity;
			f *= 2;
		}

		remainder = octaves - (int)octaves;
		if (remainder != 0)
			value += remainder * Noise.noise3(x, y, time) / f;

		return value;
	}

	private float evaluate(float x, float y) {
		float xt = s*x + c*time;
		float tt = c*x - c*time;
		float f = turbulence == 0.0 ? Noise.noise3(xt, y, tt) : turbulence2(xt, y, tt, turbulence);
		return f;
	}
	
	@Override
	public String toString() {
		return "Texture/Caustics...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Brightness")))!=null)setBrightness(ImageFilterUtil.toIntValue(o,"Brightness"));
		if((o=parameters.removeEL(KeyImpl.init("Turbulence")))!=null)setTurbulence(ImageFilterUtil.toFloatValue(o,"Turbulence"));
		if((o=parameters.removeEL(KeyImpl.init("Dispersion")))!=null)setDispersion(ImageFilterUtil.toFloatValue(o,"Dispersion"));
		if((o=parameters.removeEL(KeyImpl.init("BgColor")))!=null)setBgColor(ImageFilterUtil.toColorRGB(o,"BgColor"));
		if((o=parameters.removeEL(KeyImpl.init("Time")))!=null)setTime(ImageFilterUtil.toFloatValue(o,"Time"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));
		if((o=parameters.removeEL(KeyImpl.init("Samples")))!=null)setSamples(ImageFilterUtil.toIntValue(o,"Samples"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Brightness, Turbulence, Dispersion, BgColor, Time, Scale, Samples]");
		}

		return filter(src, dst);
	}
}