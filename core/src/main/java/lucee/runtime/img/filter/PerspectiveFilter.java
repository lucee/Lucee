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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which performs a perspective distortion on an image.
 */
public class PerspectiveFilter extends TransformFilter  implements DynFiltering {

	private float xlt, ylt, xrt, yrt, xrb, yrb, xlb, ylb;
	private float dx1, dy1, dx2, dy2, dx3, dy3;
	private float A, B, C, D, E, F, G, H, I;
	
	/**
     * Construct a PerspectiveFilter.
     */
    public PerspectiveFilter() {
		this(0, 0, 0, 0, 0, 0, 0, 0);
		//this(0, 0, 100, 0, 100, 100, 0, 100);
	}
	
	/**
     * Construct a PerspectiveFilter.
     * @param x0 the new position of the top left corner
     * @param y0 the new position of the top left corner
     * @param x1 the new position of the top right corner
     * @param y1 the new position of the top right corner
     * @param x2 the new position of the bottom right corner
     * @param y2 the new position of the bottom right corner
     * @param x3 the new position of the bottom left corner
     * @param y3 the new position of the bottom left corner
     */
	public PerspectiveFilter(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		setCorners(x0, y0, x1, y1, x2, y2, x3, y3);
	}
	
	/**
     * Set the new positions of the image corners.
     * @param x0 the new position of the top left corner
     * @param y0 the new position of the top left corner
     * @param x1 the new position of the top right corner
     * @param y1 the new position of the top right corner
     * @param x2 the new position of the bottom right corner
     * @param y2 the new position of the bottom right corner
     * @param x3 the new position of the bottom left corner
     * @param y3 the new position of the bottom left corner
     */
	public void setCorners(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		this.xlt = x0;
		this.ylt = y0;
		this.xrt = x1;
		this.yrt = y1;
		this.xrb = x2;
		this.yrb = y2;
		this.xlb = x3;
		this.ylb = y3;
		
		dx1 = x1-x2;
		dy1 = y1-y2;
		dx2 = x3-x2;
		dy2 = y3-y2;
		dx3 = x0-x1+x2-x3;
		dy3 = y0-y1+y2-y3;
		
		float a11, a12, a13, a21, a22, a23, a31, a32;

		if (dx3 == 0 && dy3 == 0) {
			a11 = x1-x0;
			a21 = x2-x1;
			a31 = x0;
			a12 = y1-y0;
			a22 = y2-y1;
			a32 = y0;
			a13 = a23 = 0;
		} else {
			a13 = (dx3*dy2-dx2*dy3)/(dx1*dy2-dy1*dx2);
			a23 = (dx1*dy3-dy1*dx3)/(dx1*dy2-dy1*dx2);
			a11 = x1-x0+a13*x1;
			a21 = x3-x0+a23*x3;
			a31 = x0;
			a12 = y1-y0+a13*y1;
			a22 = y3-y0+a23*y3;
			a32 = y0;
		}

	    A = a22 - a32*a23;
	    B = a31*a23 - a21;
	    C = a21*a32 - a31*a22;
	    D = a32*a13 - a12;
	    E = a11 - a31*a13;
	    F = a31*a12 - a11*a32;
	    G = a12*a23 - a22*a13;
	    H = a21*a13 - a11*a23;
	    I = a11*a22 - a21*a12;
	}
	
	/**
	 * the new horizontal position of the top left corner, negative values are translated to image-width - x.
	 * @param x0 the x0 to set
	 */
	public void setXLT(float xlt) {
		this.xlt = xlt;
	}

	/**
	 * the new vertical position of the top left corner, negative values are translated to image-height - y.
	 * @param y0 the y0 to set
	 */
	public void setYLT(float ylt) {
		this.ylt = ylt;
	}

	/**
	 * the new horizontal position of the top right corner, negative values are translated to image-width - x.
	 * @param x1 the x1 to set
	 */
	public void setXRT(float xrt) {
		this.xrt = xrt;
	}

	/**
	 * the new vertical position of the top right corner, negative values are translated to image-height - y.
	 * @param y1 the y1 to set
	 */
	public void setYRT(float yrt) {
		this.yrt = yrt;
	}

	/**
	 * the new horizontal position of the bottom right corner, negative values are translated to image-width - x.
	 * @param x2 the x2 to set
	 */
	public void setXRB(float xrb) {
		this.xrb = xrb;
	}
	
	/**
	 * the new vertical position of the bottom right corner, negative values are translated to image-height - y.
	 * @param y2 the y2 to set
	 */
	public void setYRB(float yrb) {
		this.yrb = yrb;
	}

	/**
	 * the new horizontal position of the bottom left corner, negative values are translated to image-width - x.
	 * @param xlb the x3 to set
	 */
	public void setXLB(float xlb) {
		this.xlb = xlb;
	}

	/**
	 * the new vertical position of the bottom left corner, negative values are translated to image-height - y.
	 * @param y3 the y3 to set
	 */
	public void setYLB(float ylb) {
		this.ylb = ylb;
	}


