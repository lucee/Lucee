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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.composite.AddComposite;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public class ShineFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private float radius = 5;
	private float angle = (float)Math.PI*7/4;
	private float distance = 5;
	private float bevel = 0.5f;
	private boolean shadowOnly = false;
	private int shineColor = 0xffffffff;
	private float brightness = 0.2f;
	private float softness = 0;

	public ShineFilter() {
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	/**
	 * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
	 * @param radius the radius of the blur in pixels.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	/**
	 * Get the radius of the kernel.
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}

	public void setBevel(float bevel) {
		this.bevel = bevel;
	}

	public float getBevel() {
		return bevel;
	}

	public void setShineColor(int shineColor) {
		this.shineColor = shineColor;
	}

	public int getShineColor() {
		return shineColor;
	}

	public void setShadowOnly(boolean shadowOnly) {
		this.shadowOnly = shadowOnly;
	}

	public boolean getShadowOnly() {
		return shadowOnly;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}
	
	public float getBrightness() {
		return brightness;
	}
	
	public void setSoftness(float softness) {
		this.softness = softness;
	}

	public float getSoftness() {
		return softness;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

		float xOffset = distance*(float)Math.cos(angle);
		float yOffset = -distance*(float)Math.sin(angle);

        BufferedImage matte = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ErodeAlphaFilter s = new ErodeAlphaFilter( bevel * 10, 0.75f, 0.1f );
        matte = s.filter( src, (BufferedImage)null );

        BufferedImage shineLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = shineLayer.createGraphics();
		g.setColor( new Color( shineColor ) );
        g.fillRect( 0, 0, width, height );
        g.setComposite( AlphaComposite.DstIn );
        g.drawRenderedImage( matte, null );
        g.setComposite( AlphaComposite.DstOut );
        g.translate( xOffset, yOffset );
        g.drawRenderedImage( matte, null );
		g.dispose();
        shineLayer = new GaussianFilter( radius ).filter( shineLayer, (BufferedImage)null );
        shineLayer = new RescaleFilter( 3*brightness ).filter( shineLayer, shineLayer );

		g = dst.createGraphics();
        g.drawRenderedImage( src, null );
        g.setComposite( new AddComposite( 1.0f ) );
        g.drawRenderedImage( shineLayer, null );
		g.dispose();

        return dst;
	}

	@Override
	public String toString() {
		return "Stylize/Shine...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Radius")))!=null)setRadius(ImageFilterUtil.toFloatValue(o,"Radius"));
		if((o=parameters.removeEL(KeyImpl.init("Brightness")))!=null)setBrightness(ImageFilterUtil.toFloatValue(o,"Brightness"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Softness")))!=null)setSoftness(ImageFilterUtil.toFloatValue(o,"Softness"));
		if((o=parameters.removeEL(KeyImpl.init("Distance")))!=null)setDistance(ImageFilterUtil.toFloatValue(o,"Distance"));
		if((o=parameters.removeEL(KeyImpl.init("ShadowOnly")))!=null)setShadowOnly(ImageFilterUtil.toBooleanValue(o,"ShadowOnly"));
		if((o=parameters.removeEL(KeyImpl.init("Bevel")))!=null)setBevel(ImageFilterUtil.toFloatValue(o,"Bevel"));
		if((o=parameters.removeEL(KeyImpl.init("ShineColor")))!=null)setShineColor(ImageFilterUtil.toColorRGB(o,"ShineColor"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Radius, Brightness, Angle, Softness, Distance, ShadowOnly, Bevel, ShineColor]");
		}

		return filter(src, dst);
	}
}