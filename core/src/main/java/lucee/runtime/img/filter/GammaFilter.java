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
 * A filter for changing the gamma of an image.
 */
public class GammaFilter extends TransferFilter  implements DynFiltering {

	private float rGamma, gGamma, bGamma;

    /**
     * Construct a GammaFilter.
     */
	public GammaFilter() {
		this(1.0f);
	}

    /**
     * Construct a GammaFilter.
     * @param gamma the gamma level for all RGB channels
     */
	public GammaFilter(float gamma) {
		this(gamma, gamma, gamma);
	}

    /**
     * Construct a GammaFilter.
     * @param rGamma the gamma level for the red channel
     * @param gGamma the gamma level for the blue channel
     * @param bGamma the gamma level for the green channel
     */
	public GammaFilter(float rGamma, float gGamma, float bGamma) {
		setGamma(rGamma, gGamma, bGamma);
	}

    /**
     * Set the gamma levels.
     * @param rGamma the gamma level for the red channel
     * @param gGamma the gamma level for the blue channel
     * @param bGamma the gamma level for the green channel
     * @see #getGamma
     */
	public void setGamma(float rGamma, float gGamma, float bGamma) {
		this.rGamma = rGamma;
		this.gGamma = gGamma;
		this.bGamma = bGamma;
		initialized = false;
	}

    /**
     * Set the gamma level.
     * @param gamma the gamma level for all RGB channels
     * @see #getGamma
     */
	public void setGamma(float gamma) {
		setGamma(gamma, gamma, gamma);
	}
	
    /**
     * Get the gamma level.
     * @return the gamma level for all RGB channels
     * @see #setGamma
     */
	public float getGamma() {
		return rGamma;
	}
	
    @Override
	protected void initialize() {
		rTable = makeTable(rGamma);

		if (gGamma == rGamma)
			gTable = rTable;
		else
			gTable = makeTable(gGamma);

		if (bGamma == rGamma)
			bTable = rTable;
		else if (bGamma == gGamma)
			bTable = gTable;
		else
			bTable = makeTable(bGamma);
	}

	private int[] makeTable(float gamma) {
		int[] table = new int[256];
		for (int i = 0; i < 256; i++) {
			int v = (int) ((255.0 * Math.pow(i/255.0, 1.0 / gamma)) + 0.5);
			if (v > 255)
				v = 255;
			table[i] = v;
		}
		return table;
	}

	@Override
	public String toString() {
		return "Colors/Gamma...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Gamma")))!=null)setGamma(ImageFilterUtil.toFloatValue(o,"Gamma"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Gamma, Gamma, Dimensions]");
		}

		return filter(src, dst);
	}
}