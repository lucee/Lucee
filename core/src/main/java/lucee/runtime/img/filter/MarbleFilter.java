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
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * This filter applies a marbling effect to an image, displacing pixels by random amounts.
 */
public class MarbleFilter extends TransformFilter  implements DynFiltering {

	private float[] sinTable, cosTable;
	private float xScale = 4;
	private float yScale = 4;
	private float amount = 1;
	private float turbulence = 1;
	
	public MarbleFilter() {
		super(ConvolveFilter.CLAMP_EDGES);
	}
	
	/**
     * Set the X scale of the effect.
     * @param xScale the scale.
     * @see #getXScale
     */
	public void setXScale(float xScale) {
		this.xScale = xScale;
	}

	/**
     * Get the X scale of the effect.
     * @return the scale.
     * @see #setXScale
     */
	public float getXScale() {
		return xScale;
	}

	/**
     * Set the Y scale of the effect.
     * @param yScale the scale.
     * @see #getYScale
     */
	public void setYScale(float yScale) {
		this.yScale = yScale;
	}

	/**
     * Get the Y scale of the effect.
     * @return the scale.
     * @see #setYScale
     */
	public float getYScale() {
		return yScale;
	}

	/**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * Get the amount of effect.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}

	/**
     * Specifies the turbulence of the effect.
     * @param turbulence the turbulence of the effect.
     * @min-value 0
     * @max-value 1
     * @see #getTurbulence
     */
	public void setTurbulence(float turbulence) {
		this.turbulence = turbulence;
	}

	/**
     * Returns the turbulence of the effect.
     * @return the turbulence of the effect.
     * @see #setTurbulence
     */
	public float getTurbulence() {
		return turbulence;
	}

	private void initialize() {
		sinTable = new float[256];
		cosTable = new float[256];
		for (int i = 0; i < 256; i++) {
			float angle = ImageMath.TWO_PI*i/256f*turbulence;
			sinTable[i] = (float)(-yScale*Math.sin(angle));
			cosTable[i] = (float)(yScale*Math.cos(angle));
		}
	}

	private int displacementMap(int x, int y) {
		return PixelUtils.clamp((int)(127 * (1+Noise.noise2(x / xScale, y / xScale))));
	}
	
	@Override
	protected void transformInverse(int x, int y, float[] out) {
		int displacement = displacementMap(x, y);
		out[0] = x + sinTable[displacement];
		out[1] = y + cosTable[displacement];
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		initialize();
		return super.filter( src, dst );
	}

	@Override
	public String toString() {
		return "Distort/Marble...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=null;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Turbulence")))!=null)setTurbulence(ImageFilterUtil.toFloatValue(o,"Turbulence"));
		if((o=parameters.removeEL(KeyImpl.init("XScale")))!=null)setXScale(ImageFilterUtil.toFloatValue(o,"XScale"));
		if((o=parameters.removeEL(KeyImpl.init("YScale")))!=null)setYScale(ImageFilterUtil.toFloatValue(o,"YScale"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Turbulence, XScale, YScale, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}