/**
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
package lucee.commons.digest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lucee.print;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.runtime.coder.CoderException;
import lucee.runtime.functions.string.ToBase64;
import lucee.runtime.op.Caster;

public class Base64Encoder {

	private static final char[] ALPHABET = new char[] {
			'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
			'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
			'0','1','2','3','4','5','6','7','8','9','+','/'
	};
	private static final char PAD = '=';

	
	private static final Map<Character,Integer> REVERSE = new HashMap<Character,Integer>();
	static {
		for (int i=0; i<64; i++) {
			REVERSE.put(ALPHABET[i], i);
		}
		REVERSE.put('-', 62);
		REVERSE.put('_', 63);
		REVERSE.put(PAD, 0);
	}
    

	public static String encodeFromString(String data) {
		return encode(data.getBytes(CharsetUtil.UTF8));
	}
	

	
	/**
     * Translates the specified byte array into Base64 string.
     *
     * @param data the byte array (not null)
     * @return the translated Base64 string (not null)
     */
	public static String encode(byte[] data) {
		StringBuilder builder = new StringBuilder();
		for (int position=0; position < data.length; position+=3) {
			builder.append(encodeGroup(data, position));
		}
		return builder.toString();
	}
	
	

	
	
	
	
	////  Helper methods
	
	
	/**
	 * Encode three bytes of data into four characters.
	 */
	private static char[] encodeGroup(byte[] data, int position) {
		final char[] c = new char[] { '=','=','=','=' };
		int b1=0, b2=0, b3=0;
		int length = data.length - position;
		
		if (length == 0)
			return c;
		
		if (length >= 1) {
			b1 = (data[position])&0xFF;
		}
		if (length >= 2) {
			b2 = (data[position+1])&0xFF;
		}
		if (length >= 3) {
			b3 = (data[position+2])&0xFF;
		}
		
		c[0] = ALPHABET[b1>>2];
		c[1] = ALPHABET[(b1 & 3)<<4 | (b2>>4)];
		if (length == 1)
			return c;
		c[2] = ALPHABET[(b2 & 15)<<2 | (b3>>6)];
		if (length == 2)
			return c;
		c[3] = ALPHABET[b3 & 0x3f];
		return c;
	}

	public static String decodeAsString(String data) throws CoderException {
		return new String(decode(data),CharsetUtil.UTF8);
	}
	
	
	public static void main(String[] args) throws Exception {
		ResourceProvider p = ResourcesImpl.getFileResourceProvider();
		Resource f1 = p.getResource("/Users/mic/Test/test/webapps/ROOT/test/testcases/LDEV1393/originalImg.png");
		Resource f2 = p.getResource("/Users/mic/Test/test/webapps/ROOT/test/testcases/LDEV1393/image_code.base64");
		
		byte[] bytes = IOUtil.toBytes(f1);
		String str1=encode(bytes);
		String str2=IOUtil.toString(f2, "UTF-8");
		String str3=Caster.toBase64(bytes,"UTF-8");
		print.e(str1.length());
		print.e(str2.length());
		print.e(str3.length());
		
		int len=21;
		int tmp=4-(len-(len/4*4));
		print.e("tmp:"+tmp);
		print.e(str1.substring(str1.length()-40));
		print.e(str2.substring(str2.length()-40));
		decode(str1);
		bytes=decode(str2);
		
		Resource f3 = p.getResource("/Users/mic/test.png");
		IOUtil.write(f3, bytes);
	}
	
	/**
     * Translates the specified Base64 string into a byte array.
     *
     * @param s the Base64 string (not null)
     * @return the byte array (not null)
     * @throws CoderException 
     */
	public static byte[] decode(String data) throws CoderException {
		byte[] array = new byte[data.length()*3/4];
		char[] block = new char[4];
		int length = 0;
		data=data.trim();
		final int len=data.length();
		if(len==0) return new byte[0];// we accept a empty string as a empty binary!
		int tmp=4-(len-(len/4*4));
		if(tmp!=4) {
			for(int i=0;i<tmp;i++) {
				data+="=";
			}
			return decode(data);
    		//throw new CoderException((len % 4)+"can't decode the the base64 input string"+printString(data)+", because the input string has an invalid length");
		}
		
		
		
		for (int position=0; position < len; ) {
			int p;
			for (p=0; p<4 && position < data.length(); position++) {
				char c = data.charAt(position);
				if (!Character.isWhitespace(c)) {
					block[p] = c;
					p++;
				}
			}
			
			if (p==0)
				break;
			
			
			int l = decodeGroup(block, array, length);
			length += l;
			if (l < 3)
				break;
		}
		return Arrays.copyOf(array, length);
	}

    /**
	 * Decode four chars from data into 0-3 bytes of data starting at position in array.
	 * @return	the number of bytes decoded.
	 */
	private static int decodeGroup(char[] data, byte[] array, int position) throws CoderException {
		int b1, b2, b3, b4;
		
		try {
			b1 = REVERSE.get(data[0]);
			b2 = REVERSE.get(data[1]);
			b3 = REVERSE.get(data[2]);
			b4 = REVERSE.get(data[3]);
			
		} catch (NullPointerException e) {
			// If auto-boxing fails
			throw new CoderException("Illegal characters in the sequence to be "+
					"decoded: "+Arrays.toString(data));
		}
		
		array[position]   = (byte)((b1 << 2) | (b2 >> 4)); 
		array[position+1] = (byte)((b2 << 4) | (b3 >> 2)); 
		array[position+2] = (byte)((b3 << 6) | (b4)); 
		
		// Check the amount of data decoded
		if (data[0] == PAD)
			return 0;
		if (data[1] == PAD) {
			throw new CoderException("Illegal character padding in sequence to be "+
					"decoded: "+Arrays.toString(data));
		}
		if (data[2] == PAD)
			return 1;
		if (data[3] == PAD)
			return 2;
		
		return 3;
	}
	private static String printString(String s) {
		if(s.length()>50) return " ["+s.substring(0,50)+" ... truncated]";
		return  " ["+s+"]";
	} 
}