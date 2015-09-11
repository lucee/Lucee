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



public class WeaveFilter extends PointFilter  implements DynFiltering {

	private float xWidth = 16;
	private float yWidth = 16;
	private float xGap = 6;
	private float yGap = 6;
	private int rows = 4;
	private int cols = 4;
	private int rgbX = 0xffff8080;
	private int rgbY = 0xff8080ff;
	private boolean useImageColors = true;
	private boolean roundThreads = false;
	private boolean shadeCrossings = true;

	public int[][] matrix = {
		{ 0, 1, 0, 1 },
		{ 1, 0, 1, 0 },
		{ 0, 1, 0, 1 },
		{ 1, 0, 1, 0 },
	};
	
	public WeaveFilter() {
	}
	
	public void setXGap(float xGap) {
		this.xGap = xGap;
	}

	public void setXWidth(float xWidth) {
		this.xWidth = xWidth;
	}

	public float getXWidth() {
		return xWidth;
	}

	public void setYWidth(float yWidth) {
		this.yWidth = yWidth;
	}

	public float getYWidth() {
		return yWidth;
	}

	public float getXGap() {
		return xGap;
	}

	public void setYGap(float yGap) {
		this.yGap = yGap;
	}

	public float getYGap() {
		return yGap;
	}

	public void setCrossings(int[][] matrix) {
		this.matrix = matrix;
	}
	
	public int[][] getCrossings() {
		return matrix;
	}
	
	public void setUseImageColors(boolean useImageColors) {
		this.useImageColors = useImageColors;
	}

	public boolean getUseImageColors() {
		return useImageColors;
	}

	public void setRoundThreads(boolean roundThreads) {
		this.roundThreads = roundThreads;
	}

	public boolean getRoundThreads() {
		return roundThreads;
	}

	public void setShadeCrossings(boolean shadeCrossings) {
		this.shadeCrossings = shadeCrossings;
	}

	public boolean getShadeCrossings() {
		return shadeCrossings;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		x += xWidth+xGap/2;
		y += yWidth+yGap/2;
		float nx = ImageMath.mod(x, xWidth+xGap);
		float ny = ImageMath.mod(y, yWidth+yGap);
		int ix = (int)(x / (xWidth+xGap));
		int iy = (int)(y / (yWidth+yGap));
		boolean inX = nx < xWidth;
		boolean inY = ny < yWidth;
		float dX, dY;
		float cX, cY;
		int lrgbX, lrgbY;

		if (roundThreads) {
			dX = Math.abs(xWidth/2-nx) / xWidth / 2;
			dY = Math.abs(yWidth/2-ny) / yWidth / 2;
		} else {
			dX = dY = 0;
		}

		if (shadeCrossings) {
			cX = ImageMath.smoothStep(xWidth/2, xWidth/2+xGap, Math.abs(xWidth/2-nx));
			cY = ImageMath.smoothStep(yWidth/2, yWidth/2+yGap, Math.abs(yWidth/2-ny));
		} else {
			cX = cY = 0;
		}

		if (useImageColors) {
			lrgbX = lrgbY = rgb;
		} else {
			lrgbX = rgbX;
			lrgbY = rgbY;
		}
		int v;
		int ixc = ix % cols;
		int iyr = iy % rows;
		int m = matrix[iyr][ixc];
		if (inX) {
			if (inY) {
				v = m == 1 ? lrgbX : lrgbY;
				v = ImageMath.mixColors(2 * (m == 1 ? dX : dY), v, 0xff000000);
			} else {
				if (shadeCrossings) {
					if (m != matrix[(iy+1) % rows][ixc]) {
						if (m == 0)
							cY = 1-cY;
						cY *= 0.5f;
						lrgbX = ImageMath.mixColors(cY, lrgbX, 0xff000000);
					} else if (m == 0)
						lrgbX = ImageMath.mixColors(0.5f, lrgbX, 0xff000000);
				}
				v = ImageMath.mixColors(2 * dX, lrgbX, 0xff000000);
			}
		} else if (inY) {
			if (shadeCrossings) {
				if (m != matrix[iyr][(ix+1) % cols]) {
					if (m == 1)
						cX = 1-cX;
					cX *= 0.5f;
					lrgbY = ImageMath.mixColors(cX, lrgbY, 0xff000000);
				} else if (m == 1)
					lrgbY = ImageMath.mixColors(0.5f, lrgbY, 0xff000000);
			}
			v = ImageMath.mixColors(2 * dY, lrgbY, 0xff000000);
		} else
			v = 0x00000000;
		return v;
	}

	@Override
	public String toString() {
		return "Texture/Weave...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("UseImageColors")))!=null)setUseImageColors(ImageFilterUtil.toBooleanValue(o,"UseImageColors"));
		if((o=parameters.removeEL(KeyImpl.init("XGap")))!=null)setXGap(ImageFilterUtil.toFloatValue(o,"XGap"));
		if((o=parameters.removeEL(KeyImpl.init("XWidth")))!=null)setXWidth(ImageFilterUtil.toFloatValue(o,"XWidth"));
		if((o=parameters.removeEL(KeyImpl.init("YWidth")))!=null)setYWidth(ImageFilterUtil.toFloatValue(o,"YWidth"));
		if((o=parameters.removeEL(KeyImpl.init("YGap")))!=null)setYGap(ImageFilterUtil.toFloatValue(o,"YGap"));
		if((o=parameters.removeEL(KeyImpl.init("Crossings")))!=null)setCrossings(ImageFilterUtil.toAAInt(o,"Crossings"));
		if((o=parameters.removeEL(KeyImpl.init("RoundThreads")))!=null)setRoundThreads(ImageFilterUtil.toBooleanValue(o,"RoundThreads"));
		if((o=parameters.removeEL(KeyImpl.init("ShadeCrossings")))!=null)setShadeCrossings(ImageFilterUtil.toBooleanValue(o,"ShadeCrossings"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [UseImageColors, XGap, XWidth, YWidth, YGap, Crossings, RoundThreads, ShadeCrossings, Dimensions]");
		}

		return filter(src, dst);
	}
}