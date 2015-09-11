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

public class OffsetFilter extends TransformFilter  implements DynFiltering {

	private int width, height;
	private int xOffset, yOffset;
	private boolean wrap;

	public OffsetFilter() {
		this(0, 0, true);
	}

	public OffsetFilter(int xOffset, int yOffset, boolean wrap) {
		super(ConvolveFilter.ZERO_EDGES );
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.wrap = wrap;
	}

	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}
	
	public int getXOffset() {
		return xOffset;
	}
	
	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}
	
	public int getYOffset() {
		return yOffset;
	}
	
	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}
	
	public boolean getWrap() {
		return wrap;
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		if ( wrap ) {
			out[0] = (x+width-xOffset) % width;
			out[1] = (y+height-yOffset) % height;
		} else {
			out[0] = x-xOffset;
			out[1] = y-yOffset;
		}
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		this.width = src.getWidth();
		this.height = src.getHeight();
		if ( wrap ) {
			while (xOffset < 0)
				xOffset += width;
			while (yOffset < 0)
				yOffset += height;
			xOffset %= width;
			yOffset %= height;
		}
		return super.filter( src, dst );
	}

	@Override
	public String toString() {
		return "Distort/Offset...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("XOffset")))!=null)setXOffset(ImageFilterUtil.toIntValue(o,"XOffset"));
		if((o=parameters.removeEL(KeyImpl.init("YOffset")))!=null)setYOffset(ImageFilterUtil.toIntValue(o,"YOffset"));
		if((o=parameters.removeEL(KeyImpl.init("Wrap")))!=null)setWrap(ImageFilterUtil.toBooleanValue(o,"Wrap"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [XOffset, YOffset, Wrap, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}