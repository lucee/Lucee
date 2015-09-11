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
import lucee.runtime.img.math.Function2D;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public class MapFilter extends TransformFilter  implements DynFiltering {

	private Function2D xMapFunction;
	private Function2D yMapFunction;

	public MapFilter() {
	}
	
	public void setXMapFunction(Function2D xMapFunction) {
		this.xMapFunction = xMapFunction;
	}

	public Function2D getXMapFunction() {
		return xMapFunction;
	}

	public void setYMapFunction(Function2D yMapFunction) {
		this.yMapFunction = yMapFunction;
	}

	public Function2D getYMapFunction() {
		return yMapFunction;
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float xMap, yMap;
		xMap = xMapFunction.evaluate(x, y);
		yMap = yMapFunction.evaluate(x, y);
		out[0] = xMap * transformedSpace.width;
		out[1] = yMap * transformedSpace.height;
	}

	@Override
	public String toString() {
		return "Distort/Map Coordinates...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("XMapFunction")))!=null)setXMapFunction(ImageFilterUtil.toFunction2D(o,"XMapFunction"));
		if((o=parameters.removeEL(KeyImpl.init("YMapFunction")))!=null)setYMapFunction(ImageFilterUtil.toFunction2D(o,"YMapFunction"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [XMapFunction, YMapFunction, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}