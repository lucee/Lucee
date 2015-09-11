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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * An abstract superclass for filters which distort images in some way. The subclass only needs to override
 * two methods to provide the mapping between source and destination pixels.
 */
public abstract class TransformFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
    

    /**
     * Use nearest-neighbour interpolation.
     */
	public final static int NEAREST_NEIGHBOUR = 0;

    /**
     * Use bilinear interpolation.
     */
	public final static int BILINEAR = 1;

    /**
     * The action to take for pixels off the image edge.
     */
	protected int edgeAction = ConvolveFilter.ZERO_EDGES;

    /**
     * The type of interpolation to use.
     */
	protected int interpolation = BILINEAR;

    /**
     * The output image rectangle.
     */
	protected Rectangle transformedSpace;

    /**
     * The input image rectangle.
     */
	protected Rectangle originalSpace;


	public TransformFilter(){
	}
	public TransformFilter(int edgeAction){
		this.edgeAction=edgeAction;
	}
	
    /**
     * Set the action to perfomr for pixels off the image edges.
     * valid values are:
     * - clamp (default): Clamp pixels off the edge to the nearest edge.
     * - wrap: Wrap pixels off the edge to the opposite edge.
     * - zero: Treat pixels off the edge as zero
     * 
     * @param edgeAction the action
     * @throws ExpressionException 
     */
	public void setEdgeAction(String edgeAction) throws ExpressionException {
		String str=edgeAction.trim().toUpperCase();
		if("ZERO".equals(str)) this.edgeAction = ConvolveFilter.ZERO_EDGES;
		else if("CLAMP".equals(str)) this.edgeAction = ConvolveFilter.CLAMP_EDGES;
		else if("WRAP".equals(str)) this.edgeAction = ConvolveFilter.WRAP_EDGES;
		else 
			throw new ExpressionException("invalid value ["+edgeAction+"] for edgeAction, valid values are [clamp,wrap,zero]");
	}


    /**
     * Get the action to perform for pixels off the edge of the image.
     * @return one of ZERO, CLAMP or WRAP
     * @see #setEdgeAction
     */
	public int getEdgeAction() {
		return edgeAction;
	}
	
    /**
     * Set the type of interpolation to perform.
     * valid values are:
     * - bilinear (default): Use bilinear interpolation.
     * - nearest_neighbour: Use nearest-neighbour interpolation.
     * 
     * @param interpolation one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #getInterpolation
     */
	public void setInterpolation(String interpolation) throws ExpressionException {
		String str=interpolation.trim().toUpperCase();
		if("NEAREST_NEIGHBOUR".equals(str)) this.interpolation = NEAREST_NEIGHBOUR;
		else if("BILINEAR".equals(str)) this.interpolation = BILINEAR;
		else 
			throw new ExpressionException("invalid value ["+interpolation+"] for interpolation, valid values are [bilinear,nearest_neighbour]");
	}

    /**
     * Get the type of interpolation to perform.
     * @return one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #setInterpolation
     */
	public int getInterpolation() {
		return interpolation;
	}
	
    /**
     * Inverse transform a point. This method needs to be overriden by all subclasses.
     * @param x the X position of the pixel in the output image
     * @param y the Y position of the pixel in the output image
     * @param out the position of the pixel in the input image
     */
	protected abstract void transformInverse(int x, int y, float[] out);

    /**
     * Forward transform a rectangle. Used to determine the size of the output image.
     * @param rect the rectangle to transform
     */
	protected void transformSpace(Rectangle rect) {
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
      	
    	
    	
    	int width = src.getWidth();
        int height = src.getHeight();
        
        
        
        
        
		//int type = src.getType();
		//WritableRaster srcRaster = 
        src.getRaster();

		originalSpace = new Rectangle(0, 0, width, height);
		transformedSpace = new Rectangle(0, 0, width, height);
		transformSpace(transformedSpace);
		
		

        if (dst == null ) {
        	dst=ImageUtil.createBufferedImage(src,transformedSpace.width,transformedSpace.height);
        }
        
        //WritableRaster dstRaster = 
        dst.getRaster();

		int[] inPixels = getRGB( src, 0, 0, width, height, null );

		if ( interpolation == NEAREST_NEIGHBOUR )
			return filterPixelsNN( dst, width, height, inPixels, transformedSpace );

		int srcWidth = width;
		int srcHeight = height;
		int srcWidth1 = width-1;
		int srcHeight1 = height-1;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY;
		//int index = 0;
		int[] outPixels = new int[outWidth];

		outX = transformedSpace.x;
		outY = transformedSpace.y;
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX+x, outY+y, out);
				int srcX = (int)Math.floor( out[0] );
				int srcY = (int)Math.floor( out[1] );
				float xWeight = out[0]-srcX;
				float yWeight = out[1]-srcY;
				int nw, ne, sw, se;

				if ( srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
					// Easy case, all corners are in the image
					int i = srcWidth*srcY + srcX;
					nw = inPixels[i];
					ne = inPixels[i+1];
					sw = inPixels[i+srcWidth];
					se = inPixels[i+srcWidth+1];
				} else {
					// Some of the corners are off the image
					nw = getPixel( inPixels, srcX, srcY, srcWidth, srcHeight );
					ne = getPixel( inPixels, srcX+1, srcY, srcWidth, srcHeight );
					sw = getPixel( inPixels, srcX, srcY+1, srcWidth, srcHeight );
					se = getPixel( inPixels, srcX+1, srcY+1, srcWidth, srcHeight );
				}
				outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
			}
			setRGB( dst, 0, y, transformedSpace.width, 1, outPixels );
		}
		return dst;
	}

	final private int getPixel( int[] pixels, int x, int y, int width, int height ) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			switch (edgeAction) {
			case ConvolveFilter.ZERO_EDGES:
			default:
				return 0;
			case ConvolveFilter.WRAP_EDGES:
				return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
			case ConvolveFilter.CLAMP_EDGES:
				return pixels[(ImageMath.clamp(y, 0, height-1) * width) + ImageMath.clamp(x, 0, width-1)];
			}
		}
		return pixels[ y*width+x ];
	}

	protected BufferedImage filterPixelsNN( BufferedImage dst, int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int srcWidth = width;
		int srcHeight = height;
		int outWidth = transformedSpace.width;
		int outHeight = transformedSpace.height;
		int outX, outY, srcX, srcY;
		int[] outPixels = new int[outWidth];

		outX = transformedSpace.x;
		outY = transformedSpace.y;
		int[] rgb = new int[4];
		float[] out = new float[2];

		for (int y = 0; y < outHeight; y++) {
			for (int x = 0; x < outWidth; x++) {
				transformInverse(outX+x, outY+y, out);
				srcX = (int)out[0];
				srcY = (int)out[1];
				// int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
				if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
					int p;
					switch (edgeAction) {
					case ConvolveFilter.ZERO_EDGES:
					default:
						p = 0;
						break;
					case ConvolveFilter.WRAP_EDGES:
						p = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
						break;
					case ConvolveFilter.CLAMP_EDGES:
						p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight-1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth-1)];
						break;
					}
					outPixels[x] = p;
				} else {
					int i = srcWidth*srcY + srcX;
					rgb[0] = inPixels[i];
					outPixels[x] = inPixels[i];
				}
			}
			setRGB( dst, 0, y, transformedSpace.width, 1, outPixels );
		}
		return dst;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}