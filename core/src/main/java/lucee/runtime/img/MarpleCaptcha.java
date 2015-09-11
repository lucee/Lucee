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
package lucee.runtime.img;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import lucee.commons.img.AbstractCaptcha;
import lucee.commons.img.CaptchaException;
import lucee.commons.lang.font.FontUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.img.filter.MarbleFilter;

public class MarpleCaptcha extends AbstractCaptcha {

	public static final int DIFFICULTY_LOW=0;
	public static final int DIFFICULTY_MEDIUM=1;
	public static final int DIFFICULTY_HIGH=2;

	@Override
	public BufferedImage generate(String text,int width, int height, String[] fonts, boolean useAntiAlias, Color fontColor,int fontSize, int difficulty) throws CaptchaException {
		MarbleFilter mf = new MarbleFilter();
		try {
			mf.setEdgeAction("WRAP");
		} catch (ExpressionException e1) {}
		mf.setAmount(0.1F);
		BufferedImage src=super.generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, difficulty);
		
		if(difficulty==DIFFICULTY_LOW) mf.setTurbulence(0.0f);
		else if(difficulty==DIFFICULTY_MEDIUM) mf.setTurbulence(0.10f);
		else mf.setTurbulence(0.2f);
		
		try {
			mf.setInterpolation("NEAREST_NEIGHBOUR");
		} catch (ExpressionException e) {}
		
		BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		mf.filter(src, dst);
		return dst;
	}
	
	@Override
	public Font getFont(String font, Font defaultValue) {
		return FontUtil.getFont(font,defaultValue);
	}
}