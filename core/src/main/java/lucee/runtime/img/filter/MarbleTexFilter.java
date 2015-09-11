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
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public class MarbleTexFilter extends PointFilter  implements DynFiltering {

	private float scale = 32;
	private float stretch = 1.0f;
	private float angle = 0.0f;
	private float turbulence = 1;
	private float turbulenceFactor = 0.5f;
	private Colormap colormap;
	private float m00 = 1.0f;
	private float m01 = 0.0f;
	private float m10 = 0.0f;
	private float m11 = 1.0f;

	public MarbleTexFilter() {
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getScale() {
		return scale;
	}

	public void setStretch(float stretch) {
		this.stretch = stretch;
	}

	public float getStretch() {
		return stretch;
	}

	public void setAngle(float angle) {
		this.angle = angle;
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		m00 = cos;
		m01 = sin;
		m10 = -sin;
		m11 = cos;
	}

	public float getAngle() {
		return angle;
	}

	public void setTurbulence(float turbulence) {
		this.turbulence = turbulence;
	}

	public float getTurbulence() {
		return turbulence;
	}

	public void setTurbulenceFactor(float turbulenceFactor) {
		this.turbulenceFactor = turbulenceFactor;
	}

	public float getTurbulenceFactor() {
		return turbulenceFactor;
	}

	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}
	
	public Colormap getColormap() {
		return colormap;
	}
	
	@Override
	public int filterRGB(int x, int y, int rgb) {
		float nx = m00*x + m01*y;
		float ny = m10*x + m11*y;
		nx /= scale * stretch;
		ny /= scale;

		//int a = rgb & 0xff000000;
		if (colormap != null) {
//			float f = Noise.turbulence2(nx, ny, turbulence);
//			f = 3*turbulenceFactor*f+ny;
//			f = Math.sin(f*Math.PI);
			float chaos = turbulenceFactor*Noise.turbulence2(nx, ny, turbulence);
//			float f = Math.sin(Math.sin(8.*chaos + 7*nx +3.*ny));
			float f = 3*turbulenceFactor*chaos+ny;
			f = (float)Math.sin(f*Math.PI);
			float perturb = (float)Math.sin(40.*chaos);
			f += .2 * perturb;
			return colormap.getColor(f);
		} 
		float red, grn, blu;
		float chaos, brownLayer, greenLayer;
		float perturb, brownPerturb, greenPerturb, grnPerturb;
		float t;

		chaos = turbulenceFactor*Noise.turbulence2(nx, ny, turbulence);
		t = (float)Math.sin(Math.sin(8.*chaos + 7*nx +3.*ny));

		greenLayer = brownLayer = Math.abs(t);

		perturb = (float)Math.sin(40.*chaos);
		perturb = Math.abs(perturb);

		brownPerturb = .6f*perturb + 0.3f;
		greenPerturb = .2f*perturb + 0.8f;
		grnPerturb = .15f*perturb + 0.85f;
		grn = 0.5f * (float)Math.pow(Math.abs(brownLayer), 0.3);
		brownLayer = (float)Math.pow(0.5 * (brownLayer+1.0), 0.6) * brownPerturb;
		greenLayer = (float)Math.pow(0.5 * (greenLayer+1.0), 0.6) * greenPerturb;

		red = (0.5f*brownLayer + 0.35f*greenLayer)*2.0f*grn;
		blu = (0.25f*brownLayer + 0.35f*greenLayer)*2.0f*grn;
		grn *= Math.max(brownLayer, greenLayer) * grnPerturb;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = PixelUtils.clamp((int)(r*red));
		g = PixelUtils.clamp((int)(g*grn));
		b = PixelUtils.clamp((int)(b*blu));
		return (rgb & 0xff000000) | (r<<16) | (g<<8) | b;
	}

	@Override
	public String toString() {
		return "Texture/Marble Texture...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Turbulence")))!=null)setTurbulence(ImageFilterUtil.toFloatValue(o,"Turbulence"));
		if((o=parameters.removeEL(KeyImpl.init("Stretch")))!=null)setStretch(ImageFilterUtil.toFloatValue(o,"Stretch"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("TurbulenceFactor")))!=null)setTurbulenceFactor(ImageFilterUtil.toFloatValue(o,"TurbulenceFactor"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Turbulence, Stretch, Angle, TurbulenceFactor, Scale, Dimensions]");
		}

		return filter(src, dst);
	}
}