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

package lucee.runtime.img.filter;import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;
/**
 * A filter which composites two images together with an optional transform.
 */
public class CompositeFilter extends AbstractBufferedImageOp  implements DynFiltering {

	private Composite composite;
    private AffineTransform transform;
	
	/**
     * Construct a CompositeFilter.
     */
    public CompositeFilter() {
	}
	
	/**
     * Construct a CompositeFilter.
     * @param composite the composite to use
     */
	public CompositeFilter( Composite composite ) {
		this.composite = composite;
	}
	
	/**
     * Construct a CompositeFilter.
     * @param composite the composite to use
     * @param transform a transform for the composited image
     */
	public CompositeFilter( Composite composite, AffineTransform transform ) {
		this.composite = composite;
		this.transform = transform;
	}
	
	/**
     * Set the composite.
     * @param composite the composite to use
     * @see #getComposite
     */
	public void setComposite( Composite composite ) {
		this.composite = composite;
	}
    
	/**
     * Get the composite.
     * @return the composite to use
     * @see #setComposite
     */
    public Composite getComposite() {
        return composite;
    }
	
	/**
     * Set the transform.
     * @param transform the transform to use
     * @see #getTransform
     */
	public void setTransform( AffineTransform transform ) {
		this.transform = transform;
	}
    
	/**
     * Get the transform.
     * @return the transform to use
     * @see #setTransform
     */
    public AffineTransform getTransform() {
        return transform;
    }
	
	@Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

		Graphics2D g = dst.createGraphics();
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.setComposite( composite );
        g.drawRenderedImage( src, transform );
        g.dispose();
		return dst;
	}

	@Override
	public String toString() {
		return "Composite";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Transform")))!=null)setTransform(ImageFilterUtil.toAffineTransform(o,"Transform"));
		if((o=parameters.removeEL(KeyImpl.init("Composite")))!=null)setComposite(ImageFilterUtil.toComposite(o,"Composite"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Transform, Composite]");
		}

		return filter(src, dst);
	}
}