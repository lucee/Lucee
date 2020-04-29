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
package lucee.runtime.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import lucee.commons.lang.StringUtil;

/**
 * Number formation class
 */
public final class NumberFormat {

	private static byte LEFT = 0;
	private static byte CENTER = 1;
	private static byte RIGHT = 2;

	/**
	 * formats a number
	 * 
	 * @param number
	 * @return formatted number as string
	 */
	public String format(Locale locale, double number) {
		DecimalFormat df = getDecimalFormat(locale);
		df.applyPattern(",0");
		df.setGroupingSize(3);
		return df.format(number);
	}

	/**
	 * format a number with given mask
	 * 
	 * @param number
	 * @param mask
	 * @return formatted number as string
	 * @throws InvalidMaskException
	 *
	 */

	public String formatX(Locale locale, double number, String mask) throws InvalidMaskException {
		return format(locale, number, convertMask(mask));
	}

	public String format(Locale locale, double number, Mask mask) throws InvalidMaskException {
		BigDecimal bd = new BigDecimal(Double.toString(number));
		bd = bd.setScale((int) mask.right, RoundingMode.HALF_UP);
		number = bd.doubleValue();
		int maskLen = mask.str.length();
		DecimalFormat df = getDecimalFormat(locale);// (mask);
		int gs = df.getGroupingSize();
		df.applyPattern(mask.str);
		df.setGroupingSize(gs);
		df.setGroupingUsed(mask.useComma);
		df.setRoundingMode(RoundingMode.UNNECESSARY);
		if (df.getMaximumFractionDigits() > 100) df.setMaximumFractionDigits(mask.right < 11 ? 11 : mask.right); // the if here exists because the value is acting unprecticted in
		// some cases, so we onkly do if really necessary

		String formattedNum = df.format(StrictMath.abs(number));
		StringBuilder formattedNumBuffer = new StringBuilder(formattedNum);
		if (mask.symbolsFirst) {
			int widthBefore = formattedNumBuffer.length();
			applySymbolics(formattedNumBuffer, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets);
			int offset = formattedNumBuffer.length() - widthBefore;

			if (formattedNumBuffer.length() < maskLen + offset) {
				int padding = (maskLen + offset) - formattedNumBuffer.length();
				applyJustification(formattedNumBuffer, mask.justification, padding);
			}
		}
		else {
			int widthBefore = formattedNumBuffer.length();

			StringBuilder temp = new StringBuilder(formattedNumBuffer.toString());
			applySymbolics(temp, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets);
			int offset = temp.length() - widthBefore;

			if (temp.length() < maskLen + offset) {
				int padding = (maskLen + offset) - temp.length();
				applyJustification(formattedNumBuffer, mask.justification, padding);
			}
			applySymbolics(formattedNumBuffer, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets);
		}
		return formattedNumBuffer.toString();
	}

	public static class Mask {
		public byte justification = RIGHT;
		public boolean useBrackets = false;
		public boolean usePlus = false;
		public boolean useMinus = false;
		public boolean useDollar = false;
		public boolean useComma = false;
		public boolean symbolsFirst = false;
		public int right = 0;
		public String str;
	}

