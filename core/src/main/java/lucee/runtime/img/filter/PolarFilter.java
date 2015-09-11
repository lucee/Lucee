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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which distorts and image by performing coordinate conversions between rectangular and polar coordinates.
 */
public class PolarFilter extends TransformFilter  implements DynFiltering {
	
	/**
     * Convert from rectangular to polar coordinates.
     */
    public final static int RECT_TO_POLAR = 0;

	/**
     * Convert from polar to rectangular coordinates.
     */
	public final static int POLAR_TO_RECT = 1;

	/**
     * Invert the image in a circle.
     */
	public final static int INVERT_IN_CIRCLE = 2;

	private int type;
	private float width, height;
	private float centreX, centreY;
	private float radius;

	/**
     * Construct a PolarFilter.
     */
    public PolarFilter() {
		this(RECT_TO_POLAR);
	}

	/**
     * Construct a PolarFilter.
     * @param type the distortion type
     */
	public PolarFilter(int type) {
		super(ConvolveFilter.CLAMP_EDGES);
		this.type=type;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		this.width = src.getWidth();
		this.height = src.getHeight();
		centreX = width/2;
		centreY = height/2;
		radius = Math.max(centreY, centreX);
		return super.filter( src, dst );
	}
	
	/**
     * Set the distortion type, valid values are
     * - RECT_TO_POLAR = Convert from rectangular to polar coordinates
     * - POLAR_TO_RECT = Convert from polar to rectangular coordinates
     * - INVERT_IN_CIRCLE = Invert the image in a circle
     */
	public void setType(String type) throws ExpressionException {
		type=type.trim().toUpperCase();
		if("RECT_TO_POLAR".equals(type)) this.type = RECT_TO_POLAR;
		else if("POLAR_TO_RECT".equals(type)) this.type = POLAR_TO_RECT;
		else if("INVERT_IN_CIRCLE".equals(type)) this.type = INVERT_IN_CIRCLE;
		else
			throw new ExpressionException("inavlid type defintion ["+type+"], valid types are [RECT_TO_POLAR,POLAR_TO_RECT,INVERT_IN_CIRCLE]");
	}

	/**
     * Get the distortion type.
     * @return the distortion type
     * @see #setType
     */
	public int getType() {
		return type;
	}

	private float sqr(float x) {
		return x*x;
	}

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float theta, t;
		float m, xmax, ymax;
		float r = 0;
		
		switch (type) {
		case RECT_TO_POLAR:
			theta = 0;
			if (x >= centreX) {
				if (y > centreY) {
					theta = ImageMath.PI - (float)Math.atan(((x - centreX))/((y - centreY)));
					r = (float)Math.sqrt(sqr (x - centreX) + sqr (y - centreY));
				} else if (y < centreY) {
					theta = (float)Math.atan (((x - centreX))/((centreY - y)));
					r = (float)Math.sqrt (sqr (x - centreX) + sqr (centreY - y));
				} else {
					theta = ImageMath.HALF_PI;
					r = x - centreX;
				}
			} else if (x < centreX) {
				if (y < centreY) {
					theta = ImageMath.TWO_PI - (float)Math.atan (((centreX -x))/((centreY - y)));
					r = (float)Math.sqrt (sqr (centreX - x) + sqr (centreY - y));
				} else if (y > centreY) {
					theta = ImageMath.PI + (float)Math.atan (((centreX - x))/((y - centreY)));
					r = (float)Math.sqrt (sqr (centreX - x) + sqr (y - centreY));
				} else {
					theta = 1.5f * ImageMath.PI;
					r = centreX - x;
				}
			}
			if (x != centreX)
				m = Math.abs (((y - centreY)) / ((x - centreX)));
			else
				m = 0;
			
			if (m <= (height / width)) {
				if (x == centreX) {
					xmax = 0;
					ymax = centreY;
				} else {
					xmax = centreX;
					ymax = m * xmax;
				}
			} else {
				ymax = centreY;
				xmax = ymax / m;
			}
			
			out[0] = (width-1) - (width - 1)/ImageMath.TWO_PI * theta;
			out[1] = height * r / radius;
			break;
		case POLAR_TO_RECT:
			theta = x / width * ImageMath.TWO_PI;
			float theta2;

			if (theta >= 1.5f * ImageMath.PI)
				theta2 = ImageMath.TWO_PI - theta;
			else if (theta >= ImageMath.PI)
				theta2 = theta - ImageMath.PI;
			else if (theta >= 0.5f * ImageMath.PI)
				theta2 = ImageMath.PI - theta;
			else
				theta2 = theta;
	
			t = (float)Math.tan(theta2);
			if (t != 0)
				m = 1.0f / t;
			else
				m = 0;
	
			if (m <= ((height) / (width))) {
				if (theta2 == 0) {
					xmax = 0;
					ymax = centreY;
				} else {
					xmax = centreX;
					ymax = m * xmax;
				}
			} else {
				ymax = centreY;
				xmax = ymax / m;
			}
	
			r = radius * (y / (height));

			float nx = -r * (float)Math.sin(theta2);
			float ny = r * (float)Math.cos(theta2);
			
			if (theta >= 1.5f * ImageMath.PI) {
				out[0] = centreX - nx;
				out[1] = centreY - ny;
			} else if (theta >= Math.PI) {
				out[0] = centreX - nx;
				out[1] = centreY + ny;
			} else if (theta >= 0.5 * Math.PI) {
				out[0] = centreX + nx;
				out[1] = centreY + ny;
			} else {
				out[0] = centreX + nx;
				out[1] = centreY - ny;
			}
			break;
		case INVERT_IN_CIRCLE:
			float dx = x-centreX;
			float dy = y-centreY;
			float distance2 = dx*dx+dy*dy;
			out[0] = centreX + centreX*centreX * dx/distance2;
			out[1] = centreY + centreY*centreY * dy/distance2;
			break;
		}
	}

	@Override
	public String toString() {
		return "Distort/Polar Coordinates...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=null;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Type")))!=null)setType(ImageFilterUtil.toString(o,"Type"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Type, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}