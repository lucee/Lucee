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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import lucee.commons.lang.StringUtil;


/**
 * Number formation class
 */
public final class NumberFormat  {

	private static byte LEFT = 0;
	private static byte CENTER = 1;
	private static byte RIGHT = 2;
	
	/**
	 * formats a number
	 * @param number
	 * @return formatted number as string
	 */
	public String format(Locale locale,double number) {
		
		DecimalFormat df=getDecimalFormat(locale);
		df.applyPattern(",0");
		df.setGroupingSize(3);
		
		
		return df.format(number).replace('\'',',');
	}

	/**
	 * format a number with given mask
	 * @param number
	 * @param mask
	 * @return formatted number as string
	 * @throws InvalidMaskException
	 */
	public String format(Locale locale,double number, String mask) throws InvalidMaskException  {
		byte justification = RIGHT;
		
		boolean useBrackets = false;
		boolean usePlus = false;
		boolean useMinus = false;
		boolean useDollar = false;
		boolean useComma = false;
		boolean foundDecimal = false;
		boolean symbolsFirst = false;
		boolean foundZero=false;
		
		int maskLen = mask.length();
		if(maskLen == 0) throw new InvalidMaskException("mask can't be a empty value");
		
		
		
		StringBuffer maskBuffer = new StringBuffer(mask);
		
			String mod=StringUtil.replace(mask, ",", "", true);
			if(StringUtil.startsWith(mod, '_'))symbolsFirst = true;
			if(mask.startsWith(",."))	{
				maskBuffer.replace(0, 1, ",0");
			}
			//if(maskBuffer.charAt(0) == '.')maskBuffer.insert(0, '0');
		//print.out(maskBuffer);
		boolean addZero=false;
		for(int i = 0; i < maskBuffer.length();) {
			
			boolean removeChar = false;
			switch(maskBuffer.charAt(i)) {
			case '_':
			case '9':
				if(foundDecimal || foundZero)	maskBuffer.setCharAt(i, '0');
				else							maskBuffer.setCharAt(i, '#');// #
			break;

			case '.':
				if(i>0 && maskBuffer.charAt(i-1)=='#')maskBuffer.setCharAt(i-1, '0');
				if(foundDecimal)	removeChar = true;
				else				foundDecimal = true;
				if(i==0)addZero=true;
			break;

			case '(':
			case ')':
				useBrackets = true;
				removeChar = true;
			break;

			case '+':
				usePlus = true;
				removeChar = true;
			break;

			case '-':
				useMinus = true;
				removeChar = true;
			break;

			case ',':
				useComma = true;
				if(true) {
					removeChar = true;
					maskLen++;
				}
			break;

			case 'L':
				justification = LEFT;
				removeChar = true;
			break;

			case 'C':
				justification = CENTER;
				removeChar = true;
			break;

			case '$':
				useDollar = true;
				removeChar = true;
			break;

			case '^':
				removeChar = true;
			break;

			case '0':
				if(!foundDecimal){
					for(int y = 0; y < i;y++) {
						if(maskBuffer.charAt(y)=='#')
							maskBuffer.setCharAt(y, '0');
					}
				}
				foundZero=true;
				break;

			default:
			    throw new InvalidMaskException("invalid charcter ["+maskBuffer.charAt(i)+"], valid characters are ['_', '9', '.', '0', '(', ')', '+', '-', ',', 'L', 'C', '$', '^']");
			
			}
			if(removeChar) {
				maskBuffer.deleteCharAt(i);
				maskLen--;
			} 
			else {
				i++;
			}
		}

		if(addZero)
			maskBuffer.insert(0, '0');
		
		
		mask = new String(maskBuffer);
		maskLen=mask.length();
		DecimalFormat df = getDecimalFormat(locale);//(mask);
		int gs=df.getGroupingSize();
		df.applyPattern(mask);
		df.setGroupingSize(gs);
		df.setGroupingUsed(useComma);
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		String formattedNum = df.format(StrictMath.abs(number));
		StringBuffer formattedNumBuffer = new StringBuffer(formattedNum);
		if(symbolsFirst) {
			int widthBefore = formattedNumBuffer.length();
			applySymbolics(formattedNumBuffer, number, usePlus, useMinus, useDollar, useBrackets);
			int offset = formattedNumBuffer.length() - widthBefore;
			
			if(formattedNumBuffer.length() < maskLen + offset) {
				int padding = (maskLen + offset) - formattedNumBuffer.length();
				applyJustification(formattedNumBuffer,justification, padding);
			}
				
				
			
		} 
		else {
			int widthBefore = formattedNumBuffer.length();
			
			StringBuffer temp = new StringBuffer(formattedNumBuffer.toString());
			applySymbolics(temp, number, usePlus, useMinus, useDollar, useBrackets);
			int offset = temp.length() - widthBefore;
			
			if(temp.length() < maskLen + offset) {
				int padding = (maskLen + offset) - temp.length();
				applyJustification(formattedNumBuffer,justification, padding);
			}
			applySymbolics(formattedNumBuffer, number, usePlus, useMinus, useDollar, useBrackets);
		}
		/*/ TODO better impl, this is just a quick fix
		formattedNum=formattedNumBuffer.toString();
		 
		int index=formattedNum.indexOf('.');
		if(index==0) {
			formattedNumBuffer.insert(0, '0');
			formattedNum=formattedNumBuffer.toString();
		}
		else if(index>0){
			
		}
			
		String tmp=formattedNum.trim();
		if(tmp.length()>0 && tmp.charAt(0)=='.')
		*/
		return formattedNumBuffer.toString();
	}
	


	private void applyJustification(StringBuffer _buffer, int _just, int padding) {
		if(_just == CENTER)		centerJustify(_buffer, padding);
		else if(_just == LEFT)	leftJustify(_buffer, padding);
		else					rightJustify(_buffer, padding);
	}

	private void applySymbolics(StringBuffer _buffer, double _no, boolean _usePlus, boolean _useMinus, boolean _useDollar, boolean _useBrackets) {
		if(_useBrackets && _no < 0.0D) {
			_buffer.insert(0, '(');
			_buffer.append(')');
		}
		if(_usePlus)
			_buffer.insert(0, _no <= 0.0D ? '-' : '+');
		if(_no < 0.0D && !_useBrackets && !_usePlus)
			_buffer.insert(0, '-');
		else
		if(_useMinus)
			_buffer.insert(0, ' ');
		if(_useDollar)
			_buffer.insert(0, '$');
	}

	private void centerJustify(StringBuffer _src, int _padding) {
		int padSplit = _padding / 2 + 1;
		rightJustify(_src, padSplit);
		leftJustify(_src, padSplit);
	}

	private void rightJustify(StringBuffer _src, int _padding) {
		for(int x = 0; x < _padding; x++)
			_src.insert(0, ' ');

	}

	private void leftJustify(StringBuffer _src, int _padding) {
		for(int x = 0; x < _padding; x++)
			_src.append(' ');

	}

	private DecimalFormat getDecimalFormat(Locale locale) {
		java.text.NumberFormat format = java.text.NumberFormat.getInstance(locale);
		if(format instanceof DecimalFormat) {
			return ((DecimalFormat)format);
			
		}
		return new DecimalFormat();
	}

}