	public static Mask convertMask(String str) throws InvalidMaskException {
		Mask mask = new Mask();
		boolean foundDecimal = false;
		boolean foundZero = false;

		int maskLen = str.length();
		if (maskLen == 0) throw new InvalidMaskException("mask can't be an empty value");

		StringBuilder maskBuffer = new StringBuilder(str);

		String mod = StringUtil.replace(str, ",", "", true);
		if (StringUtil.startsWith(mod, '_')) mask.symbolsFirst = true;
		if (str.startsWith(",.")) {
			maskBuffer.replace(0, 1, ",0");
		}
		// if(maskBuffer.charAt(0) == '.')maskBuffer.insert(0, '0');
		// print.out(maskBuffer);
		boolean addZero = false;
		for (int i = 0; i < maskBuffer.length();) {

			boolean removeChar = false;
			switch (maskBuffer.charAt(i)) {
			case '_':
			case '9':
				if (foundDecimal) {
					maskBuffer.setCharAt(i, '0');
					mask.right++;
				}
				else if (foundZero) maskBuffer.setCharAt(i, '0');
				else maskBuffer.setCharAt(i, '#');// #
				break;

			case '.':
				if (i > 0 && maskBuffer.charAt(i - 1) == '#') maskBuffer.setCharAt(i - 1, '0');
				if (foundDecimal) removeChar = true;
				else foundDecimal = true;
				if (i == 0) addZero = true;
				break;

			case '(':
			case ')':
				mask.useBrackets = true;
				removeChar = true;
				break;

			case '+':
				mask.usePlus = true;
				removeChar = true;
				break;

			case '-':
				mask.useMinus = true;
				removeChar = true;
				break;

			case ',':
				mask.useComma = true;
				if (true) {
					removeChar = true;
					maskLen++;
				}
				break;

			case 'L':
				mask.justification = LEFT;
				removeChar = true;
				break;

			case 'C':
				mask.justification = CENTER;
				removeChar = true;
				break;

			case '$':
				mask.useDollar = true;
				removeChar = true;
				break;

			case '^':
				removeChar = true;
				break;

			case '0':
				if (!foundDecimal) {
					for (int y = 0; y < i; y++) {
						if (maskBuffer.charAt(y) == '#') maskBuffer.setCharAt(y, '0');
					}
				}
				foundZero = true;
				break;

			default:
				throw new InvalidMaskException(
						"invalid charcter [" + maskBuffer.charAt(i) + "], valid characters are ['_', '9', '.', '0', '(', ')', '+', '-', ',', 'L', 'C', '$', '^']");

			}
			if (removeChar) {
				maskBuffer.deleteCharAt(i);
				maskLen--;
			}
			else {
				i++;
			}
		}

		if (addZero) addSymbol(maskBuffer, '0');
		mask.str = new String(maskBuffer);
		return mask;
	}

	private void applyJustification(StringBuilder _buffer, int _just, int padding) {
		if (_just == CENTER) centerJustify(_buffer, padding);
		else if (_just == LEFT) leftJustify(_buffer, padding);
		else rightJustify(_buffer, padding);
	}

	private void applySymbolics(StringBuilder sb, double no, boolean usePlus, boolean useMinus, boolean useDollar, boolean useBrackets) {
		if (useBrackets && no < 0.0D) {
			addSymbol(sb, '(');
			sb.append(')');
		}
		if (usePlus) addSymbol(sb, no < 0.0D ? '-' : '+');
		if (no < 0.0D && !useBrackets && !usePlus) addSymbol(sb, '-');
		else if (useMinus) addSymbol(sb, ' ');
		if (useDollar) addSymbol(sb, '$');
	}

	private static void addSymbol(StringBuilder sb, char symbol) {
		int offset = 0;
		while (sb.length() > offset && Character.isWhitespace(sb.charAt(offset))) {
			offset++;
		}
		sb.insert(offset, symbol);
	}

	private void centerJustify(StringBuilder _src, int _padding) {
		int padSplit = _padding / 2 + 1;
		rightJustify(_src, padSplit);
		leftJustify(_src, padSplit);
	}

	private void rightJustify(StringBuilder _src, int _padding) {
		for (int x = 0; x < _padding; x++)
			_src.insert(0, ' ');

	}

	private void leftJustify(StringBuilder _src, int _padding) {
		for (int x = 0; x < _padding; x++)
			_src.append(' ');

	}

	private DecimalFormat getDecimalFormat(Locale locale) {
		java.text.NumberFormat format = java.text.NumberFormat.getInstance(locale);
		if (format instanceof DecimalFormat) {
			return ((DecimalFormat) format);

		}
		return new DecimalFormat();
	}

}