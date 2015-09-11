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

package lucee.runtime.img.filter;import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which produces a water ripple distortion.
 */
public class WaterFilter extends TransformFilter  implements DynFiltering {
	
	private float wavelength = 16;
	private float amplitude = 10;
	private float phase = 0;
	private float centreX = 0.5f;
	private float centreY = 0.5f;
	private float radius = 50;

	private float radius2 = 0;
	private float icentreX;
	private float icentreY;

	public WaterFilter() {
		super(ConvolveFilter.CLAMP_EDGES );
	}

	/**
	 * Set the wavelength of the ripples.
	 * @param wavelength the wavelength
     * @see #getWavelength
	 */
	public void setWavelength(float wavelength) {
		this.wavelength = wavelength;
	}

	/**
	 * Get the wavelength of the ripples.
	 * @return the wavelength
     * @see #setWavelength
	 */
	public float getWavelength() {
		return wavelength;
	}

	/**
	 * Set the amplitude of the ripples.
	 * @param amplitude the amplitude
     * @see #getAmplitude
	 */
	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	/**
	 * Get the amplitude of the ripples.
	 * @return the amplitude
     * @see #setAmplitude
	 */
	public float getAmplitude() {
		return amplitude;
	}

	/**
	 * Set the phase of the ripples.
	 * @param phase the phase
     * @see #getPhase
	 */
	public void setPhase(float phase) {
		this.phase = phase;
	}

	/**
	 * Get the phase of the ripples.
	 * @return the phase
     * @see #setPhase
	 */
	public float getPhase() {
		return phase;
	}

	/**
	 * Set the centre of the effect in the X direction as a proportion of the image size.
	 * @param centreX the center
     * @see #getCentreX
	 */
	public void setCentreX( float centreX ) {
		this.centreX = centreX;
	}

	/**
	 * Get the centre of the effect in the X direction as a proportion of the image size.
	 * @return the center
     * @see #setCentreX
	 */
	public float getCentreX() {
		return centreX;
	}
	
	/**
	 * Set the centre of the effect in the Y direction as a proportion of the image size.
	 * @param centreY the center
     * @see #getCentreY
	 */
	public void setCentreY( float centreY ) {
		this.centreY = centreY;
	}

	/**
	 * Get the centre of the effect in the Y direction as a proportion of the image size.
	 * @return the center
     * @see #setCentreY
	 */
	public float getCentreY() {
		return centreY;
	}
	
	/**
	 * Set the centre of the effect as a proportion of the image size.
	 * @param centre the center
     * @see #getCentre
	 */
	public void setCentre( Point2D centre ) {
		this.centreX = (float)centre.getX();
		this.centreY = (float)centre.getY();
	}

	/**
	 * Get the centre of the effect as a proportion of the image size.
	 * @return the center
     * @see #setCentre
	 */
	public Point2D getCentre() {
		return new Point2D.Float( centreX, centreY );
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

	private boolean inside(int v, int a, int b) {
		return a <= v && v <= b;
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		icentreX = src.getWidth() * centreX;
		icentreY = src.getHeight() * centreY;
		if ( radius == 0 )
			radius = Math.min(icentreX, icentreY);
		radius2 = radius*radius;
		return super.filter( src, dst );
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float dx = x-icentreX;
		float dy = y-icentreY;
		float distance2 = dx*dx + dy*dy;
		if (distance2 > radius2) {
			out[0] = x;
			out[1] = y;
		} else {
			float distance = (float)Math.sqrt(distance2);
			float amount = amplitude * (float)Math.sin(distance / wavelength * ImageMath.TWO_PI - phase);
			amount *= (radius-distance)/radius;
			if ( distance != 0 )
				amount *= wavelength/distance;
			out[0] = x + dx*amount;
			out[1] = y + dy*amount;
		}
	}

	@Override
	public String toString() {
		return "Distort/Water Ripples...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=null;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("CentreX")))!=null)setCentreX(ImageFilterUtil.toFloatValue(o,"CentreX"));
		if((o=parameters.removeEL(KeyImpl.init("CentreY")))!=null)setCentreY(ImageFilterUtil.toFloatValue(o,"CentreY"));
		//if((o=parameters.removeEL(KeyImpl.init("Centre")))!=null)setCentre(ImageFilterUtil.toPoint2D(o,"Centre"));
		if((o=parameters.removeEL(KeyImpl.init("Wavelength")))!=null)setWavelength(ImageFilterUtil.toFloatValue(o,"Wavelength"));
		if((o=parameters.removeEL(KeyImpl.init("Amplitude")))!=null)setAmplitude(ImageFilterUtil.toFloatValue(o,"Amplitude"));
		if((o=parameters.removeEL(KeyImpl.init("Phase")))!=null)setPhase(ImageFilterUtil.toFloatValue(o,"Phase"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, CentreX, CentreY, Centre, Wavelength, Amplitude, Phase, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}