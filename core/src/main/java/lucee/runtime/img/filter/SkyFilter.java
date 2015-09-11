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

package lucee.runtime.img.filter;import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Random;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.img.math.Noise;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

public class SkyFilter extends PointFilter  implements DynFiltering {

	private float scale = 0.1f;
	private float stretch = 1.0f;
	private float angle = 0.0f;
	private float amount = 1.0f;
	private float H = 1.0f;
	private float octaves = 8.0f;
	private float lacunarity = 2.0f;
	private float gain = 1.0f;
	private float bias = 0.6f;
	private int operation;
	//private float min;
	//private float max;
	//private boolean ridged;
	//private FBM fBm;
	protected Random random = new Random();
	//private Function2D basis;

	private float cloudCover = 0.5f;
	private float cloudSharpness = 0.5f;
	private float time = 0.3f;
	private float glow = 0.5f;
	private float glowFalloff = 0.5f;
	private float haziness = 0.96f;
	private float t = 0.0f;
	//private float sunRadius = 10f;
	private int sunColor = 0xffffffff;
	private float sunR, sunG, sunB;
	private float sunAzimuth = 0.5f;
	private float sunElevation = 0.5f;
	private float windSpeed = 0.0f;

	private float cameraAzimuth = 0.0f;
	private float cameraElevation = 0.0f;
	private float fov = 1.0f;

	private float[] exponents;
	private float[] tan;
	private BufferedImage skyColors;
	//private int[] skyPixels;
	
	private final static float r255 = 1.0f/255.0f;

	private float width, height;

