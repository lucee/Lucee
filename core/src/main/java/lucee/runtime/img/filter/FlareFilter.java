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
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * An experimental filter for rendering lens flares.
 */
public class FlareFilter extends PointFilter  implements DynFiltering {

	private int rays = 50;
	private float radius;
	private float baseAmount = 1.0f;
	private float ringAmount = 0.2f;
	private float rayAmount = 0.1f;
	private int color = 0xffffffff;
	private int width, height;
	private float centreX = 0.5f, centreY = 0.5f;
	private float ringWidth = 1.6f;
	
	private float linear = 0.03f;
	private float gauss = 0.006f;
	private float mix = 0.50f;
	private float falloff = 6.0f;
	private float sigma;

	private float icentreX, icentreY;

	public FlareFilter() {
		setRadius(50.0f);
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setRingWidth(float ringWidth) {
		this.ringWidth = ringWidth;
	}

	public float getRingWidth() {
		return ringWidth;
	}
	
	public void setBaseAmount(float baseAmount) {
		this.baseAmount = baseAmount;
	}

	public float getBaseAmount() {
		return baseAmount;
	}

	public void setRingAmount(float ringAmount) {
		this.ringAmount = ringAmount;
	}

	public float getRingAmount() {
		return ringAmount;
	}

	public void setRayAmount(float rayAmount) {
		this.rayAmount = rayAmount;
	}

	public float getRayAmount() {
		return rayAmount;
	}

	public void setCentreY( float centreY ) {
		this.centreY = centreY;
	}

	public void setCentreX( float centreX ) {
		this.centreX = centreX;
	}

	/*public void setCentre( Point2D centre ) {
		this.centreX = (float)centre.getX();
		this.centreY = (float)centre.getY();
	}*/

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
		sigma = radius/3;
	}

	/**
	 * Get the radius of the effect.
	 * @return the radius
     * @see #setRadius
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
		icentreX = centreX*width;
		icentreY = centreY*height;
		super.setDimensions(width, height);
	}
	
	@Override
	public int filterRGB(int x, int y, int rgb) {
		float dx = x-icentreX;
		float dy = y-icentreY;
		float distance = (float)Math.sqrt(dx*dx+dy*dy);
		float a = (float)Math.exp(-distance*distance*gauss)*mix + (float)Math.exp(-distance*linear)*(1-mix);
		float ring;

		a *= baseAmount;

		if (distance > radius + ringWidth)
			a = ImageMath.lerp((distance - (radius + ringWidth))/falloff, a, 0);

		if (distance < radius - ringWidth || distance > radius + ringWidth)
			ring = 0;
		else {
	        ring = Math.abs(distance-radius)/ringWidth;
	        ring = 1 - ring*ring*(3 - 2*ring);
	        ring *= ringAmount;
		}

		a += ring;

		float angle = (float)Math.atan2(dx, dy)+ImageMath.PI;
		angle = (ImageMath.mod(angle/ImageMath.PI*17 + 1.0f + Noise.noise1(angle*10), 1.0f) - 0.5f)*2;
		angle = Math.abs(angle);
		angle = (float)Math.pow(angle, 5.0);

		float b = rayAmount * angle / (1 + distance*0.1f);
		a += b;

		a = ImageMath.clamp(a, 0, 1);
		return ImageMath.mixColors(a, rgb, color);
	}

	@Override
	public String toString() {
		return "Stylize/Flare...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		//if((o=parameters.removeEL(KeyImpl.init("Centre")))!=null)setCentre(ImageFilterUtil.toPoint2D(o,"Centre"));
		if((o=parameters.removeEL(KeyImpl.init("RingWidth")))!=null)setRingWidth(ImageFilterUtil.toFloatValue(o,"RingWidth"));
		if((o=parameters.removeEL(KeyImpl.init("BaseAmount")))!=null)setBaseAmount(ImageFilterUtil.toFloatValue(o,"BaseAmount"));
		if((o=parameters.removeEL(KeyImpl.init("RingAmount")))!=null)setRingAmount(ImageFilterUtil.toFloatValue(o,"RingAmount"));
		if((o=parameters.removeEL(KeyImpl.init("RayAmount")))!=null)setRayAmount(ImageFilterUtil.toFloatValue(o,"RayAmount"));
		if((o=parameters.removeEL(KeyImpl.init("Color")))!=null)setColor(ImageFilterUtil.toColorRGB(o,"Color"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Centre, RingWidth, BaseAmount, RingAmount, RayAmount, Color, Dimensions]");
		}

		return filter(src, dst);
	}
}