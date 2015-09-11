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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which distorts an image by rippling it in the X or Y directions.
 * The amplitude and wavelength of rippling can be specified as well as whether 
 * pixels going off the edges are wrapped or not.
 */
public class RippleFilter extends TransformFilter  implements DynFiltering {
	
    /**
     * Sine wave ripples.
     */
	public final static int SINE = 0;

    /**
     * Sawtooth wave ripples.
     */
	public final static int SAWTOOTH = 1;

    /**
     * Triangle wave ripples.
     */
	public final static int TRIANGLE = 2;

    /**
     * Noise ripples.
     */
	public final static int NOISE = 3;

	private float xAmplitude, yAmplitude;
	private float xWavelength, yWavelength;
	private int waveType;

	/**
	 * Construct a RippleFilter.
	 */
	public RippleFilter() {
		xAmplitude = 5.0f;
		yAmplitude = 0.0f;
		xWavelength = yWavelength = 16.0f;
	}

	/**
	 * Set the amplitude of ripple in the X direction.
	 * @param xAmplitude the amplitude (in pixels).
     * @see #getXAmplitude
	 */
	public void setXAmplitude(float xAmplitude) {
		this.xAmplitude = xAmplitude;
	}

	/**
	 * Get the amplitude of ripple in the X direction.
	 * @return the amplitude (in pixels).
     * @see #setXAmplitude
	 */
	public float getXAmplitude() {
		return xAmplitude;
	}

	/**
	 * Set the wavelength of ripple in the X direction.
	 * @param xWavelength the wavelength (in pixels).
     * @see #getXWavelength
	 */
	public void setXWavelength(float xWavelength) {
		this.xWavelength = xWavelength;
	}

	/**
	 * Get the wavelength of ripple in the X direction.
	 * @return the wavelength (in pixels).
     * @see #setXWavelength
	 */
	public float getXWavelength() {
		return xWavelength;
	}

	/**
	 * Set the amplitude of ripple in the Y direction.
	 * @param yAmplitude the amplitude (in pixels).
     * @see #getYAmplitude
	 */
	public void setYAmplitude(float yAmplitude) {
		this.yAmplitude = yAmplitude;
	}

	/**
	 * Get the amplitude of ripple in the Y direction.
	 * @return the amplitude (in pixels).
     * @see #setYAmplitude
	 */
	public float getYAmplitude() {
		return yAmplitude;
	}

	/**
	 * Set the wavelength of ripple in the Y direction.
	 * @param yWavelength the wavelength (in pixels).
     * @see #getYWavelength
	 */
	public void setYWavelength(float yWavelength) {
		this.yWavelength = yWavelength;
	}

	/**
	 * Get the wavelength of ripple in the Y direction.
	 * @return the wavelength (in pixels).
     * @see #setYWavelength
	 */
	public float getYWavelength() {
		return yWavelength;
	}


	/**
	 * Set the wave type.
	 * valid values are:
	 * - sine (default):  Sine wave ripples.
	 * - sawtooth: Sawtooth wave ripples.
	 * - triangle: Triangle wave ripples.
	 * - noise: Noise ripples.
     * @param waveType the type.
	 * @throws ExpressionException 
     * @see #getWaveType
	 */
	public void setWaveType(String waveType) throws ExpressionException {

		String str=waveType.trim().toUpperCase();
		if("SINE".equals(str)) this.waveType = SINE;
		else if("SAWTOOTH".equals(str)) this.waveType = SAWTOOTH;
		else if("TRIANGLE".equals(str)) this.waveType = TRIANGLE;
		else if("NOISE".equals(str)) this.waveType = NOISE;
		else 
			throw new ExpressionException("invalid value ["+waveType+"] for waveType, valid values are [sine,sawtooth,triangle,noise]");
	
	}

	/**
	 * Get the wave type.
	 * @return the type.
     * @see #setWaveType
	 */
	public int getWaveType() {
		return waveType;
	}

	@Override
	protected void transformSpace(Rectangle r) {
		if (edgeAction == ConvolveFilter.ZERO_EDGES) {
			r.x -= (int)xAmplitude;
			r.width += (int)(2*xAmplitude);
			r.y -= (int)yAmplitude;
			r.height += (int)(2*yAmplitude);
		}
	}

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float nx = y / xWavelength;
		float ny = x / yWavelength;
		float fx, fy;
		switch (waveType) {
		case SINE:
		default:
			fx = (float)Math.sin(nx);
			fy = (float)Math.sin(ny);
			break;
		case SAWTOOTH:
			fx = ImageMath.mod(nx, 1);
			fy = ImageMath.mod(ny, 1);
			break;
		case TRIANGLE:
			fx = ImageMath.triangle(nx);
			fy = ImageMath.triangle(ny);
			break;
		case NOISE:
			fx = Noise.noise1(nx);
			fy = Noise.noise1(ny);
			break;
		}
		out[0] = x + xAmplitude * fx;
		out[1] = y + yAmplitude * fy;
	}

	@Override
	public String toString() {
		return "Distort/Ripple...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {
		//BufferedImage dst=ImageUtil.createBufferedImage(src,src.getWidth()+400,src.getHeight()+400);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("XAmplitude")))!=null)setXAmplitude(ImageFilterUtil.toFloatValue(o,"XAmplitude"));
		if((o=parameters.removeEL(KeyImpl.init("XWavelength")))!=null)setXWavelength(ImageFilterUtil.toFloatValue(o,"XWavelength"));
		if((o=parameters.removeEL(KeyImpl.init("YAmplitude")))!=null)setYAmplitude(ImageFilterUtil.toFloatValue(o,"YAmplitude"));
		if((o=parameters.removeEL(KeyImpl.init("YWavelength")))!=null)setYWavelength(ImageFilterUtil.toFloatValue(o,"YWavelength"));
		if((o=parameters.removeEL(KeyImpl.init("WaveType")))!=null)setWaveType(ImageFilterUtil.toString(o,"WaveType"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [XAmplitude, XWavelength, YAmplitude, YWavelength, WaveType, EdgeAction, Interpolation]");
		}

		return filter(src, (BufferedImage)null);
	}
}