	public SkyFilter() {
		if ( skyColors == null ) {
			skyColors = ImageUtils.createImage( Toolkit.getDefaultToolkit().getImage( getClass().getResource("SkyColors.png") ).getSource() );
		}
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getAmount() {
		return amount;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}
	
	public int getOperation() {
		return operation;
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

	public void setT(float t) {
		this.t = t;
	}

	public float getT() {
		return t;
	}

	public void setFOV(float fov) {
		this.fov = fov;
	}

	public float getFOV() {
		return fov;
	}

	public void setCloudCover(float cloudCover) {
		this.cloudCover = cloudCover;
	}

	public float getCloudCover() {
		return cloudCover;
	}

	public void setCloudSharpness(float cloudSharpness) {
		this.cloudSharpness = cloudSharpness;
	}

	public float getCloudSharpness() {
		return cloudSharpness;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getTime() {
		return time;
	}

	public void setGlow(float glow) {
		this.glow = glow;
	}

	public float getGlow() {
		return glow;
	}

	public void setGlowFalloff(float glowFalloff) {
		this.glowFalloff = glowFalloff;
	}

	public float getGlowFalloff() {
		return glowFalloff;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

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

	public void setHaziness(float haziness) {
		this.haziness = haziness;
	}

	public float getHaziness() {
		return haziness;
	}

	public void setSunElevation(float sunElevation) {
		this.sunElevation = sunElevation;
	}

	public float getSunElevation() {
		return sunElevation;
	}

	public void setSunAzimuth(float sunAzimuth) {
		this.sunAzimuth = sunAzimuth;
	}

	public float getSunAzimuth() {
		return sunAzimuth;
	}

	public void setSunColor(int sunColor) {
		this.sunColor = sunColor;
	}

	public int getSunColor() {
		return sunColor;
	}

	public void setCameraElevation(float cameraElevation) {
		this.cameraElevation = cameraElevation;
	}

	public float getCameraElevation() {
		return cameraElevation;
	}

	public void setCameraAzimuth(float cameraAzimuth) {
		this.cameraAzimuth = cameraAzimuth;
	}

	public float getCameraAzimuth() {
		return cameraAzimuth;
	}

	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public float getWindSpeed() {
		return windSpeed;
	}

float mn, mx;
    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
long start = System.currentTimeMillis();
		sunR = ((sunColor >> 16) & 0xff) * r255;
		sunG = ((sunColor >> 8) & 0xff) * r255;
		sunB = (sunColor & 0xff) * r255;

mn = 10000;
mx = -10000;
		exponents = new float[(int)octaves+1];
		//float frequency = 1.0f;
		for (int i = 0; i <= (int)octaves; i++) {
			exponents[i] = (float)Math.pow(2, -i);
			//frequency *= lacunarity;
		}

		//min = -1;
		//max = 1;

//min = -1.2f;
//max = 1.2f;

		width = src.getWidth();
		height = src.getHeight();

		int h = src.getHeight();
		tan = new float[h];
		for (int i = 0; i < h; i++)
			tan[i] = (float)Math.tan( fov * i/h * Math.PI * 0.5 );

		if ( dst == null )
			dst = createCompatibleDestImage( src, null );
		int t = (int)(63*time);
//		skyPixels = getRGB( skyColors, t, 0, 1, 64, skyPixels );
		Graphics2D g = dst.createGraphics();
		g.drawImage( skyColors, 0, 0, dst.getWidth(), dst.getHeight(), t, 0, t+1, 64, null );
		g.dispose();
		super.filter( dst, dst );
//		g.drawRenderedImage( clouds, null );
//		g.dispose();
long finish = System.currentTimeMillis();
System.out.println(mn+" "+mx+" "+(finish-start)*0.001f);
		exponents = null;
		tan = null;
		return dst;
	}
	
	public float evaluate(float x, float y) {
		float value = 0.0f;
		float remainder;
		int i;
		
		// to prevent "cascading" effects
		x += 371;
		y += 529;
		
		for (i = 0; i < (int)octaves; i++) {
			value += Noise.noise3(x, y, t) * exponents[i];
			x *= lacunarity;
			y *= lacunarity;
		}

		remainder = octaves - (int)octaves;
		if (remainder != 0)
			value += remainder * Noise.noise3(x, y, t) * exponents[i];

		return value;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {

// Curvature
float fx = x / width;
//y += 20*Math.sin( fx*Math.PI*0.5 );
		float fy = y / height;
		float haze = (float)Math.pow( haziness, 100*fy*fy );
//		int argb = skyPixels[(int)fy];
		float r = ((rgb >> 16) & 0xff) * r255;
		float g = ((rgb >> 8) & 0xff) * r255;
		float b = (rgb & 0xff) * r255;

		float cx = width*0.5f;
		float nx = x-cx;
		float ny = y;
// FOV
//ny = (float)Math.tan( fov * fy * Math.PI * 0.5 );
ny = tan[y];
nx = (fx-0.5f) * (1+ny);
ny += t*windSpeed;// Wind towards the camera

//		float xscale = scale/(1+y*bias*0.1f);
		nx /= scale;
		ny /= scale * stretch;
		float f = evaluate(nx, ny);
//float fg = f;//FIXME-bump map
		// Normalize to 0..1
//		f = (f-min)/(max-min);

		f = (f+1.23f)/2.46f;

//		f *= amount;
		//int a = rgb & 0xff000000;
		int v;

		// Work out cloud cover
		float c = f - cloudCover;
		if (c < 0)
			c = 0;

		float cloudAlpha = 1 - (float)Math.pow(cloudSharpness, c);
//cloudAlpha *= amount;
//if ( cloudAlpha > 1 )
//	cloudAlpha = 1;
mn = Math.min(mn, cloudAlpha);
mx = Math.max(mx, cloudAlpha);

		// Sun glow
		float centreX = width*sunAzimuth;
		float centreY = height*sunElevation;
		float dx = x-centreX;
		float dy = y-centreY;
		float distance2 = dx*dx+dy*dy;
//		float sun = 0;
		//distance2 = (float)Math.sqrt(distance2);
distance2 = (float)Math.pow(distance2, glowFalloff);
		float sun = /*amount**/10*(float)Math.exp(-distance2*glow*0.1f);
//		sun = glow*10*(float)Math.exp(-distance2);

		// Sun glow onto sky
		r += sun * sunR;
		g += sun * sunG;
		b += sun * sunB;


//		float cloudColor = cloudAlpha *sun;
// Bump map
/*
		float nnx = x-cx;
		float nny = y-1;
		nnx /= xscale;
		nny /= xscale * stretch;
		float gf = evaluate(nnx, nny);
		float gradient = fg-gf;
if (y == 100)System.out.println(fg+" "+gf+gradient);
		cloudColor += amount * gradient;
*/
// ...
/*
		r += (cloudColor-r) * cloudAlpha;
		g += (cloudColor-g) * cloudAlpha;
		b += (cloudColor-b) * cloudAlpha;
*/
		// Clouds get darker as they get thicker
		float ca = (1-cloudAlpha*cloudAlpha*cloudAlpha*cloudAlpha) /** (1 + sun)*/ * amount;
		float cloudR = sunR * ca;
		float cloudG = sunG * ca;
		float cloudB = sunB * ca;

		// Apply the haziness as we move further away
		cloudAlpha *= haze;

		// Composite the clouds on the sky
		float iCloudAlpha = (1-cloudAlpha);
		r = iCloudAlpha*r + cloudAlpha*cloudR;
		g = iCloudAlpha*g + cloudAlpha*cloudG;
		b = iCloudAlpha*b + cloudAlpha*cloudB;

		// Exposure
		float exposure = gain;
		r = 1 - (float)Math.exp(-r * exposure);
		g = 1 - (float)Math.exp(-g * exposure);
		b = 1 - (float)Math.exp(-b * exposure);

		int ir = (int)(255*r) << 16;
		int ig = (int)(255*g) << 8;
		int ib = (int)(255*b);
		v = 0xff000000|ir|ig|ib;
		return v;
	}
	
	@Override
	public String toString() {
		return "Texture/Sky...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(KeyImpl.init("Stretch")))!=null)setStretch(ImageFilterUtil.toFloatValue(o,"Stretch"));
		if((o=parameters.removeEL(KeyImpl.init("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(KeyImpl.init("Operation")))!=null)setOperation(ImageFilterUtil.toIntValue(o,"Operation"));
		if((o=parameters.removeEL(KeyImpl.init("Octaves")))!=null)setOctaves(ImageFilterUtil.toFloatValue(o,"Octaves"));
		if((o=parameters.removeEL(KeyImpl.init("H")))!=null)setH(ImageFilterUtil.toFloatValue(o,"H"));
		if((o=parameters.removeEL(KeyImpl.init("Lacunarity")))!=null)setLacunarity(ImageFilterUtil.toFloatValue(o,"Lacunarity"));
		if((o=parameters.removeEL(KeyImpl.init("Gain")))!=null)setGain(ImageFilterUtil.toFloatValue(o,"Gain"));
		if((o=parameters.removeEL(KeyImpl.init("Bias")))!=null)setBias(ImageFilterUtil.toFloatValue(o,"Bias"));
		if((o=parameters.removeEL(KeyImpl.init("T")))!=null)setT(ImageFilterUtil.toFloatValue(o,"T"));
		if((o=parameters.removeEL(KeyImpl.init("FOV")))!=null)setFOV(ImageFilterUtil.toFloatValue(o,"FOV"));
		if((o=parameters.removeEL(KeyImpl.init("CloudCover")))!=null)setCloudCover(ImageFilterUtil.toFloatValue(o,"CloudCover"));
		if((o=parameters.removeEL(KeyImpl.init("CloudSharpness")))!=null)setCloudSharpness(ImageFilterUtil.toFloatValue(o,"CloudSharpness"));
		if((o=parameters.removeEL(KeyImpl.init("Glow")))!=null)setGlow(ImageFilterUtil.toFloatValue(o,"Glow"));
		if((o=parameters.removeEL(KeyImpl.init("GlowFalloff")))!=null)setGlowFalloff(ImageFilterUtil.toFloatValue(o,"GlowFalloff"));
		if((o=parameters.removeEL(KeyImpl.init("Haziness")))!=null)setHaziness(ImageFilterUtil.toFloatValue(o,"Haziness"));
		if((o=parameters.removeEL(KeyImpl.init("SunElevation")))!=null)setSunElevation(ImageFilterUtil.toFloatValue(o,"SunElevation"));
		if((o=parameters.removeEL(KeyImpl.init("SunAzimuth")))!=null)setSunAzimuth(ImageFilterUtil.toFloatValue(o,"SunAzimuth"));
		if((o=parameters.removeEL(KeyImpl.init("SunColor")))!=null)setSunColor(ImageFilterUtil.toColorRGB(o,"SunColor"));
		if((o=parameters.removeEL(KeyImpl.init("CameraElevation")))!=null)setCameraElevation(ImageFilterUtil.toFloatValue(o,"CameraElevation"));
		if((o=parameters.removeEL(KeyImpl.init("CameraAzimuth")))!=null)setCameraAzimuth(ImageFilterUtil.toFloatValue(o,"CameraAzimuth"));
		if((o=parameters.removeEL(KeyImpl.init("WindSpeed")))!=null)setWindSpeed(ImageFilterUtil.toFloatValue(o,"WindSpeed"));
		if((o=parameters.removeEL(KeyImpl.init("Time")))!=null)setTime(ImageFilterUtil.toFloatValue(o,"Time"));
		if((o=parameters.removeEL(KeyImpl.init("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Stretch, Angle, Operation, Octaves, H, Lacunarity, Gain, Bias, T, FOV, CloudCover, CloudSharpness, Glow, GlowFalloff, Haziness, SunElevation, SunAzimuth, SunColor, CameraElevation, CameraAzimuth, WindSpeed, Time, Scale, Dimensions]");
		}

		return filter(src, dst);
	}
}