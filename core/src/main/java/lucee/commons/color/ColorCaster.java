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
package lucee.commons.color;

import java.awt.Color;

import javax.servlet.ServletException;

import lucee.commons.lang.NumberUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ColorCaster {

	/**
	 * calculate the contrast between 2 colors
	 * 
	 * @param left
	 * @param right
	 * @return an int between 0 (badest) and 510 (best)
	 * @throws ServletException
	 */
	public static int contrast(Color left, Color right) throws ServletException {
		return (Math.max(left.getRed(), right.getRed()) - Math.min(left.getRed(), right.getRed()))
				+ (Math.max(left.getGreen(), right.getGreen()) - Math.min(left.getGreen(), right.getGreen()))
				+ (Math.max(left.getBlue(), right.getBlue()) - Math.max(left.getBlue(), right.getBlue()));
	}

	public static Color toColor(String strColor) throws ExpressionException {
		if (StringUtil.isEmpty(strColor, true)) throw new ExpressionException("can't cast empty string to a color Object");

		strColor = strColor.toLowerCase().trim();
		char first = strColor.charAt(0);

		switch (first) {
		case 'a':
			if ("aqua".equals(strColor)) return new Color(0, 0xFF, 0xFF);
			if ("aliceblue".equals(strColor)) return new Color(0xF0, 0xF8, 0xFF);
			if ("antiquewhite".equals(strColor)) return new Color(0xFA, 0xEB, 0xD7);
			if ("quamarine".equals(strColor)) return new Color(0x7F, 0xFF, 0xD4);
			if ("azure".equals(strColor)) return new Color(0xF0, 0xFF, 0xFF);
			break;
		case 'b':
			if ("black".equals(strColor)) return Color.BLACK;
			if ("blue".equals(strColor)) return Color.BLUE;
			if ("blue".equals(strColor)) return Color.CYAN;
			if ("beige".equals(strColor)) return new Color(0xF5, 0xF5, 0xDC);
			if ("blueviolet".equals(strColor)) return new Color(0x8A, 0x2B, 0xE2);
			if ("brown".equals(strColor)) return new Color(0xA5, 0x2A, 0x2A);
			if ("burlywood".equals(strColor)) return new Color(0xDE, 0xB8, 0x87);
			break;
		case 'c':
			if ("cyan".equals(strColor)) return Color.CYAN;
			if ("cadetblue".equals(strColor)) return new Color(0x5F, 0x9E, 0xA0);
			if ("chartreuse".equals(strColor)) return new Color(0x7F, 0xFF, 0x00);
			if ("chocolate".equals(strColor)) return new Color(0xD2, 0x69, 0x1E);
			if ("coral".equals(strColor)) return new Color(0xFF, 0x7F, 0x50);
			if ("cornflowerblue".equals(strColor)) return new Color(0x64, 0x95, 0xED);
			if ("cornsilk".equals(strColor)) return new Color(0xFF, 0xF8, 0xDC);
			if ("crimson".equals(strColor)) return new Color(0xDC, 0x14, 0x3C);
			break;
		case 'd':
			if ("darkgray".equals(strColor)) return Color.DARK_GRAY;
			if ("darkgrey".equals(strColor)) return Color.DARK_GRAY;
			if ("darkblue".equals(strColor)) return new Color(0x00, 0x00, 0x8B);
			if ("darkcyan".equals(strColor)) return new Color(0x00, 0x8B, 0x8B);
			if ("darkgoldenrod".equals(strColor)) return new Color(0xB8, 0x86, 0x0B);
			if ("darkgreen".equals(strColor)) return new Color(0x00, 0x64, 0x00);
			if ("darkkhaki".equals(strColor)) return new Color(0xBD, 0xB7, 0x6B);
			if ("darkmagenta".equals(strColor)) return new Color(0x8B, 0x00, 0x8B);
			if ("darkolivegreen".equals(strColor)) return new Color(0x55, 0x6B, 0x2F);
			if ("darkorange".equals(strColor)) return new Color(0xFF, 0x8C, 0x00);
			if ("darkorchid".equals(strColor)) return new Color(0x99, 0x32, 0xcc);
			if ("darkred".equals(strColor)) return new Color(0x8B, 0x00, 0x00);
			if ("darksalmon".equals(strColor)) return new Color(0xE9, 0x96, 0x7A);
			if ("darkseagreen".equals(strColor)) return new Color(0x8F, 0xBC, 0x8F);
			if ("darkslateblue".equals(strColor)) return new Color(0x2F, 0x4F, 0x4F);
			if ("darkslategray".equals(strColor)) return new Color(0x48, 0x3D, 0x8B);
			if ("darkslategrey".equals(strColor)) return new Color(0x48, 0x3D, 0x8B);
			if ("darkturquoise".equals(strColor)) return new Color(0x00, 0xCE, 0xD1);
			if ("darkviolet".equals(strColor)) return new Color(0x94, 0x00, 0xD3);
			if ("deeppink".equals(strColor)) return new Color(0xFF, 0x14, 0x93);
			if ("deepskyblue".equals(strColor)) return new Color(0x00, 0xBF, 0xFF);
			if ("dimgray".equals(strColor)) return new Color(0x69, 0x69, 0x69);
			if ("dodgerblue".equals(strColor)) return new Color(0x1E, 0x90, 0xFF);
			break;
		case 'f':
			if ("fuchsia".equals(strColor)) return new Color(0xFF, 0, 0xFF);
			if ("firebrick".equals(strColor)) return new Color(0xB2, 0x22, 0x22);
			if ("floralwhite".equals(strColor)) return new Color(0xFF, 0xFA, 0xF0);
			if ("forestgreen".equals(strColor)) return new Color(0x22, 0x8B, 0x22);
			break;
		case 'g':
			if ("gray".equals(strColor)) return Color.GRAY;
			if ("grey".equals(strColor)) return Color.GRAY;
			if ("green".equals(strColor)) return Color.GREEN;
			if ("gainsboro".equals(strColor)) return new Color(0xDC, 0xDC, 0xDC);
			if ("ghostwhite".equals(strColor)) return new Color(0xF8, 0xF8, 0xFF);
			if ("gold".equals(strColor)) return new Color(0xFF, 0xD7, 0x00);
			if ("goldenrod".equals(strColor)) return new Color(0xDA, 0xA5, 0x20);
			if ("greenyellow".equals(strColor)) return new Color(0xAD, 0xFF, 0x2F);

			break;
		case 'h':
			if ("honeydew".equals(strColor)) return new Color(0xF0, 0xFF, 0xF0);
			if ("hotpink".equals(strColor)) return new Color(0xFF, 0x69, 0xB4);
			break;
		case 'i':
			if ("indianred".equals(strColor)) return new Color(0xCD, 0x5C, 0x5C);
			if ("indigo".equals(strColor)) return new Color(0x4B, 0x00, 0x82);
			if ("ivory".equals(strColor)) return new Color(0xFF, 0xFF, 0xF0);
			break;
		case 'k':
			if ("khaki".equals(strColor)) return new Color(0xF0, 0xE6, 0x8C);
			break;
		case 'l':
			if ("lightgray".equals(strColor)) return Color.lightGray;
			if ("lightgrey".equals(strColor)) return Color.lightGray;
			if ("lime".equals(strColor)) return new Color(0, 0xFF, 0);
			if ("lavender".equals(strColor)) return new Color(0xE6, 0xE6, 0xFA);
			if ("lavenderblush".equals(strColor)) return new Color(0xFF, 0xF0, 0xF5);
			if ("lawngreen".equals(strColor)) return new Color(0x7C, 0xFC, 0x00);
			if ("lemonchiffon".equals(strColor)) return new Color(0xFF, 0xFA, 0xCD);
			if ("lightblue".equals(strColor)) return new Color(0xAD, 0xD8, 0xE6);
			if ("lightcoral".equals(strColor)) return new Color(0xF0, 0x80, 0x80);
			if ("lightcyan".equals(strColor)) return new Color(0xE0, 0xFF, 0xFF);
			if ("lightgoldenrodyellow".equals(strColor)) return new Color(0xFA, 0xFA, 0xD2);
			if ("lightgreen".equals(strColor)) return new Color(0x90, 0xEE, 0x90);
			if ("lightgrey".equals(strColor)) return new Color(0xD3, 0xD3, 0xD3);
			if ("lightpink".equals(strColor)) return new Color(0xFF, 0xB6, 0xC1);
			if ("lightsalmon".equals(strColor)) return new Color(0xFF, 0xA0, 0x7A);
			if ("lightseagreen".equals(strColor)) return new Color(0x20, 0xB2, 0xAA);
			if ("lightskyblue".equals(strColor)) return new Color(0x87, 0xCE, 0xFA);
			if ("lightslategray".equals(strColor)) return new Color(0x77, 0x88, 0x99);
			if ("lightslategrey".equals(strColor)) return new Color(0x77, 0x88, 0x99);
			if ("lightsteelblue".equals(strColor)) return new Color(0xB0, 0xC4, 0xDE);
			if ("lightyellow".equals(strColor)) return new Color(0xFF, 0xFF, 0xE0);
			if ("limegreen".equals(strColor)) return new Color(0x32, 0xCD, 0x32);
			if ("linen".equals(strColor)) return new Color(0xFA, 0xF0, 0xE6);

			break;
		case 'm':
			if ("magenta".equals(strColor)) return Color.MAGENTA;
			if ("maroon".equals(strColor)) return new Color(0X80, 0, 0);
			if ("mediumaquamarine".equals(strColor)) return new Color(0x66, 0xCD, 0xAA);
			if ("mediumblue".equals(strColor)) return new Color(0x00, 0x00, 0xCD);
			if ("mediumorchid".equals(strColor)) return new Color(0xBA, 0x55, 0xD3);
			if ("mediumpurple".equals(strColor)) return new Color(0x93, 0x70, 0xDB);
			if ("mediumseagreen".equals(strColor)) return new Color(0x3C, 0xB3, 0x71);
			if ("mediumslateblue".equals(strColor)) return new Color(0x7B, 0x68, 0xEE);
			if ("mediumspringgreen".equals(strColor)) return new Color(0x00, 0xFA, 0x9A);
			if ("mediumturquoise".equals(strColor)) return new Color(0x48, 0xD1, 0xCC);
			if ("mediumvioletred".equals(strColor)) return new Color(0xC7, 0x15, 0x85);
			if ("midnightblue".equals(strColor)) return new Color(0x19, 0x19, 0x70);
			if ("mintcream".equals(strColor)) return new Color(0xF5, 0xFF, 0xFA);
			if ("mistyrose".equals(strColor)) return new Color(0xFF, 0xE4, 0xE1);
			if ("moccasin".equals(strColor)) return new Color(0xFF, 0xE4, 0xB5);
			break;
		case 'n':
			if ("navy".equals(strColor)) return new Color(0, 0, 0X80);
			if ("navajowhite".equals(strColor)) return new Color(0xFF, 0xDE, 0xAD);
			break;
		case 'o':
			if ("orange".equals(strColor)) return Color.ORANGE;
			if ("olive".equals(strColor)) return new Color(0X80, 0X80, 0);
			if ("oldlace".equals(strColor)) return new Color(0xFD, 0xF5, 0xE6);
			if ("olivedrab".equals(strColor)) return new Color(0x6B, 0x8E, 0x23);
			if ("orangered".equals(strColor)) return new Color(0xFF, 0x45, 0x00);
			if ("orchid".equals(strColor)) return new Color(0xDA, 0x70, 0xD6);
			break;
		case 'p':
			if ("pink".equals(strColor)) return Color.PINK;
			if ("purple".equals(strColor)) return new Color(0X80, 0, 0X80);
			if ("palegoldenrod".equals(strColor)) return new Color(0xEE, 0xE8, 0xAA);
			if ("palegreen".equals(strColor)) return new Color(0x98, 0xFB, 0x98);
			if ("paleturquoise".equals(strColor)) return new Color(0xAF, 0xEE, 0xEE);
			if ("palevioletred".equals(strColor)) return new Color(0xDB, 0x70, 0x93);
			if ("papayawhip".equals(strColor)) return new Color(0xFF, 0xEF, 0xD5);
			if ("peachpuff".equals(strColor)) return new Color(0xFF, 0xDA, 0xB9);
			if ("peru".equals(strColor)) return new Color(0xCD, 0x85, 0x3F);
			if ("pink".equals(strColor)) return new Color(0xFF, 0xC0, 0xCB);
			if ("plum".equals(strColor)) return new Color(0xDD, 0xA0, 0xDD);
			if ("powderblue".equals(strColor)) return new Color(0xB0, 0xE0, 0xE6);
			break;
		case 'r':
			if ("red".equals(strColor)) return Color.RED;
			if ("rosybrown".equals(strColor)) return new Color(0xBC, 0x8F, 0x8F);
			if ("royalblue".equals(strColor)) return new Color(0x41, 0x69, 0xE1);
			break;
		case 's':
			if ("silver".equals(strColor)) return new Color(0XC0, 0XC0, 0XC0);
			if ("saddlebrown".equals(strColor)) return new Color(0x8B, 0x45, 0x13);
			if ("salmon".equals(strColor)) return new Color(0xFA, 0x80, 0x72);
			if ("sandybrown".equals(strColor)) return new Color(0xF4, 0xA4, 0x60);
			if ("seagreen".equals(strColor)) return new Color(0x2E, 0x8B, 0x57);
			if ("seashell".equals(strColor)) return new Color(0xFF, 0xF5, 0xEE);
			if ("sienna".equals(strColor)) return new Color(0xA0, 0x52, 0x2D);
			if ("skyblue".equals(strColor)) return new Color(0x87, 0xCE, 0xEB);
			if ("slateblue".equals(strColor)) return new Color(0x6A, 0x5A, 0xCD);
			if ("slategray".equals(strColor)) return new Color(0x70, 0x80, 0x90);
			if ("slategrey".equals(strColor)) return new Color(0x70, 0x80, 0x90);
			if ("snow".equals(strColor)) return new Color(0xFF, 0xFA, 0xFA);
			if ("springgreen".equals(strColor)) return new Color(0x00, 0xFF, 0x7F);
			if ("steelblue".equals(strColor)) return new Color(0x46, 0x82, 0xB4);
			break;
		case 't':
			if ("teal".equals(strColor)) return new Color(0, 0X80, 0X80);
			if ("tan".equals(strColor)) return new Color(0xD2, 0xB4, 0x8C);
			if ("thistle".equals(strColor)) return new Color(0xD8, 0xBF, 0xD8);
			if ("tomato".equals(strColor)) return new Color(0xFF, 0x63, 0x47);
			if ("turquoise".equals(strColor)) return new Color(0x40, 0xE0, 0xD0);
			// if("".equals(strColor)) return new Color(0x,0x,0x);
			break;
		case 'v':
			if ("violet".equals(strColor)) return new Color(0xEE, 0x82, 0xEE);
			break;
		case 'w':
			if ("white".equals(strColor)) return Color.WHITE;
			if ("wheat".equals(strColor)) return new Color(0xF5, 0xDE, 0xB3);
			if ("whitesmoke".equals(strColor)) return new Color(0xF5, 0xF5, 0xF5);
			break;
		case 'y':
			if ("yellow".equals(strColor)) return Color.YELLOW;
			if ("yellowgreen".equals(strColor)) return new Color(0x9A, 0xCD, 0x32);
			break;
		}

		if (first == '#') {
			String strColor2 = strColor.substring(1);
			// #fff
			if (strColor2.length() == 3) {
				char c1 = strColor2.charAt(0);
				char c2 = strColor2.charAt(1);
				char c3 = strColor2.charAt(2);
				return new Color(NumberUtil.hexToInt("" + c1 + c1), NumberUtil.hexToInt("" + c2 + c2), NumberUtil.hexToInt("" + c3 + c3));
			}
			// #ffffff
			if (strColor2.length() == 6) {
				String s1 = strColor2.substring(0, 2);
				String s2 = strColor2.substring(2, 4);
				String s3 = strColor2.substring(4, 6);
				return new Color(NumberUtil.hexToInt(s1), NumberUtil.hexToInt(s2), NumberUtil.hexToInt(s3));
			}
			// #ffffffff
			if (strColor2.length() == 8) {
				String s1 = strColor2.substring(0, 2);
				String s2 = strColor2.substring(2, 4);
				String s3 = strColor2.substring(4, 6);
				String s4 = strColor2.substring(6, 8);
				return new Color(NumberUtil.hexToInt(s1), NumberUtil.hexToInt(s2), NumberUtil.hexToInt(s3), NumberUtil.hexToInt(s4));
			}
		}

		// rgb(255,0,0)
		if (strColor.startsWith("rgb(") && strColor.endsWith(")")) {
			String strColor2 = strColor.substring(4, strColor.length() - 1).trim();
			String[] arr = ListUtil.listToStringArray(strColor2, ',');
			if (arr.length == 3) {
				int i1 = Caster.toIntValue(arr[0]);
				int i2 = Caster.toIntValue(arr[1]);
				int i3 = Caster.toIntValue(arr[2]);
				return new Color(i1, i2, i3);
			}
		}

		// fff
		if (strColor.length() == 3) {
			char c1 = strColor.charAt(0);
			char c2 = strColor.charAt(1);
			char c3 = strColor.charAt(2);
			int i1 = NumberUtil.hexToInt("" + c1 + c1, -1);
			int i2 = NumberUtil.hexToInt("" + c2 + c2, -1);
			int i3 = NumberUtil.hexToInt("" + c3 + c3, -1);
			if (i1 != -1 && i2 != -1 && i3 != -1) return new Color(i1, i2, i3);
		}
		// ffffff
		else if (strColor.length() == 6) {
			String s1 = strColor.substring(0, 2);
			String s2 = strColor.substring(2, 4);
			String s3 = strColor.substring(4, 6);
			int i1 = NumberUtil.hexToInt(s1, -1);
			int i2 = NumberUtil.hexToInt(s2, -1);
			int i3 = NumberUtil.hexToInt(s3, -1);
			if (i1 != -1 && i2 != -1 && i3 != -1) return new Color(i1, i2, i3);
		}
		// ffffffff
		else if (strColor.length() == 8) {
			String s1 = strColor.substring(0, 2);
			String s2 = strColor.substring(2, 4);
			String s3 = strColor.substring(4, 6);
			String s4 = strColor.substring(6, 8);
			int i1 = NumberUtil.hexToInt(s1, -1);
			int i2 = NumberUtil.hexToInt(s2, -1);
			int i3 = NumberUtil.hexToInt(s3, -1);
			int i4 = NumberUtil.hexToInt(s4, -1);
			if (i1 != -1 && i2 != -1 && i3 != -1 && i4 != -1) return new Color(i1, i2, i3, i4);
		}

		// 255,0,0
		String[] arr = ListUtil.listToStringArray(strColor, ',');
		if (arr.length == 3) {
			int i1 = Caster.toIntValue(arr[0], -1);
			int i2 = Caster.toIntValue(arr[1], -1);
			int i3 = Caster.toIntValue(arr[2], -1);
			if (i1 > -1 && i2 > -1 && i3 > -1) return new Color(i1, i2, i3);
		}

		throw new ExpressionException("invalid color definition [" + strColor + "]",
				"color must be a know constant label (blue,green,yellow ...), a hexadecimal value (#ffffff) or a RGB value (rgb(255,255,255)), 255,255,255");
	}

	public static String toHexString(Color color) {
		return "#" + toHexString(color.getRed()) + toHexString(color.getGreen()) + toHexString(color.getBlue());

	}

	private static String toHexString(int clr) {
		String str = Integer.toHexString(clr);
		if (str.length() == 1) return "0" + str;
		return str;
	}

}