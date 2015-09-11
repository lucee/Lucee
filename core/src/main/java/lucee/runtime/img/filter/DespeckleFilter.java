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

package lucee.runtime.img.filter;import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which removes noise from an image using a "pepper and salt" algorithm.
 */
public class DespeckleFilter extends WholeImageFilter  implements DynFiltering {

	public DespeckleFilter() {
	}

	private short pepperAndSalt( short c, short v1, short v2 ) {
		if ( c < v1 )
			c++;
		if ( c < v2 )
			c++;
		if ( c > v1 )
			c--;
		if ( c > v2 )
			c--;
		return c;
	}
	
	@Override
	protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
		int index = 0;
		short[][] r = new short[3][width];
		short[][] g = new short[3][width];
		short[][] b = new short[3][width];
		int[] outPixels = new int[width * height];

		for (int x = 0; x < width; x++) {
			int rgb = inPixels[x];
			r[1][x] = (short)((rgb >> 16) & 0xff);
			g[1][x] = (short)((rgb >> 8) & 0xff);
			b[1][x] = (short)(rgb & 0xff);
		}
		for (int y = 0; y < height; y++) {
			boolean yIn = y > 0 && y < height-1;
			int nextRowIndex = index+width;
			if ( y < height-1) {
				for (int x = 0; x < width; x++) {
					int rgb = inPixels[nextRowIndex++];
					r[2][x] = (short)((rgb >> 16) & 0xff);
					g[2][x] = (short)((rgb >> 8) & 0xff);
					b[2][x] = (short)(rgb & 0xff);
				}
			}
			for (int x = 0; x < width; x++) {
				boolean xIn = x > 0 && x < width-1;
				short or = r[1][x];
				short og = g[1][x];
				short ob = b[1][x];
				int w = x-1;
				int e = x+1;
				
				if ( yIn ) {
					or = pepperAndSalt( or, r[0][x], r[2][x] );
					og = pepperAndSalt( og, g[0][x], g[2][x] );
					ob = pepperAndSalt( ob, b[0][x], b[2][x] );
				}

				if ( xIn ) {
					or = pepperAndSalt( or, r[1][w], r[1][e] );
					og = pepperAndSalt( og, g[1][w], g[1][e] );
					ob = pepperAndSalt( ob, b[1][w], b[1][e] );
				}

				if ( yIn && xIn ) {
					or = pepperAndSalt( or, r[0][w], r[2][e] );
					og = pepperAndSalt( og, g[0][w], g[2][e] );
					ob = pepperAndSalt( ob, b[0][w], b[2][e] );

					or = pepperAndSalt( or, r[2][w], r[0][e] );
					og = pepperAndSalt( og, g[2][w], g[0][e] );
					ob = pepperAndSalt( ob, b[2][w], b[0][e] );
				}

				outPixels[index] = (inPixels[index] & 0xff000000) | (or << 16) | (og << 8) | ob;
				index++;
			}
			short[] t;
			t = r[0];
			r[0] = r[1];
			r[1] = r[2];
			r[2] = t;
			t = g[0];
			g[0] = g[1];
			g[1] = g[2];
			g[2] = t;
			t = b[0];
			b[0] = b[1];
			b[1] = b[2];
			b[2] = t;
		}
	
		return outPixels;
	}

	@Override
	public String toString() {
		return "Blur/Despeckle...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		//Object o;

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported []");
		}

		return filter(src, dst);
	}
}