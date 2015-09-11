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

package lucee.runtime.img.filter;import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which draws a drop shadow based on the alpha channel of the image.
 */
public class ShadowFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private float radius = 5;
	private float angle = (float)Math.PI*6/4;
	private float distance = 5;
	private float opacity = 0.5f;
	private boolean addMargins = false;
	private boolean shadowOnly = false;
	private int shadowColor = 0xff000000;

	/**
     * Construct a ShadowFilter.
     */
    public ShadowFilter() {
	}

	/**
     * Construct a ShadowFilter.
     * @param radius the radius of the shadow
     * @param xOffset the X offset of the shadow
     * @param yOffset the Y offset of the shadow
     * @param opacity the opacity of the shadow
     */
	public ShadowFilter(float radius, float xOffset, float yOffset, float opacity) {
		this.radius = radius;
		this.angle = (float)Math.atan2(yOffset, xOffset);
		this.distance = (float)Math.sqrt(xOffset*xOffset + yOffset*yOffset);
		this.opacity = opacity;
	}

	/**
     * Specifies the angle of the shadow.
     * @param angle the angle of the shadow.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
     * Returns the angle of the shadow.
     * @return the angle of the shadow.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	/**
     * Set the distance of the shadow.
     * @param distance the distance.
     * @see #getDistance
     */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
     * Get the distance of the shadow.
     * @return the distance.
     * @see #setDistance
     */
	public float getDistance() {
		return distance;
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
     * @see #getRadius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	/**
	 * Get the radius of the kernel.
	 * @return the radius
     * @see #setRadius
	 */
	public float getRadius() {
		return radius;
	}

	/**
     * Set the opacity of the shadow.
     * @param opacity the opacity.
     * @see #getOpacity
     */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	/**
     * Get the opacity of the shadow.
     * @return the opacity.
     * @see #setOpacity
     */
	public float getOpacity() {
		return opacity;
	}

	/**
     * Set the color of the shadow.
     * @param shadowColor the color.
     * @see #getShadowColor
     */
	public void setShadowColor(int shadowColor) {
		this.shadowColor = shadowColor;
	}

	/**
     * Get the color of the shadow.
     * @return the color.
     * @see #setShadowColor
     */
	public int getShadowColor() {
		return shadowColor;
	}

	/**
     * Set whether to increase the size of the output image to accomodate the shadow.
     * @param addMargins true to add margins.
     * @see #getAddMargins
     */
	public void setAddMargins(boolean addMargins) {
		this.addMargins = addMargins;
	}

	/**
     * Get whether to increase the size of the output image to accomodate the shadow.
     * @return true to add margins.
     * @see #setAddMargins
     */
	public boolean getAddMargins() {
		return addMargins;
	}

	/**
     * Set whether to only draw the shadow without the original image.
     * @param shadowOnly true to only draw the shadow.
     * @see #getShadowOnly
     */
	public void setShadowOnly(boolean shadowOnly) {
		this.shadowOnly = shadowOnly;
	}

	/**
     * Get whether to only draw the shadow without the original image.
     * @return true to only draw the shadow.
     * @see #setShadowOnly
     */
	public boolean getShadowOnly() {
		return shadowOnly;
	}

    @Override
	public Rectangle2D getBounds2D( BufferedImage src ) {
        Rectangle r = new Rectangle(0, 0, src.getWidth(), src.getHeight());
		if ( addMargins ) {
			float xOffset = distance*(float)Math.cos(angle);
			float yOffset = -distance*(float)Math.sin(angle);
			r.width += (int)(Math.abs(xOffset)+2*radius);
			r.height += (int)(Math.abs(yOffset)+2*radius);
		}
        return r;
    }
    
    @Override
	public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();

		if ( addMargins ) {
            float xOffset = distance*(float)Math.cos(angle);
            float yOffset = -distance*(float)Math.sin(angle);
			float topShadow = Math.max( 0, radius-yOffset );
			float leftShadow = Math.max( 0, radius-xOffset );
            dstPt.setLocation( srcPt.getX()+leftShadow, srcPt.getY()+topShadow );
		} else
            dstPt.setLocation( srcPt.getX(), srcPt.getY() );

        return dstPt;
    }

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null ) {
            if ( addMargins ) {
				ColorModel cm = src.getColorModel();
				dst = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);
			} else
				dst = createCompatibleDestImage( src, null );
		}

        float shadowR = ((shadowColor >> 16) & 0xff) / 255f;
        float shadowG = ((shadowColor >> 8) & 0xff) / 255f;
        float shadowB = (shadowColor & 0xff) / 255f;

		// Make a black mask from the image's alpha channel 
        float[][] extractAlpha = {
            { 0, 0, 0, shadowR },
            { 0, 0, 0, shadowG },
            { 0, 0, 0, shadowB },
            { 0, 0, 0, opacity }
        };
        BufferedImage shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        new BandCombineOp( extractAlpha, null ).filter( src.getRaster(), shadow.getRaster() );
        shadow = new GaussianFilter( radius ).filter( shadow, (BufferedImage)null );

		float xOffset = distance*(float)Math.cos(angle);
		float yOffset = -distance*(float)Math.sin(angle);

		Graphics2D g = dst.createGraphics();
		g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
		if ( addMargins ) {
			//float radius2 = radius/2;
			float topShadow = Math.max( 0, radius-yOffset );
			float leftShadow = Math.max( 0, radius-xOffset );
			g.translate( topShadow, leftShadow );
		}
		g.drawRenderedImage( shadow, AffineTransform.getTranslateInstance( xOffset, yOffset ) );
		if ( !shadowOnly ) {
			g.setComposite( AlphaComposite.SrcOver );
			g.drawRenderedImage( src, null );
		}
		g.dispose();

        return dst;
	}

	@Override
	public String toString() {
		return "Stylize/Drop Shadow...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Distance")))!=null)setDistance(ImageFilterUtil.toFloatValue(o,"Distance"));
		if((o=parameters.removeEL(KeyImpl.init("Opacity")))!=null)setOpacity(ImageFilterUtil.toFloatValue(o,"Opacity"));
		if((o=parameters.removeEL(KeyImpl.init("ShadowColor")))!=null)setShadowColor(ImageFilterUtil.toColorRGB(o,"ShadowColor"));
		if((o=parameters.removeEL(KeyImpl.init("AddMargins")))!=null)setAddMargins(ImageFilterUtil.toBooleanValue(o,"AddMargins"));
		if((o=parameters.removeEL(KeyImpl.init("ShadowOnly")))!=null)setShadowOnly(ImageFilterUtil.toBooleanValue(o,"ShadowOnly"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Angle, Distance, Opacity, ShadowColor, AddMargins, ShadowOnly]");
		}

		return filter(src, dst);
	}
}