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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which uses the alpha channel of a "mask" image to interpolate between a source and destination image.
 */
public class ApplyMaskFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private BufferedImage destination;
	private BufferedImage maskImage;

    /**
     * Construct an ApplyMaskFIlter.
     */
	public ApplyMaskFilter() {
	}

    /**
     * Construct an ApplyMaskFIlter.
     * @param maskImage the mask image
     * @param destination the destination image
     */
	public ApplyMaskFilter( BufferedImage maskImage, BufferedImage destination ) {
		this.maskImage = maskImage;
		this.destination = destination;
	}

    /**
     * Set the destination image.
     * @param destination the destination image
     * @see #getDestination
     */
	public void setDestination( BufferedImage destination ) {
		this.destination = destination;
	}
	
    /**
     * Get the destination image.
     * @return the destination image
     * @see #setDestination
     */
	public BufferedImage getDestination() {
		return destination;
	}
	
    /**
     * Set the mask image.
     * @param maskImage the mask image
     * @see #getMaskImage
     */
	public void setMaskImage( BufferedImage maskImage ) {
		this.maskImage = maskImage;
	}
	
    /**
     * Get the mask image.
     * @return the mask image
     * @see #setMaskImage
     */
	public BufferedImage getMaskImage() {
		return maskImage;
	}
		
    /**
     * Interpolates between two rasters according to the alpha level of a mask raster.
     * @param src the source raster
     * @param dst the destination raster
     * @param sel the mask raster
     */
	public static void composeThroughMask(Raster src, WritableRaster dst, Raster sel) {
		int x = src.getMinX();
		int y = src.getMinY();
		int w = src.getWidth();
		int h = src.getHeight();

		int srcRGB[] = null;
		int selRGB[] = null;
		int dstRGB[] = null;

		for ( int i = 0; i < h; i++ ) {
			srcRGB = src.getPixels(x, y, w, 1, srcRGB);
			selRGB = sel.getPixels(x, y, w, 1, selRGB);
			dstRGB = dst.getPixels(x, y, w, 1, dstRGB);

			int k = x;
			for ( int j = 0; j < w; j++ ) {
				int sr = srcRGB[k];
				int dir = dstRGB[k];
				int sg = srcRGB[k+1];
				int dig = dstRGB[k+1];
				int sb = srcRGB[k+2];
				int dib = dstRGB[k+2];
				int sa = srcRGB[k+3];
				int dia = dstRGB[k+3];

				float a = selRGB[k+3]/255f;
				float ac = 1-a;

				dstRGB[k] = (int)(a*sr + ac*dir); 
				dstRGB[k+1] = (int)(a*sg + ac*dig); 
				dstRGB[k+2] = (int)(a*sb + ac*dib); 
				dstRGB[k+3] = (int)(a*sa + ac*dia);
				k += 4;
			}

			dst.setPixels(x, y, w, 1, dstRGB);
			y++;
		}
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        //int width = src.getWidth();
        //int height = src.getHeight();
		//int type = src.getType();
		//WritableRaster srcRaster = 
    	src.getRaster();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		//WritableRaster dstRaster = 
        dst.getRaster();

        if ( destination != null && maskImage != null )
			composeThroughMask( src.getRaster(), dst.getRaster(), maskImage.getRaster() );

        return dst;
    }

	@Override
	public String toString() {
		return "Keying/Key...";
	}
	@Override
	public BufferedImage filter(BufferedImage src ,Struct parameters) throws PageException {
		BufferedImage dst=src;
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("MaskImage")))!=null)setMaskImage(ImageFilterUtil.toBufferedImage(o,"MaskImage"));
		if((o=parameters.removeEL(KeyImpl.init("destination")))!=null)setDestination(ImageFilterUtil.toBufferedImage(o,"destination"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [MaskImage]");
		}

		return filter(src, dst);
	}
}