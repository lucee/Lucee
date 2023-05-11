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
package lucee.commons.img;

import java.util.concurrent.ThreadLocalRandom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract template class for captcha generation
 */
public abstract class AbstractCaptcha {

	public static final int DIFFICULTY_LOW = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HIGH = 2;

	/**
	 * generates a Captcha as a Buffered Image file
	 * 
	 * @param text text for the captcha
	 * @param width width of the resulting image
	 * @param height height of the resulting image
	 * @param fonts list of font used for the captcha (all font are random used)
	 * @param useAntiAlias use anti aliasing or not
	 * @param fontColor color of the font
	 * @param fontSize size of the font
	 * @param difficulty difficulty of the reslting captcha
	 * @return captcha image
	 * @throws CaptchaException
	 */
	public BufferedImage generate(String text, int width, int height, String[] fonts, boolean useAntiAlias, Color fontColor, int fontSize, int difficulty) throws CaptchaException {
		if (difficulty == DIFFICULTY_LOW) {
			return generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 0, 0, 0, 0, 0, 0, 230, 25);
		}
		if (difficulty == DIFFICULTY_MEDIUM) {
			return generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 0, 0, 5, 30, 0, 0, 200, 35);
		}
		return generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 4, 10, 30, 60, 4, 10, 170, 45);
	}

	private BufferedImage generate(String text, int width, int height, String[] fonts, boolean useAntiAlias, Color fontColor, int fontSize, int minOvals, int maxOvals,
			int minBGLines, int maxBGLines, int minFGLines, int maxFGLines, int startColor, int shear) throws CaptchaException {

		if (text == null || text.trim().length() == 0) throw new CaptchaException("missing Text");

		char[] characters = text.toCharArray();
		int top = height / 3;

		Dimension dimension = new Dimension(width, height);
		int imageType = BufferedImage.TYPE_INT_RGB;
		BufferedImage bufferedImage = new BufferedImage((int) dimension.getWidth(), (int) dimension.getHeight(), imageType);
		Graphics2D graphics = bufferedImage.createGraphics();

		// Set anti-alias setting
		if (useAntiAlias) graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawBackground(graphics, dimension, startColor);

		// draw ovals
		if (maxOvals > 0 && maxOvals > minOvals) {
			int to = rnd(minOvals, maxOvals);
			for (int i = 1; i <= to; i++) {
				drawRandomOval(graphics, dimension, getRandomColor(startColor));
			}
		}

		// Draw background lines
		if (maxBGLines > 0 && maxBGLines > minBGLines) {
			int to = rnd(minBGLines, maxBGLines);
			for (int i = 1; i <= to; i++) {
				drawRandomLine(graphics, dimension, getRandomColor(startColor));
			}
		}

		if (fonts == null || fonts.length == 0) throw new CaptchaException("no font's defined");

		// font
		Font f;
		ArrayList<Font> fontList = new ArrayList<Font>();
		for (int i = 0; i < fonts.length; i++) {
			f = getFont(fonts[i], null);
			if (f != null) fontList.add(f);

		}
		if (fonts.length == 0) throw new CaptchaException("defined fonts are not available on this system");

		int charWidth = 0;
		int charHeight = 0, tmp;
		int space = 0;
		Font[] _fonts = new Font[characters.length];
		for (int i = 0; i < characters.length; i++) {
			char c = characters[i];
			_fonts[i] = createFont(fontList, fontSize, shear, i);
			graphics.setFont(_fonts[i]);
			charWidth += graphics.getFontMetrics().charWidth(c);
			tmp = graphics.getFontMetrics().getHeight();
			if (tmp > charHeight) charHeight = tmp;
		}
		if (charWidth < width) {
			space = (width - charWidth) / (characters.length + 1);
		}
		else if (charWidth > width) throw new CaptchaException("the specified width for the CAPTCHA image is not big enough to fit the text. Minimum width is [" + charWidth + "]");
		if (charHeight > height) throw new CaptchaException("the specified height for the CAPTCHA image is not big enough to fit the text. Minimum height is [" + charHeight + "]");
		int left = space;

		// Draw captcha text
		for (int i = 0; i < characters.length; i++) {
			char c = characters[i];
			// <cfset staticCollections.shuffle(definedFonts) />

			graphics.setFont(_fonts[i]);
			graphics.setColor(fontColor);
			// Check if font can display current character --->
			/*
			 * <cfloop condition="NOT graphics.getFont().canDisplay(char)"> <cfset setFont(graphics,
			 * definedFonts) /> </cfloop>
			 */

			// Compute the top character position --->
			top = rnd(graphics.getFontMetrics().getAscent(), height - (height - graphics.getFontMetrics().getHeight()) / 2);

			// Draw character text
			graphics.drawString(String.valueOf(c), left, top);

			// Compute the next character lef tposition --->
			// ((rnd(150, 200) / 100) *
			left += graphics.getFontMetrics().charWidth(c) + rnd(space, space);
		}

		// Draw forground lines
		if (maxFGLines > 0 && maxFGLines > minFGLines) {
			int to = rnd(minFGLines, maxFGLines);
			for (int i = 1; i <= to; i++) {
				drawRandomLine(graphics, dimension, getRandomColor(startColor));
			}
		}

		return bufferedImage;
	}

	/**
	 * creates a font from given string
	 * 
	 * @param font
	 * @param defaultValue
	 * @return
	 */
	public abstract Font getFont(String font, Font defaultValue);

	private void drawBackground(Graphics2D graphics, Dimension dimension, int _startColor) {
		Color startColor = getRandomColor(_startColor);
		Color endColor = getRandomColor(_startColor);
		GradientPaint gradientPaint = new GradientPaint(getRandomPointOnBorder(dimension), startColor, getRandomPointOnBorder(dimension), endColor.brighter(), true);
		graphics.setPaint(gradientPaint);
		// arguments.graphics.setColor(startColor) />

		graphics.fill(new Rectangle(dimension));
	}

	private Font createFont(List<Font> fonts, int fontSize, int shear, int index) {
		AffineTransform trans1 = getRandomTransformation(shear, shear);
		AffineTransform trans2 = getRandomTransformation(shear, shear);
		Font font = fonts.get(index % fonts.size());
		font = font.deriveFont((float) fontSize).deriveFont(trans1).deriveFont(trans2);
		return font;
	}

	private Color getRandomColor(int startColor) {
		return new Color(r(startColor), r(startColor), r(startColor));
	}

	private int r(int startColor) {
		return rnd(startColor - 100, startColor);
	}

	private Point getRandomPointOnBorder(Dimension dimension) {
		int height = (int) dimension.getHeight();
		int width = (int) dimension.getWidth();

		switch (rnd(1, 4)) {
		case 1: // left side
			return new Point(0, rnd(0, height));
		case 2: // right side
			return new Point(width, rnd(0, height));
		case 3: // top side
			return new Point(rnd(0, width), 0);
		case 4:
		default: // bottom side
			return new Point(rnd(0, width), height);
		}
	}

	private AffineTransform getRandomTransformation(int shearXRange, int shearYRange) {
		// create a slightly random affine transform
		double shearX = rndd(-1 * (shearXRange * (rnd(50, 150) / 100d)), (shearXRange * (rndd(50, 150) / 100d))) / 100d;
		double shearY = rndd(-1 * (shearYRange * (rnd(50, 150) / 100d)), (shearYRange * (rndd(50, 150) / 100d))) / 100d;

		AffineTransform transformation = new AffineTransform();
		transformation.shear(shearX, shearY);
		return transformation;
	}

	private BasicStroke getRandomStroke() {
		return new BasicStroke(rnd(1, 3));
	}

	private Point getRandomPoint(Dimension dimension) {
		int height = (int) dimension.getHeight();
		int width = (int) dimension.getWidth();
		return new Point(rnd(0, width), rnd(0, height));
	}

	protected static int rnd(double min, double max) {
		return (int) rndd(min, max);
	}

	private static double rndd(double min, double max) {
		if (min > max) {
			double tmp = min;
			min = max;
			max = tmp;
		}
		return (ThreadLocalRandom.current().doubles(1, min, max)).toArray()[0];
	}

	private void drawRandomLine(Graphics2D graphics, Dimension dimension, Color lineColorType) {
		Point point1 = getRandomPointOnBorder(dimension);
		Point point2 = getRandomPointOnBorder(dimension);

		graphics.setStroke(getRandomStroke());
		graphics.setColor(lineColorType);
		graphics.drawLine((int) point1.getX(), (int) point1.getY(), (int) point2.getX(), (int) point2.getY());
	}

	private void drawRandomOval(Graphics2D graphics, Dimension dimension, Color ovalColorType) {
		Point point = getRandomPoint(dimension);
		double height = dimension.getHeight();
		// double width = dimension.getWidth() ;
		double minOval = height * .10;
		double maxOval = height * .75;

		graphics.setColor(ovalColorType);

		switch (rnd(1, 3)) {
		case 1:
			graphics.setStroke(getRandomStroke());
			graphics.drawOval((int) point.getX(), (int) point.getY(), rnd(minOval, maxOval), rnd(minOval, maxOval));
			break;
		case 2:
		case 3:
			graphics.fillOval((int) point.getX(), (int) point.getY(), rnd(minOval, maxOval), rnd(minOval, maxOval));
			break;
		}
	}
}