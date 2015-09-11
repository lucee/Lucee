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
 * A filter to change the brightness and contrast of an image.
 */
public class ContrastFilter extends TransferFilter  implements DynFiltering {

	private float brightness = 1.0f;
	private float contrast = 1.0f;
	
	@Override
	protected float transferFunction( float f ) {
		f = f*brightness;
		f = (f-0.5f)*contrast+0.5f;
		return f;
	}

    /**
     * Set the filter brightness.
     * @param brightness the brightness in the range 0 to 1
     * @min-value 0
     * @max-value 0
     * @see #getBrightness
     */
	public void setBrightness(float brightness) {
		this.brightness = brightness;
		initialized = false;
	}
	
    /**
     * Get the filter brightness.
     * @return the brightness in the range 0 to 1
     * @see #setBrightness
     */
	public float getBrightness() {
		return brightness;
	}

    /**
     * Set the filter contrast.
     * @param contrast the contrast in the range 0 to 1
     * @min-value 0
     * @max-value 0
     * @see #getContrast
     */
	public void setContrast(float contrast) {
		this.contrast = contrast;
		initialized = false;
	}
	
    /**
     * Get the filter contrast.
     * @return the contrast in the range 0 to 1
     * @see #setContrast
     */
	public float getContrast() {
		return contrast;
	}

	@Override
	public String toString() {
		return "Colors/Contrast...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Brightness")))!=null)setBrightness(ImageFilterUtil.toFloatValue(o,"Brightness"));
		if((o=parameters.removeEL(KeyImpl.init("Contrast")))!=null)setContrast(ImageFilterUtil.toFloatValue(o,"Contrast"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Brightness, Contrast, Dimensions]");
		}

		return filter(src, dst);
	}
}