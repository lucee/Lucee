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

package lucee.runtime.img.filter;import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which crops an image to a given rectangle.
 */
public class CropFilter extends AbstractBufferedImageOp  implements DynFiltering {

	private int x;
	private int y;
	private int width;
	private int height;

    /**
     * Construct a CropFilter.
     */
	public CropFilter() {
		this(0, 0, 32, 32);
	}

    /**
     * Construct a CropFilter.
     * @param x the left edge of the crop rectangle
     * @param y the top edge of the crop rectangle
     * @param width the width of the crop rectangle
     * @param height the height of the crop rectangle
     */
	public CropFilter(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

    /**
     * Set the left edge of the crop rectangle.
     * @param x the left edge of the crop rectangle
     * @see #getX
     */
	public void setX(int x) {
		this.x = x;
	}

    /**
     * Get the left edge of the crop rectangle.
     * @return the left edge of the crop rectangle
     * @see #setX
     */
	public int getX() {
		return x;
	}

    /**
     * Set the top edge of the crop rectangle.
     * @param y the top edge of the crop rectangle
     * @see #getY
     */
	public void setY(int y) {
		this.y = y;
	}

    /**
     * Get the top edge of the crop rectangle.
     * @return the top edge of the crop rectangle
     * @see #setY
     */
	public int getY() {
		return y;
	}

    /**
     * Set the width of the crop rectangle.
     * @param width the width of the crop rectangle
     * @see #getWidth
     */
	public void setWidth(int width) {
		this.width = width;
	}

    /**
     * Get the width of the crop rectangle.
     * @return the width of the crop rectangle
     * @see #setWidth
     */
	public int getWidth() {
		return width;
	}

    /**
     * Set the height of the crop rectangle.
     * @param height the height of the crop rectangle
     * @see #getHeight
     */
	public void setHeight(int height) {
		this.height = height;
	}

    /**
     * Get the height of the crop rectangle.
     * @return the height of the crop rectangle
     * @see #setHeight
     */
	public int getHeight() {
		return height;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        
    	int w = src.getWidth();
        int h = src.getHeight();

        if(x<0)x=0;
        if(y<0)y=0;
        if(x>w)x=w;
        if(y>h)y=h;
        
        
		dst=ImageUtil.createBufferedImage(src,width,height);
		

        

		Graphics2D g = dst.createGraphics();
		g.drawRenderedImage( src, AffineTransform.getTranslateInstance(-x, -y) );
		g.dispose();

        return dst;
    }

	@Override
	public String toString() {
		return "Distort/Crop";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("X")))!=null)setX(ImageFilterUtil.toIntValue(o,"X"));
		if((o=parameters.removeEL(KeyImpl.init("Y")))!=null)setY(ImageFilterUtil.toIntValue(o,"Y"));
		if((o=parameters.removeEL(KeyImpl.init("Width")))!=null)setWidth(ImageFilterUtil.toIntValue(o,"Width"));
		if((o=parameters.removeEL(KeyImpl.init("Height")))!=null)setHeight(ImageFilterUtil.toIntValue(o,"Height"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [X, Y, Width, Height]");
		}

		return filter(src, (BufferedImage)null);
	}
}