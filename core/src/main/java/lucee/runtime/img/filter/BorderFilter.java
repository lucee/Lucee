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

package lucee.runtime.img.filter;import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
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
 * A filter to add a border around an image using the supplied Paint, which may be null for no painting.
 */
public class BorderFilter extends AbstractBufferedImageOp  implements DynFiltering {

	private int leftBorder, rightBorder;
	private int topBorder, bottomBorder;
	private Paint borderPaint;

    /**
     * Construct a BorderFilter which does nothing.
     */
	public BorderFilter() {
	}

    /**
     * Construct a BorderFilter.
	 * @param leftBorder the left border value
	 * @param topBorder the top border value
	 * @param rightBorder the right border value
	 * @param bottomBorder the bottom border value
	 * @param borderPaint the paint with which to fill the border
     */
	public BorderFilter( int leftBorder, int topBorder, int rightBorder, int bottomBorder, Paint borderPaint ) {
		this.leftBorder = leftBorder;
		this.topBorder = topBorder;
		this.rightBorder = rightBorder;
		this.bottomBorder = bottomBorder;
		this.borderPaint = borderPaint;
	}

	/**
	 * Set the border size on the left edge.
	 * @param leftBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getLeftBorder
	 */
	public void setLeft(int leftBorder) {
		this.leftBorder = leftBorder;
	}
	
    /**
     * Returns the left border value.
     * @return the left border value.
     * @see #setLeftBorder
     */
 	public int getLeftBorder() {
		return leftBorder;
	}
	
	/**
	 * Set the border size on the right edge.
	 * @param rightBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getRightBorder
	 */
	public void setRight(int rightBorder) {
		this.rightBorder = rightBorder;
	}
	
    /**
     * Returns the right border value.
     * @return the right border value.
     * @see #setRightBorder
     */
	public int getRightBorder() {
		return rightBorder;
	}
	
	/**
	 * Set the border size on the top edge.
	 * @param topBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getTopBorder
	 */
	public void setTop(int topBorder) {
		this.topBorder = topBorder;
	}

    /**
     * Returns the top border value.
     * @return the top border value.
     * @see #setTopBorder
     */
	public int getTopBorder() {
		return topBorder;
	}

	/**
	 * Set the border size on the bottom edge.
	 * @param bottomBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getBottomBorder
	 */
	public void setBottom(int bottomBorder) {
		this.bottomBorder = bottomBorder;
	}

    /**
     * Returns the border border value.
     * @return the border border value.
     * @see #setBottomBorder
     */
	public int getBottomBorder() {
		return bottomBorder;
	}

	/**
	 * Set the border color.
	 * @param borderColor the color with which to fill the border
	 */
	public void setColor( Color borderColor ) {
		this.borderPaint = borderColor;
	}

	/**
	 * Get the border paint.
	 * @return the paint with which to fill the border
     * @see #setBorderPaint
	 */
	public Paint getBorderPaint() {
		return borderPaint;
	}

	@Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		int width = src.getWidth();
		int height = src.getHeight();

		int totalWidth=width+leftBorder+rightBorder;
		int totalHeight=height+topBorder+bottomBorder;
		
		dst=ImageUtil.createBufferedImage(src,totalWidth,totalHeight);
		
		
		Graphics2D g = dst.createGraphics();
		if ( borderPaint != null ) {
			g.setPaint( borderPaint );
			if ( leftBorder > 0 )  	g.fillRect( 0, 0, leftBorder, totalHeight );
			if ( rightBorder > 0 ) 	g.fillRect( totalWidth-rightBorder, 0, rightBorder, totalHeight );
			if ( topBorder > 0 )	g.fillRect( leftBorder, 0, totalWidth-leftBorder-rightBorder, topBorder );
			if ( bottomBorder > 0 )	g.fillRect( leftBorder, totalHeight-bottomBorder, totalWidth-leftBorder-rightBorder, bottomBorder );
		}
		g.drawRenderedImage( src, AffineTransform.getTranslateInstance( leftBorder, rightBorder ) );
		g.dispose();
		return dst;
	}

	@Override
	public String toString() {
		return "Distort/Border...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {
		
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Left")))!=null)setLeft(ImageFilterUtil.toIntValue(o,"Left"));
		if((o=parameters.removeEL(KeyImpl.init("Right")))!=null)setRight(ImageFilterUtil.toIntValue(o,"Right"));
		if((o=parameters.removeEL(KeyImpl.init("Top")))!=null)setTop(ImageFilterUtil.toIntValue(o,"Top"));
		if((o=parameters.removeEL(KeyImpl.init("Bottom")))!=null)setBottom(ImageFilterUtil.toIntValue(o,"Bottom"));
		if((o=parameters.removeEL(KeyImpl.init("Color")))!=null)setColor(ImageFilterUtil.toColor(o,"Color"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [LeftBorder, RightBorder, TopBorder, BottomBorder, BorderPaint]");
		}
		
		
		//BufferedImage dst=ImageUtil.createBufferedImage(src);
		
		
		return filter(src, (BufferedImage)null);
	}
}