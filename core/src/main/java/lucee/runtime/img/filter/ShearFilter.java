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

public class ShearFilter extends TransformFilter  implements DynFiltering {

	private float xangle = 0.0f;
	private float yangle = 0.0f;
	private float shx = 0.0f;
	private float shy = 0.0f;
	private float xoffset = 0.0f;
	private float yoffset = 0.0f;
	private boolean resize = true;

	public ShearFilter() {
	}

	public void setResize(boolean resize) {
		this.resize = resize;
	}

	public boolean isResize() {
		return resize;
	}

	public void setXAngle(float xangle) {
		this.xangle = xangle;
		initialize();
	}

	public float getXAngle() {
		return xangle;
	}

	public void setYAngle(float yangle) {
		this.yangle = yangle;
		initialize();
	}

	public float getYAngle() {
		return yangle;
	}

	private void initialize() {
		shx = (float)Math.sin(xangle);
		shy = (float)Math.sin(yangle);
	}
	
	@Override
	protected void transformSpace(Rectangle r) {
		float tangent = (float)Math.tan(xangle);
		xoffset = -r.height * tangent;
		if (tangent < 0.0)
			tangent = -tangent;
		r.width = (int)(r.height * tangent + r.width + 0.999999f);
		tangent = (float)Math.tan(yangle);
		yoffset = -r.width * tangent;
		if (tangent < 0.0)
			tangent = -tangent;
		r.height = (int)(r.width * tangent + r.height + 0.999999f);
	}

/*
	public void imageComplete(int status) {
try {
		if (status == IMAGEERROR || status == IMAGEABORTED) {
			consumer.imageComplete(status);
			return;
		}

		int width = originalSpace.width;
		int height = originalSpace.height;

		float tangent = Math.tan(angle);
		if (tangent < 0.0)
			tangent = -tangent;
		int newWidth = (int)(height * tangent + width + 0.999999);
		int[] outPixels = new int[height*newWidth];
		int inIndex = 0;
		int yOffset = 0;
		for (int y = 0; y < height; y++) {
			float newCol;
			if (angle >= 0.0)
				newCol = y * tangent;
			else
				newCol = (height-y) * tangent;
			int iNewCol = (int)newCol;
			float f = newCol - iNewCol;
			f = 1.0 - f;

			int outIndex = yOffset+iNewCol;
			int lastRGB = inPixels[inIndex];
			for (int x = 0; x < width; x++) {
				int rgb = inPixels[inIndex];
				outPixels[outIndex] = ImageMath.mixColors(f, lastRGB, rgb);
				lastRGB = rgb;
				inIndex++;
				outIndex++;
			}
			outPixels[outIndex] = ImageMath.mixColors(f, lastRGB, 0);
			yOffset += newWidth;
		}
		consumer.setPixels(0, 0, newWidth, height, defaultRGBModel, outPixels, 0, newWidth);
		consumer.imageComplete(status);
		inPixels = null;
}
catch (Exception e) {
	
}
	}
*/
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		out[0] = x + xoffset + (y * shx);
		out[1] = y + yoffset + (x * shy);
	}

	@Override
	public String toString() {
		return "Distort/Shear...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Resize")))!=null)setResize(ImageFilterUtil.toBooleanValue(o,"Resize"));
		if((o=parameters.removeEL(KeyImpl.init("XAngle")))!=null)setXAngle(ImageFilterUtil.toFloatValue(o,"XAngle"));
		if((o=parameters.removeEL(KeyImpl.init("YAngle")))!=null)setYAngle(ImageFilterUtil.toFloatValue(o,"YAngle"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Resize, XAngle, YAngle, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}