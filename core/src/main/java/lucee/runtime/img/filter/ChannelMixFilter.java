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
 * A filter which allows the red, green and blue channels of an image to be mixed into each other.
 */
public class ChannelMixFilter extends PointFilter  implements DynFiltering {
	
	private int blueGreen, redBlue, greenRed;
	private int intoR, intoG, intoB;
	
	public ChannelMixFilter() {
		canFilterIndexColorModel = true;
	}

	public void setBlueGreen(int blueGreen) {
		this.blueGreen = blueGreen;
	}

	public int getBlueGreen() {
		return blueGreen;
	}

	public void setRedBlue(int redBlue) {
		this.redBlue = redBlue;
	}

	public int getRedBlue() {
		return redBlue;
	}

	public void setGreenRed(int greenRed) {
		this.greenRed = greenRed;
	}

	public int getGreenRed() {
		return greenRed;
	}

	public void setIntoR(int intoR) {
		this.intoR = intoR;
	}

	public int getIntoR() {
		return intoR;
	}

	public void setIntoG(int intoG) {
		this.intoG = intoG;
	}

	public int getIntoG() {
		return intoG;
	}

	public void setIntoB(int intoB) {
		this.intoB = intoB;
	}

	public int getIntoB() {
		return intoB;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		int nr = PixelUtils.clamp((intoR * (blueGreen*g+(255-blueGreen)*b)/255 + (255-intoR)*r)/255);
		int ng = PixelUtils.clamp((intoG * (redBlue*b+(255-redBlue)*r)/255 + (255-intoG)*g)/255);
		int nb = PixelUtils.clamp((intoB * (greenRed*r+(255-greenRed)*g)/255 + (255-intoB)*b)/255);
		return a | (nr << 16) | (ng << 8) | nb;
	}

	@Override
	public String toString() {
		return "Colors/Mix Channels...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("BlueGreen")))!=null)setBlueGreen(ImageFilterUtil.toIntValue(o,"BlueGreen"));
		if((o=parameters.removeEL(KeyImpl.init("RedBlue")))!=null)setRedBlue(ImageFilterUtil.toIntValue(o,"RedBlue"));
		if((o=parameters.removeEL(KeyImpl.init("GreenRed")))!=null)setGreenRed(ImageFilterUtil.toIntValue(o,"GreenRed"));
		if((o=parameters.removeEL(KeyImpl.init("IntoR")))!=null)setIntoR(ImageFilterUtil.toIntValue(o,"IntoR"));
		if((o=parameters.removeEL(KeyImpl.init("IntoG")))!=null)setIntoG(ImageFilterUtil.toIntValue(o,"IntoG"));
		if((o=parameters.removeEL(KeyImpl.init("IntoB")))!=null)setIntoB(ImageFilterUtil.toIntValue(o,"IntoB"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [BlueGreen, RedBlue, GreenRed, IntoR, IntoG, IntoB, Dimensions]");
		}

		return filter(src, dst);
	}
}