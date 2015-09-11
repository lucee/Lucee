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

package lucee.runtime.img.filter;import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which uses another filter to perform a transition.
 * e.g. to create a blur transition, you could write: new TransitionFilter( new BoxBlurFilter(), "radius", 0, 100 );
 */
public class TransitionFilter extends AbstractBufferedImageOp  implements DynFiltering {
	
	private float transition = 0;
	private BufferedImage destination;
    private String property;
    private Method method;

    /**
     * The filter used for the transition.
     */
    protected BufferedImageOp filter;

    /**
     * The start value for the filter property.
     */
    protected float minValue;

    /**
     * The end value for the filter property.
     */
    protected float maxValue;


    /**
     * Construct a TransitionFilter.
     * @param filter the filter to use
     * @param property the filter property which is changed over the transition
     * @param minValue the start value for the filter property
     * @param maxValue the end value for the filter property
     */
	public TransitionFilter( BufferedImageOp filter, String property, float minValue, float maxValue ) {
		this.filter = filter;
		this.property = property;
		this.minValue = minValue;
		this.maxValue = maxValue;
		try {
			BeanInfo info = Introspector.getBeanInfo( filter.getClass() );
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for ( int i = 0; i < pds.length; i++ ) {
                PropertyDescriptor pd = pds[i];
                if ( property.equals( pd.getName() ) ) {
                    method = pd.getWriteMethod();
                    break;
                }
            }
            if ( method == null )
                throw new IllegalArgumentException( "No such property in object: "+property );
		}
		catch (IntrospectionException e) {
            throw new IllegalArgumentException( e.toString() );
		}
	}

	/**
	 * Set the transition of the image in the range 0..1.
	 * @param transition the transition
     * @min-value 0
     * @max-value 1
     * @see #getTransition
	 */
	public void setTransition( float transition ) {
		this.transition = transition;
	}
	
	/**
	 * Get the transition of the image.
	 * @return the transition
     * @see #setTransition
	 */
	public float getTransition() {
		return transition;
	}
	
    /**
     * Set the destination image.
     * @param destination the destination image
     * @see #getDestination
     */
	public void setDestination( BufferedImage destination ) {
		this.destination = destination;
	}
	
    /**
     * Get the destination image.
     * @return the destination image
     * @see #setDestination
     */
	public BufferedImage getDestination() {
		return destination;
	}
	
/*
	public void setFilter( BufferedImageOp filter ) {
		this.filter = filter;
	}
	
	public int getFilter() {
		return filter;
	}
*/
	
    /**
     * Prepare the filter for the transiton at a given time.
     * The default implementation sets the given filter property, but you could override this method to make other changes.
     * @param transition the transition time in the range 0 - 1
     */
	public void prepareFilter( float transition ) {
        try {
            method.invoke( filter, new Object[] { new Float( transition ) } );
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException("Error setting value for property: "+property);
        }
	}
	
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		if ( destination == null )
			return dst;

		float itransition = 1-transition;

		Graphics2D g = dst.createGraphics();
		if ( transition != 1 ) {
            float t = minValue + transition * ( maxValue-minValue );
			prepareFilter( t );
            g.drawImage( src, filter, 0, 0 );
		}
		if ( transition != 0 ) {
            g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, transition ) );
            float t = minValue + itransition * ( maxValue-minValue );
			prepareFilter( t );
            g.drawImage( destination, filter, 0, 0 );
		}
		g.dispose();

        return dst;
    }

	@Override
	public String toString() {
		return "Transitions/Transition...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Transition")))!=null)setTransition(ImageFilterUtil.toFloatValue(o,"Transition"));
		if((o=parameters.removeEL(KeyImpl.init("destination")))!=null)setDestination(ImageFilterUtil.toBufferedImage(o,"destination"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Transition]");
		}

		return filter(src, dst);
	}
}