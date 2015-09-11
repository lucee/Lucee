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

package lucee.runtime.img.filter;import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

public class ImageCombiningFilter {

	public int filterRGB(int x, int y, int rgb1, int rgb2) {
		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		//int g1 = (rgb1 >> 8) & 0xff;
		//int b1 = rgb1 & 0xff;
		//int a2 = (rgb2 >> 24) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		//int g2 = (rgb2 >> 8) & 0xff;
		//int b2 = rgb2 & 0xff;
		int r = PixelUtils.clamp(r1 + r2);
		int g = PixelUtils.clamp(r1 + r2);
		int b = PixelUtils.clamp(r1 + r2);
		return (a1 << 24) | (r << 16) | (g << 8) | b;
	}

	public ImageProducer filter(Image image1, Image image2, int x, int y, int w, int h) {
		int[] pixels1 = new int[w * h];
		int[] pixels2 = new int[w * h];
		int[] pixels3 = new int[w * h];
		PixelGrabber pg1 = new PixelGrabber(image1, x, y, w, h, pixels1, 0, w);
		PixelGrabber pg2 = new PixelGrabber(image2, x, y, w, h, pixels2, 0, w);
		try {
			pg1.grabPixels();
			pg2.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("interrupted waiting for pixels!");
			return null;
		}
		if ((pg1.status() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return null;
		}
		if ((pg2.status() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return null;
		}

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int k = j * w + i;
				pixels3[k] = filterRGB(x+i, y+j, pixels1[k], pixels2[k]);
			}
		}
		return new MemoryImageSource(w, h, pixels3, 0, w);
	}
}