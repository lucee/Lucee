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

package lucee.runtime.img.filter;import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public class ScratchFilter extends AbstractBufferedImageOp  implements DynFiltering {
    private float density = 0.1f;
    private float angle;
    private float angleVariation = 1.0f;
    private float width = 0.5f;
    private float length = 0.5f;
    private int color = 0xffffffff;
    private int seed = 0;

    public ScratchFilter() {
	}
	
	public void setAngle( float angle ) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}
	
	public void setAngleVariation( float angleVariation ) {
		this.angleVariation = angleVariation;
	}

	public float getAngleVariation() {
		return angleVariation;
	}
	
	public void setDensity( float density ) {
		this.density = density;
	}

	public float getDensity() {
		return density;
	}
	
	public void setLength( float length ) {
		this.length = length;
	}

	public float getLength() {
		return length;
	}
	
	public void setWidth( float width ) {
		this.width = width;
	}

	public float getWidth() {
		return width;
	}
	
	public void setColor( int color ) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setSeed( int seed ) {
		this.seed = seed;
	}

	public int getSeed() {
		return seed;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int width = src.getWidth();
        int height = src.getHeight();
        int numScratches = (int)(density * width * height / 100);
ArrayList lines = new ArrayList();
{
        float l = length * width;
        Random random = new Random( seed );
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g.setColor( new Color( color ) );
        g.setStroke( new BasicStroke( this.width ) );
        for ( int i = 0; i < numScratches; i++ ) {
            float x = width * random.nextFloat();
            float y = height * random.nextFloat();
            float a = angle + ImageMath.TWO_PI * (angleVariation * (random.nextFloat() - 0.5f));
            float s = (float)Math.sin( a ) * l;
            float c = (float)Math.cos( a ) * l;
            float x1 = x-c;
            float y1 = y-s;
            float x2 = x+c;
            float y2 = y+s;
            g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
lines.add( new Line2D.Float( x1, y1, x2, y2 ) );
        }
        g.dispose();
}
        
if ( false ) {
//		int[] inPixels = getRGB( src, 0, 0, width, height, null );
		int[] inPixels = new int[width*height];
        int index = 0;
        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                float sx = x, sy = y;
                for ( int i = 0; i < numScratches; i++ ) {
                    Line2D.Float l = (Line2D.Float)lines.get( i );
                    float dot = (l.x2-l.x1)*(sx-l.x1) + (l.y2-l.y1)*(sy-l.y1);
                    if ( dot > 0 )
                        inPixels[index] |= (1 << i );
                }
                index++;
            }
        }

        Colormap colormap = new LinearColormap();
        index = 0;
        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                float f = (float)(inPixels[index] & 0x7fffffff) / 0x7fffffff;
                inPixels[index] = colormap.getColor( f );
                index++;
            }
        }
		setRGB( dst, 0, 0, width, height, inPixels );
}
        return dst;
    }
    
	@Override
	public String toString() {
		return "Render/Scratches...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=src;//ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Density")))!=null)setDensity(ImageFilterUtil.toFloatValue(o,"Density"));
		if((o=parameters.removeEL(KeyImpl.init("AngleVariation")))!=null)setAngleVariation(ImageFilterUtil.toFloatValue(o,"AngleVariation"));
		if((o=parameters.removeEL(KeyImpl.init("Length")))!=null)setLength(ImageFilterUtil.toFloatValue(o,"Length"));
		if((o=parameters.removeEL(KeyImpl.init("Seed")))!=null)setSeed(ImageFilterUtil.toIntValue(o,"Seed"));
		if((o=parameters.removeEL(KeyImpl.init("Color")))!=null)setColor(ImageFilterUtil.toColorRGB(o,"Color"));
		if((o=parameters.removeEL(KeyImpl.init("Width")))!=null)setWidth(ImageFilterUtil.toFloatValue(o,"Width"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Angle, Density, AngleVariation, Length, Seed, Color, Width]");
		}

		return filter(src, dst);
	}
}