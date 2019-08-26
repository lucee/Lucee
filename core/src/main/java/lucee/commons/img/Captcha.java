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

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * concrete captcha implementation
 */
public final class Captcha extends AbstractCaptcha {

	private static final char[] chars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
			'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
			// ,'0','1','2','3','4','5','6','7','8','9'
	};

	@Override
	public Font getFont(String font, Font defaultValue) {
		return Font.decode(font);
	}

	/**
	 * write out image object to an output stream
	 * 
	 * @param image
	 * @param os
	 * @param format
	 * @throws IOException
	 */
	public static void writeOut(BufferedImage image, OutputStream os, String format) throws IOException {
		ImageIO.write(image, format, os);

	}

	/**
	 * creates a random String in given length
	 * 
	 * @param length length of the string to create
	 * @return
	 */
	public static String randomString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(chars[AbstractCaptcha.rnd(0, chars.length - 1)]);
		}
		return sb.toString();
	}
}