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

package lucee.runtime.img.filter;import java.awt.Point;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A class which warps an image using a field Warp algorithm.
 */
public class FieldWarpFilter extends TransformFilter  implements DynFiltering {

	public static class Line {
		public int x1, y1, x2, y2;
		public int dx, dy;
		public float length, lengthSquared;
		
		public Line(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		public void setup() {
			dx = x2-x1;
			dy = y2-y1;
			lengthSquared = dx*dx + dy*dy;
			length = (float)Math.sqrt(lengthSquared);
		}
	}

	private float amount = 1.0f;
	private float power = 1.0f;
	private float strength = 2.0f;
	private Line[] inLines;
	private Line[] outLines;
	private Line[] intermediateLines;
	private float width, height;

	public FieldWarpFilter() {
	}

	/**
	 * Set the amount of warp.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	/**
	 * Get the amount of warp.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}
	
	public void setPower(float power) {
		this.power = power;
	}
	
	public float getPower() {
		return power;
	}
	
	public void setStrength(float strength) {
		this.strength = strength;
	}
	
	public float getStrength() {
		return strength;
	}
	
	public void setInLines( Line[] inLines ) {
		this.inLines = inLines;
	}
	
	public Line[] getInLines() {
		return inLines;
	}
	
	public void setOutLines( Line[] outLines ) {
		this.outLines = outLines;
	}
	
	public Line[] getOutLines() {
		return outLines;
	}
	
	protected void transform(int x, int y, Point out) {
	}

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		float u = 0, v = 0;
		float fraction = 0;
		float distance;
		float fdist;
		float weight;
		float a = 0.001f;
		float b = 1.5f*strength + 0.5f;
		float p = power;

		float totalWeight = 0.0f;
		float sumX = 0.0f;
		float sumY = 0.0f;

		for (int line = 0; line < inLines.length; line++) {
			Line l1 = inLines[line];
			Line l = intermediateLines[line];
			float dx = x - l.x1;
			float dy = y - l.y1;

			fraction = (dx * l.dx + dy * l.dy) / l.lengthSquared;
			fdist = (dy * l.dx - dx * l.dy) / l.length;
			if (fraction <= 0)
				distance = (float)Math.sqrt(dx*dx + dy*dy);
			else if (fraction >= 1) {
				dx = x - l.x2;
				dy = y - l.y2;
				distance = (float)Math.sqrt(dx*dx + dy*dy);
			} else if (fdist >= 0)
				distance = fdist;
			else
				distance = -fdist;
			u = l1.x1 + fraction * l1.dx - fdist * l1.dy / l1.length;
			v = l1.y1 + fraction * l1.dy + fdist * l1.dx / l1.length;

			weight = (float)Math.pow(Math.pow(l.length, p) / (a + distance), b);

			sumX += (u - x) * weight;
			sumY += (v - y) * weight;
			totalWeight += weight;
		}

		out[0] = x + sumX / totalWeight + 0.5f;
		out[1] = y + sumY / totalWeight + 0.5f;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		this.width = width;
		this.height = height;
		if ( inLines != null && outLines != null ) {
			intermediateLines = new Line[inLines.length];
			for (int line = 0; line < inLines.length; line++) {
				Line l = intermediateLines[line] = new Line(
					ImageMath.lerp(amount, inLines[line].x1, outLines[line].x1),
					ImageMath.lerp(amount, inLines[line].y1, outLines[line].y1),
					ImageMath.lerp(amount, inLines[line].x2, outLines[line].x2),
					ImageMath.lerp(amount, inLines[line].y2, outLines[line].y2)
				);
				l.setup();
				inLines[line].setup();
			}
			dst = super.filter( src, dst );
			intermediateLines = null;
			return dst;
		}
		return src;
	}

	@Override
	public String toString() {
		return "Distort/Field Warp...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Power")))!=null)setPower(ImageFilterUtil.toFloatValue(o,"Power"));
		if((o=parameters.removeEL(KeyImpl.init("Strength")))!=null)setStrength(ImageFilterUtil.toFloatValue(o,"Strength"));
		if((o=parameters.removeEL(KeyImpl.init("InLines")))!=null)setInLines(ImageFilterUtil.toAFieldWarpFilter$Line(o,"InLines"));
		if((o=parameters.removeEL(KeyImpl.init("OutLines")))!=null)setOutLines(ImageFilterUtil.toAFieldWarpFilter$Line(o,"OutLines"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Power, Strength, InLines, OutLines, EdgeAction, Interpolation]");
		}

		return filter(src, dst);
	}
}