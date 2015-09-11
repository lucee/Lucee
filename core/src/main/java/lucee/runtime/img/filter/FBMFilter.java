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
import java.util.Random;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.math.CellularFunction2D;
import lucee.runtime.img.math.FBM;
import lucee.runtime.img.math.Function2D;
import lucee.runtime.img.math.Noise;
import lucee.runtime.img.math.RidgedFBM;
import lucee.runtime.img.math.SCNoise;
import lucee.runtime.img.math.VLNoise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which produces textures from fractal Brownian motion.
 */
public class FBMFilter extends PointFilter implements Cloneable, DynFiltering {

	public final static int NOISE = 0;
	public final static int RIDGED = 1;
	public final static int VLNOISE = 2;
	public final static int SCNOISE = 3;
	public final static int CELLULAR = 4;

	private float scale = 32;
	private float stretch = 1.0f;
	private float angle = 0.0f;
	private float amount = 1.0f;
	private float H = 1.0f;
	private float octaves = 4.0f;
	private float lacunarity = 2.0f;
	private float gain = 0.5f;
	private float bias = 0.5f;
	private int operation;
	private float m00 = 1.0f;
	private float m01 = 0.0f;
	private float m10 = 0.0f;
	private float m11 = 1.0f;
	private float min;
	private float max;
	private Colormap colormap = new Gradient();
	//private boolean ridged;
	private FBM fBm;
	protected Random random = new Random();
	private int basisType = NOISE;
	private Function2D basis;

	public FBMFilter() {
		setBasisType(NOISE);
	}

	/**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * Get the amount of texture.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}
	
	public int getOperation() {
		return operation;
	}
	
	/**
     * Specifies the scale of the texture.
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 300+
     * @see #getScale
     */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
     * Returns the scale of the texture.
     * @return the scale of the texture.
     * @see #setScale
     */
	public float getScale() {
		return scale;
	}

	/**
     * Specifies the stretch factor of the texture.
     * @param stretch the stretch factor of the texture.
     * @min-value 1
     * @max-value 50+
     * @see #getStretch
     */
	public void setStretch(float stretch) {
		this.stretch = stretch;
	}

	/**
     * Returns the stretch factor of the texture.
     * @return the stretch factor of the texture.
     * @see #setStretch
     */
	public float getStretch() {
		return stretch;
	}

	/**
     * Specifies the angle of the texture.
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
	public void setAngle(float angle) {
		this.angle = angle;
		float cos = (float)Math.cos(this.angle);
		float sin = (float)Math.sin(this.angle);
		m00 = cos;
		m01 = sin;
		m10 = -sin;
		m11 = cos;
	}

	/**
     * Returns the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
	public float getAngle() {
		return angle;
	}

	public void setOctaves(float octaves) {
		this.octaves = octaves;
	}

	public float getOctaves() {
		return octaves;
	}

	public void setH(float H) {
		this.H = H;
	}

	public float getH() {
		return H;
	}

	public void setLacunarity(float lacunarity) {
		this.lacunarity = lacunarity;
	}

	public float getLacunarity() {
		return lacunarity;
	}

	public void setGain(float gain) {
		this.gain = gain;
	}

	public float getGain() {
		return gain;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public float getBias() {
		return bias;
	}

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
	public void setColormap(Colormap colormap) {
		this.colormap = colormap;
	}
	
    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
	public Colormap getColormap() {
		return colormap;
	}
	
	public void setBasisType(int basisType) {
		this.basisType = basisType;
		switch (basisType) {
		default:
		case NOISE:
			basis = new Noise();
			break;
		case RIDGED:
			basis = new RidgedFBM();
			break;
		case VLNOISE:
			basis = new VLNoise();
			break;
		case SCNOISE:
			basis = new SCNoise();
			break;
		case CELLULAR:
			basis = new CellularFunction2D();
			break;
		}
	}

	public int getBasisType() {
		return basisType;
	}

	public void setBasis(Function2D basis) {
		this.basis = basis;
	}

	public Function2D getBasis() {
		return basis;
	}

	protected FBM makeFBM(float H, float lacunarity, float octaves) {
		FBM fbm = new FBM(H, lacunarity, octaves, basis);
		float[] minmax = Noise.findRange(fbm, null);
		min = minmax[0];
		max = minmax[1];
		return fbm;
	}
	
	@Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		fBm = makeFBM(H, lacunarity, octaves);
		return super.filter( src, dst );
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		float nx = m00*x + m01*y;
		float ny = m10*x + m11*y;
		nx /= scale;
		ny /= scale * stretch;
		float f = fBm.evaluate(nx, ny);
		// Normalize to 0..1
		f = (f-min)/(max-min);
		f = ImageMath.gain(f, gain);
		f = ImageMath.bias(f, bias);
		f *= amount;
		int a = rgb & 0xff000000;
		int v;
		if (colormap != null)
			v = colormap.getColor(f);
		else {
			v = PixelUtils.clamp((int)(f*255));
			int r = v << 16;
			int g = v << 8;
			int b = v;
			v = a|r|g|b;
		}
		if (operation != PixelUtils.REPLACE)
			v = PixelUtils.combinePixels(rgb, v, operation);
		return v;
	}

	@Override
	public String toString() {
		return "Texture/Fractal Brownian Motion...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Stretch")))!=null)setStretch(ImageFilterUtil.toFloatValue(o,"Stretch"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("BasisType")))!=null)setBasisType(ImageFilterUtil.toIntValue(o,"BasisType"));
		if((o=parameters.removeEL(KeyImpl.init("Operation")))!=null)setOperation(ImageFilterUtil.toIntValue(o,"Operation"));
		if((o=parameters.removeEL(KeyImpl.init("Octaves")))!=null)setOctaves(ImageFilterUtil.toFloatValue(o,"Octaves"));
		if((o=parameters.removeEL(KeyImpl.init("H")))!=null)setH(ImageFilterUtil.toFloatValue(o,"H"));
		if((o=parameters.removeEL(KeyImpl.init("Lacunarity")))!=null)setLacunarity(ImageFilterUtil.toFloatValue(o,"Lacunarity"));
		if((o=parameters.removeEL(KeyImpl.init("Gain")))!=null)setGain(ImageFilterUtil.toFloatValue(o,"Gain"));
		if((o=parameters.removeEL(KeyImpl.init("Bias")))!=null)setBias(ImageFilterUtil.toFloatValue(o,"Bias"));
		if((o=parameters.removeEL(KeyImpl.init("Basis")))!=null)setBasis(ImageFilterUtil.toFunction2D(o,"Basis"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Colormap, Amount, Stretch, Angle, BasisType, Operation, Octaves, H, Lacunarity, Gain, Bias, Basis, Scale, Dimensions]");
		}

		return filter(src, dst);
	}
}