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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
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
 * A filter which renders text onto an image.
 */
public class RenderTextFilter extends AbstractBufferedImageOp  implements DynFiltering {

	private String text;
	private Font font;
    private Paint paint;
	private Composite composite;
    private AffineTransform transform;
	
	/**
     * Construct a RenderTextFilter.
     */
    public RenderTextFilter() {
	}
	
	/**
     * Construct a RenderTextFilter.
     * @param text the text
     * @param font the font to use (may be null)
     * @param paint the paint (may be null)
     * @param composite the composite (may be null)
     * @param transform the transform (may be null)
     */
	public RenderTextFilter( String text, Font font, Paint paint, Composite composite, AffineTransform transform ) {
		this.text = text;
		this.font = font;
		this.composite = composite;
		this.paint = paint;
		this.transform = transform;
	}
	
	/**
     * Set the text to paint.
     * @param text the text
     * @see #getText
     */
	public void setText( String text ) {
		this.text = text;
	}
    
	/**
     * Get the text to paint.
     * @return the text
     * @see #setText
     */
    public String getText() {
        return text;
    }
	
	/**
     * Set the composite with which to paint the text.
     * @param composite the composite
     * @see #getComposite
     */
	public void setComposite( Composite composite ) {
		this.composite = composite;
	}
    
	/**
     * Get the composite with which to paint the text.
     * @return the composite
     * @see #setComposite
     */
    public Composite getComposite() {
        return composite;
    }
	
	/**
     * Set the paint with which to paint the text.
     * @param paint the paint
     * @see #getPaint
     */
	public void setPaint( Paint paint ) {
		this.paint = paint;
	}
    
	/**
     * Get the paint with which to paint the text.
     * @return the paint
     * @see #setPaint
     */
    public Paint getPaint() {
        return paint;
    }
	
	/**
     * Set the font with which to paint the text.
     * @param font the font
     * @see #getFont
     */
	public void setFont( Font font ) {
		this.font = font;
	}
    
	/**
     * Get the font with which to paint the text.
     * @return the font
     * @see #setFont
     */
    public Font getFont() {
        return font;
    }
	
	/**
     * Set the transform with which to paint the text.
     * @param transform the transform
     * @see #getTransform
     */
	public void setTransform( AffineTransform transform ) {
		this.transform = transform;
	}
    
	/**
     * Get the transform with which to paint the text.
     * @return the transform
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
        g.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        if ( font != null )
            g.setFont( font );
        if ( transform != null )
            g.setTransform( transform );
        if ( composite != null )
            g.setComposite( composite );
        if ( paint != null )
            g.setPaint( paint );
        if ( text != null )
            g.drawString( text, 10, 100 );
        g.dispose();
		return dst;
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Font")))!=null)setFont(ImageFilterUtil.toFont(o,"Font"));
		if((o=parameters.removeEL(KeyImpl.init("Transform")))!=null)setTransform(ImageFilterUtil.toAffineTransform(o,"Transform"));
		if((o=parameters.removeEL(KeyImpl.init("Composite")))!=null)setComposite(ImageFilterUtil.toComposite(o,"Composite"));
		if((o=parameters.removeEL(KeyImpl.init("Paint")))!=null)setPaint(ImageFilterUtil.toColor(o,"Paint"));
		if((o=parameters.removeEL(KeyImpl.init("Text")))!=null)setText(ImageFilterUtil.toString(o,"Text"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Font, Transform, Composite, Paint, Text]");
		}

		return filter(src, dst);
	}
}