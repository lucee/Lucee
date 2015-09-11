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
 * A filter to posterize an image.
 */
public class PosterizeFilter extends PointFilter  implements DynFiltering {

	private int numLevels;
	private int[] levels;
    private boolean initialized = false;

	public PosterizeFilter() {
		setNumLevels(6);
	}
	
	/**
     * Set the number of levels in the output image.
     * @param numLevels the number of levels
     * @see #getNumLevels
     */
    public void setNumLevels(int numLevels) {
		this.numLevels = numLevels;
		initialized = false;
	}

	/**
     * Get the number of levels in the output image.
     * @return the number of levels
     * @see #setNumLevels
     */
	public int getNumLevels() {
		return numLevels;
	}

	/**
     * Initialize the filter.
     */
    protected void initialize() {
		levels = new int[256];
		if (numLevels != 1)
			for (int i = 0; i < 256; i++)
				levels[i] = 255 * (numLevels*i / 256) / (numLevels-1);
	}
	
	@Override
	public int filterRGB(int x, int y, int rgb) {
		if (!initialized) {
			initialized = true;
			initialize();
		}
		int a = rgb & 0xff000000;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = levels[r];
		g = levels[g];
		b = levels[b];
		return a | (r << 16) | (g << 8) | b;
	}

	@Override
	public String toString() {
		return "Colors/Posterize...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("NumLevels")))!=null)setNumLevels(ImageFilterUtil.toIntValue(o,"NumLevels"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [NumLevels, Dimensions]");
		}

		return filter(src, dst);
	}
}