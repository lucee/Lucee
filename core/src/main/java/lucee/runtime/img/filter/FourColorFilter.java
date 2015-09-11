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
 * A filter which draws a gradient interpolated between four colors defined at the corners of the image.
 */
public class FourColorFilter extends PointFilter  implements DynFiltering {
	
	private int width;
	private int height;
	private int colorNW;
	private int colorNE;
	private int colorSW;
	private int colorSE;
	private int rNW, gNW, bNW;
	private int rNE, gNE, bNE;
	private int rSW, gSW, bSW;
	private int rSE, gSE, bSE;

	public FourColorFilter() {
		setColorNW( 0xffff0000 );
		setColorNE( 0xffff00ff );
		setColorSW( 0xff0000ff );
		setColorSE( 0xff00ffff );
	}

	public void setColorNW( int color ) {
		this.colorNW = color;
		rNW = (color >> 16) & 0xff;
		gNW = (color >> 8) & 0xff;
		bNW = color & 0xff;
	}

	public int getColorNW() {
		return colorNW;
	}

	public void setColorNE( int color ) {
		this.colorNE = color;
		rNE = (color >> 16) & 0xff;
		gNE = (color >> 8) & 0xff;
		bNE = color & 0xff;
	}

	public int getColorNE() {
		return colorNE;
	}

	public void setColorSW( int color ) {
		this.colorSW = color;
		rSW = (color >> 16) & 0xff;
		gSW = (color >> 8) & 0xff;
		bSW = color & 0xff;
	}

	public int getColorSW() {
		return colorSW;
	}

	public void setColorSE( int color ) {
		this.colorSE = color;
		rSE = (color >> 16) & 0xff;
		gSE = (color >> 8) & 0xff;
		bSE = color & 0xff;
	}

	public int getColorSE() {
		return colorSE;
	}

	@Override
	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
		super.setDimensions(width, height);
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		float fx = (float)x / width;
		float fy = (float)y / height;
		float p, q;

		p = rNW + (rNE - rNW) * fx;
		q = rSW + (rSE - rSW) * fx;
		int r = (int)( p + (q - p) * fy + 0.5f );

		p = gNW + (gNE - gNW) * fx;
		q = gSW + (gSE - gSW) * fx;
		int g = (int)( p + (q - p) * fy + 0.5f );

		p = bNW + (bNE - bNW) * fx;
		q = bSW + (bSE - bSW) * fx;
		int b = (int)( p + (q - p) * fy + 0.5f );

		return 0xff000000 | (r << 16) | (g << 8) | b;
	}
	
	@Override
	public String toString() {
		return "Texture/Four Color Fill...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("ColorNW")))!=null)setColorNW(ImageFilterUtil.toIntValue(o,"ColorNW"));
		if((o=parameters.removeEL(KeyImpl.init("ColorNE")))!=null)setColorNE(ImageFilterUtil.toIntValue(o,"ColorNE"));
		if((o=parameters.removeEL(KeyImpl.init("ColorSW")))!=null)setColorSW(ImageFilterUtil.toIntValue(o,"ColorSW"));
		if((o=parameters.removeEL(KeyImpl.init("ColorSE")))!=null)setColorSE(ImageFilterUtil.toIntValue(o,"ColorSE"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [ColorNW, ColorNE, ColorSW, ColorSE, Dimensions]");
		}

		return filter(src, dst);
	}
}