	@Override
	protected void transformSpace(Rectangle rect) {
		rect.x = (int)Math.min( Math.min( xlt, xrt ), Math.min( xrb, xlb ) );
		rect.y = (int)Math.min( Math.min( ylt, yrt ), Math.min( yrb, ylb ) );
		rect.width = (int)Math.max( Math.max( xlt, xrt ), Math.max( xrb, xlb ) ) - rect.x;
		rect.height = (int)Math.max( Math.max( ylt, yrt ), Math.max( yrb, ylb ) ) - rect.y;
	}

    /**
     * Get the origin of the output image. Use this for working out where to draw your new image.
     * @return the X origin.
     */
	public float getOriginX() {
		return xlt - (int)Math.min( Math.min( xlt, xrt ), Math.min( xrb, xlb ) );
	}

    /**
     * Get the origin of the output image. Use this for working out where to draw your new image.
     * @return the Y origin.
     */
	public float getOriginY() {
		return ylt - (int)Math.min( Math.min( ylt, yrt ), Math.min( yrb, ylb ) );
	}

/*
    public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();

		dx1 = x1-x2;
		dy1 = y1-y2;
		dx2 = x3-x2;
		dy2 = y3-y2;
		dx3 = x0-x1+x2-x3;
		dy3 = y0-y1+y2-y3;
		
		float a11, a12, a13, a21, a22, a23, a31, a32;

		if (dx3 == 0 && dy3 == 0) {
			a11 = x1-x0;
			a21 = x2-x1;
			a31 = x0;
			a12 = y1-y0;
			a22 = y2-y1;
			a32 = y0;
			a13 = a23 = 0;
		} else {
			a13 = (dx3*dy2-dx2*dy3)/(dx1*dy2-dy1*dx2);
			a23 = (dx1*dy3-dy1*dx3)/(dx1*dy2-dy1*dx2);
			a11 = x1-x0+a13*x1;
			a21 = x3-x0+a23*x3;
			a31 = x0;
			a12 = y1-y0+a13*y1;
			a22 = y3-y0+a23*y3;
			a32 = y0;
		}

		float x = (float)srcPt.getX();
		float y = (float)srcPt.getY();
		float D = 1.0f/(a13*x + a23*y + 1);

        dstPt.setLocation( (a11*x + a21*y + a31)*D, (a12*x + a22*y + a32)*D );
        return dstPt;
    }
*/

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		out[0] = originalSpace.width * (A*x+B*y+C)/(G*x+H*y+I);
		out[1] = originalSpace.height * (D*x+E*y+F)/(G*x+H*y+I);
	}

	@Override
	public String toString() {
		return "Distort/Perspective...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("xlt")))!=null)setXLT(ImageFilterUtil.toFloatValue(o,"xlt"));
		if((o=parameters.removeEL(KeyImpl.init("ylt")))!=null)setYLT(ImageFilterUtil.toFloatValue(o,"ylt"));
		
		if((o=parameters.removeEL(KeyImpl.init("xrt")))!=null)setXRT(ImageFilterUtil.toFloatValue(o,"xrt"));
		if((o=parameters.removeEL(KeyImpl.init("yrt")))!=null)setYRT(ImageFilterUtil.toFloatValue(o,"yrt"));
		
		if((o=parameters.removeEL(KeyImpl.init("xrb")))!=null)setXRB(ImageFilterUtil.toFloatValue(o,"xrb"));
		if((o=parameters.removeEL(KeyImpl.init("yrb")))!=null)setYRB(ImageFilterUtil.toFloatValue(o,"yrb"));
		
		if((o=parameters.removeEL(KeyImpl.init("xlb")))!=null)setXLB(ImageFilterUtil.toFloatValue(o,"xlb"));
		if((o=parameters.removeEL(KeyImpl.init("ylb")))!=null)setYLB(ImageFilterUtil.toFloatValue(o,"ylb"));
		
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Corners, EdgeAction, Interpolation]");
		}

		return filter(src, (BufferedImage)null);
	}
	
	@Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        
		int width = src.getWidth();
	    int height = src.getHeight();

	    if(xrt==0)xrt=width;
	    if(xrb==0)xrb=width;
	    if(yrb==0)yrb=height;
	    if(ylb==0)ylb=height;
	    
	    if(xlt<0) xlt=width+xlt;
	    if(xrt<0) xrt=width+xrt;
	    if(xrb<0) xrb=width+xrb;
	    if(xlb<0) xlb=width+xlb;
	    
	    if(ylt<0) ylt=width+ylt;
	    if(yrt<0) yrt=width+yrt;
	    if(yrb<0) yrb=width+yrb;
	    if(ylb<0) ylb=width+ylb;
	    
	    
	    
	    setCorners(xlt, ylt, xrt, yrt, xrb, yrb, xlb, ylb);
	    

	    float t=ylt<yrt?ylt:yrt;
	    float l=xlt<xlb?xlt:xlb;
	    
	    float b=ylb>yrb?ylb:yrb;
	    float r=xrt>xrb?xrt:xrb;
	    
	    float w=r-l;
	    float h=b-t;
	    
	    dst=ImageUtil.createBufferedImage(src,Math.round(w),Math.round(h));
	    
	    
	    
	    return super.filter(src, dst);
		        
	}
}