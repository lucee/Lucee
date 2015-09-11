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
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which produces a simulated wood texture. This is a bit of a hack, but might be usefult to some people.
 */
public class WoodFilter extends PointFilter  implements DynFiltering {

	private float scale = 200;
	private float stretch = 10.0f;
	private float angle = (float)Math.PI/2;
	private float rings = 0.5f;
	private float turbulence = 0.0f;
	private float fibres = 0.5f;
	private float gain = 0.8f;
	private float m00 = 1.0f;
	private float m01 = 0.0f;
	private float m10 = 0.0f;
	private float m11 = 1.0f;
	private Colormap colormap = new LinearColormap( 0xffe5c494, 0xff987b51 );

	/**
     * Construct a WoodFilter.
     */
    public WoodFilter() {
	}

	/**
     * Specifies the rings value.
     * @param rings the rings value.
     * @min-value 0
     * @max-value 1
     * @see #getRings
     */
    public void setRings(float rings) {
		this.rings = rings;
	}

    /**
     * Returns the rings value.
     * @return the rings value.
     * @see #setRings
     */
 	public float getRings() {
		return rings;
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
     * Specifies the stretch factor of the texture.
     * @param stretch the stretch factor of the texture.
     * @min-value 1
     * @max-value 50+
     * @see #getStretch
     */
	public void setStretch(float stretch) {
		this.stretch = stretch;
	}

	/**
     * Returns the stretch factor of the texture.
     * @return the stretch factor of the texture.
     * @see #setStretch
     */
	public float getStretch() {
		return stretch;
	}

	/**
     * Specifies the angle of the texture.
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
     * Returns the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
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
     * Returns the turbulence of the texture.
     * @return the turbulence of the texture.
     * @see #setTurbulence
     */
	public float getTurbulence() {
		return turbulence;
	}

	/**
     * Specifies the amount of fibres in the texture.
     * @param fibres the amount of fibres in the texture.
     * @min-value 0
     * @max-value 1
     * @see #getFibres
     */
	public void setFibres(float fibres) {
		this.fibres = fibres;
	}

	/**
     * Returns the amount of fibres in  the texture.
     * @return the amount of fibres in the texture.
     * @see #setFibres
     */
	public float getFibres() {
		return fibres;
	}

	/**
     * Specifies the gain of the texture.
     * @param gain the gain of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getGain
     */
	public void setGain(float gain) {
		this.gain = gain;
	}

	/**
     * Returns the gain of the texture.
     * @return the gain of the texture.
     * @see #setGain
     */
	public float getGain() {
		return gain;
	}

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}
	
    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}
	
	@Override
	public int filterRGB(int x, int y, int rgb) {
		float nx = m00*x + m01*y;
		float ny = m10*x + m11*y;
		nx /= scale;
		ny /= scale * stretch;
		float f = Noise.noise2(nx, ny);
        f += 0.1f*turbulence * Noise.noise2(nx*0.05f, ny*20);
		f = (f * 0.5f) + 0.5f;

        f *= rings*50;
        f = f-(int)f;
        f *= 1-ImageMath.smoothStep(gain, 1.0f, f);

        f += fibres*Noise.noise2(nx*scale, ny*50);

		int a = rgb & 0xff000000;
		int v;
		if (colormap != null)
			v = colormap.getColor(f);
		else {
			v = PixelUtils.clamp((int)(f*255));
			int r = v << 16;
			int g = v << 8;
			int b = v;
			v = a|r|g|b;
		}

		return v;
	}

	@Override
	public String toString() {
		return "Texture/Wood...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Turbulence")))!=null)setTurbulence(ImageFilterUtil.toFloatValue(o,"Turbulence"));
		if((o=parameters.removeEL(KeyImpl.init("Stretch")))!=null)setStretch(ImageFilterUtil.toFloatValue(o,"Stretch"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Gain")))!=null)setGain(ImageFilterUtil.toFloatValue(o,"Gain"));
		if((o=parameters.removeEL(KeyImpl.init("Rings")))!=null)setRings(ImageFilterUtil.toFloatValue(o,"Rings"));
		if((o=parameters.removeEL(KeyImpl.init("Fibres")))!=null)setFibres(ImageFilterUtil.toFloatValue(o,"Fibres"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Turbulence, Stretch, Angle, Gain, Rings, Fibres, Scale, Dimensions]");
		}

		return filter(src, dst);
	}
}