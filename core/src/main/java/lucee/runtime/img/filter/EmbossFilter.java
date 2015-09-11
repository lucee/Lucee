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
 * A class to emboss an image.
 */
public class EmbossFilter extends WholeImageFilter  implements DynFiltering {

	private final static float pixelScale = 255.9f;

	private float azimuth = 135.0f * ImageMath.PI / 180.0f, elevation = 30.0f * ImageMath.PI / 180f;
	private boolean emboss = false;
	private float width45 = 3.0f;

	public EmbossFilter() {
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}
	
	public float getAzimuth() {
		return azimuth;
	}
	
	public void setElevation(float elevation) {
		this.elevation = elevation;
	}
	
	public float getElevation() {
		return elevation;
	}
	
	public void setBumpHeight(float bumpHeight) {
		this.width45 = 3 * bumpHeight;
	}

	public float getBumpHeight() {
		return width45 / 3;
	}

	public void setEmboss(boolean emboss) {
		this.emboss = emboss;
	}
	
	public boolean getEmboss() {
		return emboss;
	}
	
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int index = 0;
		int[] outPixels = new int[width * height];

		int[] bumpPixels;
		int bumpMapWidth, bumpMapHeight;
		
		bumpMapWidth = width;
		bumpMapHeight = height;
		bumpPixels = new int[bumpMapWidth * bumpMapHeight];
		for (int i = 0; i < inPixels.length; i++)
			bumpPixels[i] = PixelUtils.brightness(inPixels[i]);

		int Nx, Ny, Nz, Lx, Ly, Lz, Nz2, NzLz, NdotL;
		int shade, background;

		Lx = (int)(Math.cos(azimuth) * Math.cos(elevation) * pixelScale);
		Ly = (int)(Math.sin(azimuth) * Math.cos(elevation) * pixelScale);
		Lz = (int)(Math.sin(elevation) * pixelScale);

		Nz = (int)(6 * 255 / width45);
		Nz2 = Nz * Nz;
		NzLz = Nz * Lz;

		background = Lz;

		int bumpIndex = 0;
		
		for (int y = 0; y < height; y++, bumpIndex += bumpMapWidth) {
			int s1 = bumpIndex;
			int s2 = s1 + bumpMapWidth;
			int s3 = s2 + bumpMapWidth;

			for (int x = 0; x < width; x++, s1++, s2++, s3++) {
				if (y != 0 && y < height-2 && x != 0 && x < width-2) {
					Nx = bumpPixels[s1-1] + bumpPixels[s2-1] + bumpPixels[s3-1] - bumpPixels[s1+1] - bumpPixels[s2+1] - bumpPixels[s3+1];
					Ny = bumpPixels[s3-1] + bumpPixels[s3] + bumpPixels[s3+1] - bumpPixels[s1-1] - bumpPixels[s1] - bumpPixels[s1+1];

					if (Nx == 0 && Ny == 0)
						shade = background;
					else if ((NdotL = Nx*Lx + Ny*Ly + NzLz) < 0)
						shade = 0;
					else
						shade = (int)(NdotL / Math.sqrt(Nx*Nx + Ny*Ny + Nz2));
				} else
					shade = background;

				if (emboss) {
					int rgb = inPixels[index];
					int a = rgb & 0xff000000;
					int r = (rgb >> 16) & 0xff;
					int g = (rgb >> 8) & 0xff;
					int b = rgb & 0xff;
					r = (r*shade) >> 8;
					g = (g*shade) >> 8;
					b = (b*shade) >> 8;
					outPixels[index++] = a | (r << 16) | (g << 8) | b;
				} else
					outPixels[index++] = 0xff000000 | (shade << 16) | (shade << 8) | shade;
			}
		}

		return outPixels;
	}

	@Override
	public String toString() {
		return "Stylize/Emboss...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("BumpHeight")))!=null)setBumpHeight(ImageFilterUtil.toFloatValue(o,"BumpHeight"));
		if((o=parameters.removeEL(KeyImpl.init("Azimuth")))!=null)setAzimuth(ImageFilterUtil.toFloatValue(o,"Azimuth"));
		if((o=parameters.removeEL(KeyImpl.init("Elevation")))!=null)setElevation(ImageFilterUtil.toFloatValue(o,"Elevation"));
		if((o=parameters.removeEL(KeyImpl.init("Emboss")))!=null)setEmboss(ImageFilterUtil.toBooleanValue(o,"Emboss"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [BumpHeight, Azimuth, Elevation, Emboss]");
		}

		return filter(src, dst);
	}
}