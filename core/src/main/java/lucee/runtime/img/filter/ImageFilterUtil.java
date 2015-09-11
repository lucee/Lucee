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
package lucee.runtime.img.filter;import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import lucee.commons.color.ColorCaster;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.img.filter.LightFilter.Material;
import lucee.runtime.img.math.Function2D;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.Type;

public class ImageFilterUtil {

	public static float toFloatValue(Object value, String argName) throws FunctionException {
		float res = Caster.toFloatValue(value,Float.NaN);
		if(Float.isNaN(res)) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"float",argName));
		}
		return res;
	}

	public static int toIntValue(Object value, String argName) throws FunctionException {
		int res = Caster.toIntValue(value,Integer.MIN_VALUE);
		if(Integer.MIN_VALUE==res) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"int",argName));
		}
		return res;
	}
	public static boolean toBooleanValue(Object value, String argName) throws FunctionException {
		Boolean res = Caster.toBoolean(value,null);
		if(res==null) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"boolean",argName));
		}
		return res;
	}
	public static String toString(Object value, String argName) throws FunctionException {
		String res = Caster.toString(value,null);
		if(res==null) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"String",argName));
		}
		return res;
	}
	


	public static BufferedImage toBufferedImage(Object o, String argName) throws PageException {
		if(o instanceof BufferedImage) return (BufferedImage) o;
		return Image.toImage(ThreadLocalPageContext.get(),o).getBufferedImage();
	}

	public static Colormap toColormap(Object value, String argName) throws FunctionException {
		if(value instanceof Colormap)
			return (Colormap) value;
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"Colormap",argName)+" use function ImageFilterColorMap to create a colormap");
	}
	
	////

	public static Color toColor(Object value, String argName) throws PageException {
		if(value instanceof Color)
			return (Color) value;
		return ColorCaster.toColor(Caster.toString(value));
		
	}
	
	public static int toColorRGB(Object value, String argName) throws PageException {
		return toColor(value, argName).getRGB();
		
	}
	


	public static Point toPoint(Object value, String argName) throws PageException {
		if(value instanceof Point) return (Point) value;
		String str = Caster.toString(value);
		
		Struct sct = Caster.toStruct(value,null);
		if(sct!=null){
			return new Point(Caster.toIntValue(sct.get("x")),Caster.toIntValue(sct.get("y")));
		}
		
		String[] arr = ListUtil.listToStringArray(str, ',');
		if(arr.length==2) {
			return new Point(Caster.toIntValue(arr[0]),Caster.toIntValue(arr[1]));
		}
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "use the following format [x,y]");
		
	}

	public static int[] toDimensions(Object value, String argName) throws PageException {
		return toAInt(value, argName);
	}

	public static LightFilter.Material toLightFilter$Material(Object value, String argName) throws PageException {
		if(value instanceof LightFilter.Material)
			return (LightFilter.Material) value;
		
		Struct sct = Caster.toStruct(value,null);
		if(sct!=null){
			Material material = new LightFilter.Material();
			material.setDiffuseColor(toColorRGB(sct.get("color"), argName+".color"));
			material.setOpacity(Caster.toFloatValue(sct.get("opacity")));
			return material;
		}
		String str = Caster.toString(value,null);
		if(str!=null){
			String[] arr = ListUtil.listToStringArray(str, ',');
			if(arr.length==2) {
				Material material = new LightFilter.Material();
				material.setDiffuseColor(toColorRGB(arr[0], argName+"[1]"));
				material.setOpacity(Caster.toFloatValue(arr[1]));
				return material;
			}
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "use the following format [color,opacity]");
			
		}
		
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "use the following format [\"color,opacity\"] or [{color='#cc0033',opacity=0.5}]");
		
	}

	public static Function2D toFunction2D(Object value, String argName) throws FunctionException {
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "type Function2D not supported yet!");
	}


	public static AffineTransform toAffineTransform(Object value, String argName) throws FunctionException {
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "type BufferedImage not supported yet!");
	}

	public static Composite toComposite(Object value, String argName) throws FunctionException {
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "type Composite not supported yet!");
	}

	public static CurvesFilter.Curve[] toACurvesFilter$Curve(Object value, String argName) throws PageException {
		if(value instanceof CurvesFilter.Curve[]) return (CurvesFilter.Curve[]) value;
		Object[] arr = Caster.toNativeArray(value);
		CurvesFilter.Curve[] curves=new CurvesFilter.Curve[arr.length];
		for(int i=0;i<arr.length;i++){
			curves[i]=toCurvesFilter$Curve(arr[i],argName);
		}
		return curves;
	}

	public static CurvesFilter.Curve toCurvesFilter$Curve(Object value, String argName) throws FunctionException {
		if(value instanceof CurvesFilter.Curve)
			return (CurvesFilter.Curve) value;
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"Curve",argName)+" use function ImageFilterCurve to create a Curve");
	}

	public static int[] toAInt(Object value, String argName) throws PageException {
		return ArrayUtil.toIntArray(value);
	}

	public static float[] toAFloat(Object value, String argName) throws PageException {
		return ArrayUtil.toFloatArray(value);
	}

	public static int[][] toAAInt(Object value, String argName) throws FunctionException {
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "type int[][] not supported yet!");
	}

	public static WarpGrid toWarpGrid(Object value, String argName) throws FunctionException {
		if(value instanceof WarpGrid)
			return (WarpGrid) value;
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", msg(value,"WarpGrid",argName)+" use function ImageFilterWarpGrid to create a WarpGrid");
	}

	public static FieldWarpFilter.Line[] toAFieldWarpFilter$Line(Object o, String string) throws FunctionException {
		throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "type WarpGrid not supported yet!");
	}
	
	
	
	

	private static String msg(Object value, String type, String argName) {
		return "Can't cast argument ["+argName+"] from type ["+Type.getName(value)+"] to a value of type ["+type+"]";
	}

	public static Font toFont(Object o, String string) {
		// TODO Auto-generated method stub
		return null;
	}





	private static float range(float value, int from, int to) throws ExpressionException {
		if(value>=from && value<=to)
			return value;
		throw new ExpressionException("["+Caster.toString(value)+"] is out of range, value must be between ["+Caster.toString(from)+"] and ["+Caster.toString(to)+"]");
	}


}