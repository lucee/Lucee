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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which renders "glints" on bright parts of the image.
 */
public class GlintFilter extends AbstractBufferedImageOp  implements DynFiltering {

    private float threshold = 1.0f;
    private int length = 5;
    private float blur = 0.0f;
    private float amount = 0.1f;
	private boolean glintOnly = false;
	private Colormap colormap = new LinearColormap( 0xffffffff, 0xff000000 );

    public GlintFilter() {
	}
	
	/**
     * Set the threshold value.
     * @param threshold the threshold value
     * @see #getThreshold
     */
	public void setThreshold( float threshold ) {
		this.threshold = threshold;
	}
	
	/**
     * Get the threshold value.
     * @return the threshold value
     * @see #setThreshold
     */
	public float getThreshold() {
		return threshold;
	}
	
	/**
	 * Set the amount of glint.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount( float amount ) {
		this.amount = amount;
	}
	
	/**
	 * Get the amount of glint.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}
	
	/**
     * Set the length of the stars.
     * @param length the length
     * @see #getLength
     */
	public void setLength( int length ) {
		this.length = length;
	}
	
	/**
     * Get the length of the stars.
     * @return the length
     * @see #setLength
     */
	public int getLength() {
		return length;
	}
	
	/**
     * Set the blur that is applied before thresholding.
     * @param blur the blur radius
     * @see #getBlur
     */
	public void setBlur(float blur) {
		this.blur = blur;
	}

	/**
     * Set the blur that is applied before thresholding.
     * @return the blur radius
     * @see #setBlur
     */
	public float getBlur() {
		return blur;
	}
	
	/**
     * Set whether to render the stars and the image or only the stars.
     * @param glintOnly true to render only stars
     * @see #getGlintOnly
     */
	public void setGlintOnly(boolean glintOnly) {
		this.glintOnly = glintOnly;
	}

	/**
     * Get whether to render the stars and the image or only the stars.
     * @return true to render only stars
     * @see #setGlintOnly
     */
	public boolean getGlintOnly() {
		return glintOnly;
	}
	
    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();
		int[] pixels = new int[width];
		int length2 = (int)(length / 1.414f);
		int[] colors = new int[length+1];
		int[] colors2 = new int[length2+1];

		if ( colormap != null ) {
			for (int i = 0; i <= length; i++) {
				int argb = colormap.getColor( (float)i/length );
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = argb & 0xff;
				argb = (argb & 0xff000000) | ((int)(amount*r) << 16) | ((int)(amount*g) << 8) | (int)(amount*b);
				colors[i] = argb;
			}
			for (int i = 0; i <= length2; i++) {
				int argb = colormap.getColor( (float)i/length2 );
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = argb & 0xff;
				argb = (argb & 0xff000000) | ((int)(amount*r) << 16) | ((int)(amount*g) << 8) | (int)(amount*b);
				colors2[i] = argb;
			}
		}

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int threshold3 = (int)(threshold*3*255);
		for ( int y = 0; y < height; y++ ) {
			getRGB( src, 0, y, width, 1, pixels );
			for ( int x = 0; x < width; x++ ) {
				int rgb = pixels[x];
				int a = rgb & 0xff000000;
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				int l = r + g + b;
				if (l < threshold3)
					pixels[x] = 0xff000000;
				else {
					l /= 3;
					pixels[x] = a | (l << 16) | (l << 8) | l;
				}
			}
			setRGB( mask, 0, y, width, 1, pixels );
		}

		if ( blur != 0 )
			mask = new GaussianFilter(blur).filter( mask, (BufferedImage)null );

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		int[] dstPixels;
		if ( glintOnly )
			dstPixels = new int[width*height];
		else
			dstPixels = getRGB( src, 0, 0, width, height, null );//FIXME - only need 2*length

		for ( int y = 0; y < height; y++ ) {
			int index = y*width;
			getRGB( mask, 0, y, width, 1, pixels );
			int ymin = Math.max( y-length, 0 )-y;
			int ymax = Math.min( y+length, height-1 )-y;
			int ymin2 = Math.max( y-length2, 0 )-y;
			int ymax2 = Math.min( y+length2, height-1 )-y;
			for ( int x = 0; x < width; x++ ) {
				if ( (pixels[x] & 0xff) > threshold*255 ) {
					int xmin = Math.max( x-length, 0 )-x;
					int xmax = Math.min( x+length, width-1 )-x;
					int xmin2 = Math.max( x-length2, 0 )-x;
					int xmax2 = Math.min( x+length2, width-1 )-x;

					// Horizontal
					for ( int i = 0, k = 0; i <= xmax; i++, k++ )
						dstPixels[index+i] = PixelUtils.combinePixels( dstPixels[index+i], colors[k], PixelUtils.ADD );
					for ( int i = -1, k = 1; i >= xmin; i--, k++ )
						dstPixels[index+i] = PixelUtils.combinePixels( dstPixels[index+i], colors[k], PixelUtils.ADD );
					// Vertical
					for ( int i = 1, j = index+width, k = 0; i <= ymax; i++, j += width, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors[k], PixelUtils.ADD );
					for ( int i = -1, j = index-width, k = 0; i >= ymin; i--, j -= width, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors[k], PixelUtils.ADD );

					// Diagonals
					//int xymin = Math.max( xmin2, ymin2 );
					//int xymax = Math.min( xmax2, ymax2 );
					// SE
					int count = Math.min( xmax2, ymax2 );
					for ( int i = 1, j = index+width+1, k = 0; i <= count; i++, j += width+1, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors2[k], PixelUtils.ADD );
					// NW
					count = Math.min( -xmin2, -ymin2 );
					for ( int i = 1, j = index-width-1, k = 0; i <= count; i++, j -= width+1, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors2[k], PixelUtils.ADD );
					// NE
					count = Math.min( xmax2, -ymin2 );
					for ( int i = 1, j = index-width+1, k = 0; i <= count; i++, j += -width+1, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors2[k], PixelUtils.ADD );
					// SW
					count = Math.min( -xmin2, ymax2 );
					for ( int i = 1, j = index+width-1, k = 0; i <= count; i++, j += width-1, k++ )
						dstPixels[j] = PixelUtils.combinePixels( dstPixels[j], colors2[k], PixelUtils.ADD );
				}
				index++;
			}
		}
		setRGB( dst, 0, 0, width, height, dstPixels );

        return dst;
    }
    
	@Override
	public String toString() {
		return "Effects/Glint...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Blur")))!=null)setBlur(ImageFilterUtil.toFloatValue(o,"Blur"));
		if((o=parameters.removeEL(KeyImpl.init("GlintOnly")))!=null)setGlintOnly(ImageFilterUtil.toBooleanValue(o,"GlintOnly"));
		if((o=parameters.removeEL(KeyImpl.init("Length")))!=null)setLength(ImageFilterUtil.toIntValue(o,"Length"));
		if((o=parameters.removeEL(KeyImpl.init("Threshold")))!=null)setThreshold(ImageFilterUtil.toFloatValue(o,"Threshold"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Amount, Blur, GlintOnly, Length, Threshold]");
		}

		return filter(src, dst);
	}
}