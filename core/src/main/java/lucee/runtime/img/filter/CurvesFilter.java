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



public class CurvesFilter extends TransferFilter  implements DynFiltering {

	private Curve[] curves = new Curve[1];
	
    public static class Curve {
        public float[] x;
        public float[] y;
        
        public Curve() {
            x = new float[] { 0, 1 };
            y = new float[] { 0, 1 };
        }
        
        public Curve( Curve curve ) {
            x = curve.x.clone();
            y = curve.y.clone();
        }
        
        public int addKnot( float kx, float ky ) {
            int pos = -1;
            int numKnots = x.length;
            float[] nx = new float[numKnots+1];
            float[] ny = new float[numKnots+1];
            int j = 0;
            for ( int i = 0; i < numKnots; i++ ) {
                if ( pos == -1 && x[i] > kx ) {
                    pos = j;
                    nx[j] = kx;
                    ny[j] = ky;
                    j++;
                }
                nx[j] = x[i];
                ny[j] = y[i];
                j++;
            }
            if ( pos == -1 ) {
                pos = j;
                nx[j] = kx;
                ny[j] = ky;
            }
            x = nx;
            y = ny;
            return pos;
        }
        
        public void removeKnot( int n ) {
            int numKnots = x.length;
            if ( numKnots <= 2 )
                return;
            float[] nx = new float[numKnots-1];
            float[] ny = new float[numKnots-1];
            int j = 0;
            for ( int i = 0; i < numKnots-1; i++ ) {
                if ( i == n )
                    j++;
                nx[i] = x[j];
                ny[i] = y[j];
                j++;
            }
            x = nx;
            y = ny;
            for ( int i = 0; i < x.length; i++ )
                System.out.println( i+": "+x[i]+" "+y[i]);
        }

        private void sortKnots() {
            int numKnots = x.length;
            for (int i = 1; i < numKnots-1; i++) {
                for (int j = 1; j < i; j++) {
                    if (x[i] < x[j]) {
                        float t = x[i];
                        x[i] = x[j];
                        x[j] = t;
                        t = y[i];
                        y[i] = y[j];
                        y[j] = t;
                    }
                }
            }
        }

        protected int[] makeTable() {
            int numKnots = x.length;
            float[] nx = new float[numKnots+2];
            float[] ny = new float[numKnots+2];
            System.arraycopy( x, 0, nx, 1, numKnots);
            System.arraycopy( y, 0, ny, 1, numKnots);
            nx[0] = nx[1];
            ny[0] = ny[1];
            nx[numKnots+1] = nx[numKnots];
            ny[numKnots+1] = ny[numKnots];

            int[] table = new int[256];
            for (int i = 0; i < 1024; i++) {
                float f = i/1024.0f;
                int x = (int)(255 * ImageMath.spline( f, nx.length, nx ) + 0.5f);
                int y = (int)(255 * ImageMath.spline( f, nx.length, ny ) + 0.5f);
                x = ImageMath.clamp( x, 0, 255 );
                y = ImageMath.clamp( y, 0, 255 );
                table[x] = y;
            }
//          System.out.println();
//          for ( int i = 0; i < 256; i++ )
//              System.out.println( i+": "+table[i]);
            return table;
        }
    }
    
    public CurvesFilter() {
        curves = new Curve[3];
        curves[0] = new Curve();
        curves[1] = new Curve();
        curves[2] = new Curve();
    }
    
	@Override
	protected void initialize() {
		initialized = true;
		if ( curves.length == 1 )
            rTable = gTable = bTable = curves[0].makeTable();
        else {
            rTable = curves[0].makeTable();
            gTable = curves[1].makeTable();
            bTable = curves[2].makeTable();
        }
	}

	public void setCurve( Curve curve ) {
        curves = new Curve[] { curve };
		initialized = false;
	}
	
	public void setCurves( Curve[] curves ) {
		if ( curves == null || (curves.length != 1 && curves.length != 3) )
            throw new IllegalArgumentException( "Curves must be length 1 or 3" );
        this.curves = curves;
		initialized = false;
	}
	
	public Curve[] getCurves() {
		return curves;
	}

	@Override
	public String toString() {
		return "Colors/Curves...";
	}

	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Curves")))!=null)setCurves(ImageFilterUtil.toACurvesFilter$Curve(o,"Curves"));
		if((o=parameters.removeEL(KeyImpl.init("Curve")))!=null)setCurve(ImageFilterUtil.toCurvesFilter$Curve(o,"Curve"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Curves, Curve, Dimensions]");
		}

		return filter(src, dst);
	}
}