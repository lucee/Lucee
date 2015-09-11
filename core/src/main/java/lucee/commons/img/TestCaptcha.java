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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestCaptcha {
	
	public static void main(String[] args) throws IOException {
		Captcha captcha=new Captcha();
		
		// generate captcha image
		BufferedImage image = captcha.generate(
				Captcha.randomString(10),	// Text
				450,					// width
				70, 					// height
				new String[]{"arial","courier new"},	// fonts 
				true, 					// use anti alias
				Color.BLACK, 			// font color
				45, 					// font size
				AbstractCaptcha.DIFFICULTY_HIGH	// difficulty
			);

		// write out captcha image as a png file
		FileOutputStream fos = new FileOutputStream(new File("/Users/mic/temp/captcha.png"));
		Captcha.writeOut(image, fos, "png");

		// write out captcha image as a jpg file
		fos = new FileOutputStream(new File("/Users/mic/temp/captcha.jpg"));
		Captcha.writeOut(image, fos, "jpg");
		
	}
}