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
 * A filter which simulates a lens placed over an image.
 */
public class SphereFilter extends TransformFilter  implements DynFiltering {

	private float a = 0;
	private float b = 0;
	private float a2 = 0;
	private float b2 = 0;
	private float centreX = 0.5f;
	private float centreY = 0.5f;
	private float refractionIndex = 1.5f;

	private float icentreX;
	private float icentreY;

	public SphereFilter() {
		super(ConvolveFilter.CLAMP_EDGES );
		setRadius( 100.0f );
	}

	/**
	 * Set the index of refaction.
	 * @param refractionIndex the index of refaction
     * @see #getRefractionIndex
	 */
	public void setRefractionIndex(float refractionIndex) {
		this.refractionIndex = refractionIndex;
	}

	/**
	 * Get the index of refaction.
	 * @return the index of refaction
     * @see #setRefractionIndex
	 */
	public float getRefractionIndex() {
		return refractionIndex;
	}

	/**
	 * Set the radius of the effect.
	 * @param r the radius
     * @min-value 0
     * @see #getRadius
	 */
	public void setRadius(float r) {
		this.a = r;
		this.b = r;
	}

	/**
	 * Get the radius of the effect.
	 * @return the radius
     * @see #setRadius
	 */
	public float getRadius() {
		return a;
	}

	/**
	 * Set the centre of the effect in the X direction as a proportion of the image size.
	 * @param centreX the center
     * @see #getCentreX
	 */
	public void setCentreX( float centreX ) {
		this.centreX = centreX;
	}

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
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		int width = src.getWidth();
		int height = src.getHeight();
		icentreX = width * centreX;
		icentreY = height * centreY;
		if (a == 0)
			a = width/2;
		if (b == 0)
			b = height/2;
		a2 = a*a;
		b2 = b*b;
		return super.filter( src, dst );
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float dx = x-icentreX;
		float dy = y-icentreY;
		float x2 = dx*dx;
		float y2 = dy*dy;
		if (y2 >= (b2 - (b2*x2)/a2)) {
			out[0] = x;
			out[1] = y;
		} else {
			float rRefraction = 1.0f / refractionIndex;

			float z = (float)Math.sqrt((1.0f - x2/a2 - y2/b2) * (a*b));
			float z2 = z*z;

			float xAngle = (float)Math.acos(dx / Math.sqrt(x2+z2));
			float angle1 = ImageMath.HALF_PI - xAngle;
			float angle2 = (float)Math.asin(Math.sin(angle1)*rRefraction);
			angle2 = ImageMath.HALF_PI - xAngle - angle2;
			out[0] = x - (float)Math.tan(angle2)*z;

			float yAngle = (float)Math.acos(dy / Math.sqrt(y2+z2));
			angle1 = ImageMath.HALF_PI - yAngle;
			angle2 = (float)Math.asin(Math.sin(angle1)*rRefraction);
			angle2 = ImageMath.HALF_PI - yAngle - angle2;
			out[1] = y - (float)Math.tan(angle2)*z;
		}
	}

	@Override
	public String toString() {
		return "Distort/Sphere...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=null;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("CentreX")))!=null)setCentreX(ImageFilterUtil.toFloatValue(o,"CentreX"));
		if((o=parameters.removeEL(KeyImpl.init("CentreY")))!=null)setCentreY(ImageFilterUtil.toFloatValue(o,"CentreY"));
		//if((o=parameters.removeEL(KeyImpl.init("Centre")))!=null)setCentre(ImageFilterUtil.toPoint2D(o,"Centre"));
		if((o=parameters.removeEL(KeyImpl.init("RefractionIndex")))!=null)setRefractionIndex(ImageFilterUtil.toFloatValue(o,"RefractionIndex"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, CentreX, CentreY, Centre, RefractionIndex, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}