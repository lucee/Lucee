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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which rotates an image. These days this is easier done with Java2D, but this filter remains.
 */
public class RotateFilter extends TransformFilter  implements DynFiltering {
	
	private float angle;
	private float cos, sin;
	private boolean resize = true;

	/**
     * Construct a RotateFilter.
     */
    public RotateFilter() {
		this(ImageMath.PI);
	}

	/**
     * Construct a RotateFilter.
     * @param angle the angle to rotate
     */
	public RotateFilter(float angle) {
		this(angle, true);
	}

	/**
     * Construct a RotateFilter.
     * @param angle the angle to rotate
     * @param resize true if the output image should be resized
     */
	public RotateFilter(float angle, boolean resize) {
		setAngle(angle);
		this.resize = resize;
	}

	/**
     * Specifies the angle of rotation.
     * @param angle the angle of rotation.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
		cos = (float)Math.cos(this.angle);
		sin = (float)Math.sin(this.angle);
	}

	/**
     * Returns the angle of rotation.
     * @return the angle of rotation.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	@Override
	protected void transformSpace(Rectangle rect) {
		if (resize) {
			Point out = new Point(0, 0);
			int minx = Integer.MAX_VALUE;
			int miny = Integer.MAX_VALUE;
			int maxx = Integer.MIN_VALUE;
			int maxy = Integer.MIN_VALUE;
			int w = rect.width;
			int h = rect.height;
			int x = rect.x;
			int y = rect.y;

			for (int i = 0; i < 4; i++)  {
				switch (i) {
				case 0: transform(x, y, out); break;
				case 1: transform(x + w, y, out); break;
				case 2: transform(x, y + h, out); break;
				case 3: transform(x + w, y + h, out); break;
				}
				minx = Math.min(minx, out.x);
				miny = Math.min(miny, out.y);
				maxx = Math.max(maxx, out.x);
				maxy = Math.max(maxy, out.y);
			}

			rect.x = minx;
			rect.y = miny;
			rect.width = maxx - minx;
			rect.height = maxy - miny;
		}
	}
	
	

	private void transform(int x, int y, Point out) {
		out.x = (int)((x * cos) + (y * sin));
		out.y = (int)((y * cos) - (x * sin));
	}

	@Override
	protected void transformInverse(int x, int y, float[] out) {
		out[0] = (x * cos) - (y * sin);
		out[1] = (y * cos) + (x * sin);
	}

	@Override
	public String toString() {
		return "Rotate "+(int)(angle * 180 / Math.PI);
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {
		//BufferedImage dst=ImageUtil.createBufferedImage(src,src.getWidth()+400,src.getHeight()+400);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("EdgeAction")))!=null)setEdgeAction(ImageFilterUtil.toString(o,"EdgeAction"));
		if((o=parameters.removeEL(KeyImpl.init("Interpolation")))!=null)setInterpolation(ImageFilterUtil.toString(o,"Interpolation"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Angle, EdgeAction, Interpolation]");
		}
		
		

		//Rectangle rect = new Rectangle(0, 0, src.getWidth(), src.getHeight());
		//transformSpace(rect);
		BufferedImage dst=null;//ImageUtil.createBufferedImage(src,rect.width,rect.height);
		
		return filter(src, dst);
	}
}