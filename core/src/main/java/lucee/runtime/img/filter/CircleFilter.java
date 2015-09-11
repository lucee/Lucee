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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
/**
 * A filter which wraps an image around a circular arc.
 */
public class CircleFilter extends TransformFilter  implements DynFiltering {

	private float radius = 10;
	private float height = 20;
	private float angle = 0;
	private float spreadAngle = (float)Math.PI;
	private float centreX = 0.5f;
	private float centreY = 0.5f;

	private float icentreX;
	private float icentreY;
	private float iWidth;
	private float iHeight;

	/**
     * Construct a CircleFilter.
     */
    public CircleFilter() {
		try {
			setEdgeAction( "ZERO" );
		} catch (ExpressionException e) {}
	}

	/**
     * Set the height of the arc.
     * @param height the height
     * @see #getHeight
     */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
     * Get the height of the arc.
     * @return the height
     * @see #setHeight
     */
	public float getHeight() {
		return height;
	}

	/**
     * Set the angle of the arc.
     * @param angle the angle of the arc.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
     * Returns the angle of the arc.
     * @return the angle of the arc.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	/**
     * Set the spread angle of the arc.
     * @param spreadAngle the angle
     * @angle
     * @see #getSpreadAngle
     */
	public void setSpreadAngle(float spreadAngle) {
		this.spreadAngle = spreadAngle;
	}

	/**
     * Get the spread angle of the arc.
     * @return the angle
     * @angle
     * @see #setSpreadAngle
     */
	public float getSpreadAngle() {
		return spreadAngle;
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
	 * Set the centre of the effect in the Y direction as a proportion of the image size.
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
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		iWidth = src.getWidth();
		iHeight = src.getHeight();
		icentreX = iWidth * centreX;
		icentreY = iHeight * centreY;
		iWidth--;
		return super.filter( src, dst );
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float dx = x-icentreX;
		float dy = y-icentreY;
		float theta = (float)Math.atan2( -dy, -dx ) + angle;
		float r = (float)Math.sqrt( dx*dx + dy*dy );

		theta = ImageMath.mod( theta, 2*(float)Math.PI );

		out[0] = iWidth * theta/(spreadAngle+0.00001f);
		out[1] = iHeight * (1-(r-radius)/(height+0.00001f));
	}

	@Override
	public String toString() {
		return "Distort/Circle...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=null;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("SpreadAngle")))!=null)setSpreadAngle(ImageFilterUtil.toFloatValue(o,"SpreadAngle"));
		if((o=parameters.removeEL(KeyImpl.init("CentreX")))!=null)setCentreX(ImageFilterUtil.toFloatValue(o,"CentreX"));
		if((o=parameters.removeEL(KeyImpl.init("CentreY")))!=null)setCentreY(ImageFilterUtil.toFloatValue(o,"CentreY"));
		//if((o=parameters.removeEL(KeyImpl.init("Centre")))!=null)setCentre(ImageFilterUtil.toPoint2D(o,"Centre"));
		if((o=parameters.removeEL(KeyImpl.init("Height")))!=null)setHeight(ImageFilterUtil.toFloatValue(o,"Height"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Angle, SpreadAngle, CentreX, CentreY, Centre, Height, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}