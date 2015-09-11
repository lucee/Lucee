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

package lucee.runtime.img.filter;import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which produces motion blur the slow, but higher-quality way.
 */
public class MotionBlurFilter extends AbstractBufferedImageOp  implements DynFiltering {

	private float angle = 0.0f;
	private float falloff = 1.0f;
	private float distance = 1.0f;
	private float zoom = 0.0f;
	private float rotation = 0.0f;
	private boolean wrapEdges = false;
	private boolean premultiplyAlpha = true;

    /**
     * Construct a MotionBlurFilter.
     */
	public MotionBlurFilter() {
	}

    /**
     * Construct a MotionBlurFilter.
     * @param distance the distance of blur.
     * @param angle the angle of blur.
     * @param rotation the angle of rotation.
     * @param zoom the zoom factor.
     */
	public MotionBlurFilter( float distance, float angle, float rotation, float zoom ) {
        this.distance = distance;
        this.angle = angle;
        this.rotation = rotation;
        this.zoom = zoom;
    }
    
	/**
     * Specifies the angle of blur.
     * @param angle the angle of blur.
     * @angle
     * @see #getAngle
     */
	public void setAngle( float angle ) {
		this.angle = angle;
	}

	/**
     * Returns the angle of blur.
     * @return the angle of blur.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}
	
	/**
     * Set the distance of blur.
     * @param distance the distance of blur.
     * @see #getDistance
     */
	public void setDistance( float distance ) {
		this.distance = distance;
	}

	/**
     * Get the distance of blur.
     * @return the distance of blur.
     * @see #setDistance
     */
	public float getDistance() {
		return distance;
	}
	
	/**
     * Set the blur rotation.
     * @param rotation the angle of rotation.
     * @see #getRotation
     */
	public void setRotation( float rotation ) {
		this.rotation = rotation;
	}

	/**
     * Get the blur rotation.
     * @return the angle of rotation.
     * @see #setRotation
     */
	public float getRotation() {
		return rotation;
	}
	
	public void setZoom( float zoom ) {
		this.zoom = zoom;
	}

	/**
     * Get the blur zoom.
     * @return the zoom factor.
     * @see #setZoom
     */
	public float getZoom() {
		return zoom;
	}
	
	/**
     * Set whether to wrap at the image edges.
     * @param wrapEdges true if it should wrap.
     * @see #getWrapEdges
     */
	public void setWrapEdges(boolean wrapEdges) {
		this.wrapEdges = wrapEdges;
	}

	/**
     * Get whether to wrap at the image edges.
     * @return true if it should wrap.
     * @see #setWrapEdges
     */
	public boolean getWrapEdges() {
		return wrapEdges;
	}

    /**
     * Set whether to premultiply the alpha channel.
     * @param premultiplyAlpha true to premultiply the alpha
     * @see #getPremultiplyAlpha
     */
	public void setPremultiplyAlpha( boolean premultiplyAlpha ) {
		this.premultiplyAlpha = premultiplyAlpha;
	}

    /**
     * Get whether to premultiply the alpha channel.
     * @return true to premultiply the alpha
     * @see #setPremultiplyAlpha
     */
	public boolean getPremultiplyAlpha() {
		return premultiplyAlpha;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );

		//float sinAngle = (float)Math.sin(angle);
		//float cosAngle = (float)Math.cos(angle);

		//float total;
		int cx = width/2;
		int cy = height/2;
		int index = 0;

        float imageRadius = (float)Math.sqrt( cx*cx + cy*cy );
        float translateX = (float)(distance * Math.cos( angle ));
        float translateY = (float)(distance * -Math.sin( angle ));
		float maxDistance = distance + Math.abs(rotation*imageRadius) + zoom*imageRadius;
		int repetitions = (int)maxDistance;
		AffineTransform t = new AffineTransform();
		Point2D.Float p = new Point2D.Float();

        if ( premultiplyAlpha )
			ImageMath.premultiply( inPixels, 0, inPixels.length );
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int a = 0, r = 0, g = 0, b = 0;
				int count = 0;
				for (int i = 0; i < repetitions; i++) {
					int newX = x, newY = y;
					float f = (float)i/repetitions;

					p.x = x;
					p.y = y;
					t.setToIdentity();
					t.translate( cx+f*translateX, cy+f*translateY );
					float s = 1-zoom*f;
					t.scale( s, s );
					if ( rotation != 0 )
						t.rotate( -rotation*f );
					t.translate( -cx, -cy );
					t.transform( p, p );
					newX = (int)p.x;
					newY = (int)p.y;

					if (newX < 0 || newX >= width) {
						if ( wrapEdges )
							newX = ImageMath.mod( newX, width );
						else
							break;
					}
					if (newY < 0 || newY >= height) {
						if ( wrapEdges )
							newY = ImageMath.mod( newY, height );
						else
							break;
					}

					count++;
					int rgb = inPixels[newY*width+newX];
					a += (rgb >> 24) & 0xff;
					r += (rgb >> 16) & 0xff;
					g += (rgb >> 8) & 0xff;
					b += rgb & 0xff;
				}
				if (count == 0) {
					outPixels[index] = inPixels[index];
				} else {
					a = PixelUtils.clamp((a/count));
					r = PixelUtils.clamp((r/count));
					g = PixelUtils.clamp((g/count));
					b = PixelUtils.clamp((b/count));
					outPixels[index] = (a << 24) | (r << 16) | (g << 8) | b;
				}
				index++;
			}
		}
        if ( premultiplyAlpha )
			ImageMath.unpremultiply( outPixels, 0, inPixels.length );

        setRGB( dst, 0, 0, width, height, outPixels );
        return dst;
    }

	@Override
	public String toString() {
		return "Blur/Motion Blur...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("PremultiplyAlpha")))!=null)setPremultiplyAlpha(ImageFilterUtil.toBooleanValue(o,"PremultiplyAlpha"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Distance")))!=null)setDistance(ImageFilterUtil.toFloatValue(o,"Distance"));
		if((o=parameters.removeEL(KeyImpl.init("Rotation")))!=null)setRotation(ImageFilterUtil.toFloatValue(o,"Rotation"));
		if((o=parameters.removeEL(KeyImpl.init("Zoom")))!=null)setZoom(ImageFilterUtil.toFloatValue(o,"Zoom"));
		if((o=parameters.removeEL(KeyImpl.init("WrapEdges")))!=null)setWrapEdges(ImageFilterUtil.toBooleanValue(o,"WrapEdges"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [PremultiplyAlpha, Angle, Distance, Rotation, Zoom, WrapEdges]");
		}

		return filter(src, dst);
	}
}