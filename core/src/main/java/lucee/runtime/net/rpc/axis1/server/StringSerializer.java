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
package lucee.runtime.net.rpc.axis1.server;

import javax.xml.namespace.QName;

import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.ser.SimpleSerializer;

 class StringSerializer extends SimpleSerializer {
	/*protected static String xmlCodeForChar(char c) {
		StringBuilder buff = new StringBuilder();
		buff.append("&#x");
		buff.append(StringUtils.leftPad(Integer.toHexString(c), 4, "0"));
		buff.append(";"); 
		
		return buff.toString();
	}*/

	public StringSerializer(Class javaType, QName xmlType) {
		super(javaType, xmlType);
	}

	public String getValueAsString(Object value, SerializationContext context) {
		//return escapeNonPrintableChars(super.getValueAsString(value, context));
		return super.getValueAsString(value, context);
	}

	/*private String escapeNonPrintableChars(String val) {
		StringBuilder buff = new StringBuilder();
		for (int idx = 0; idx < val.length(); ++idx) {
			char c = val.charAt(idx);
			buff.append(charIsNonPrintable(c) ? xmlCodeForChar(c) : c);
		}
		
		return buff.toString(); 
	}

	private boolean charIsNonPrintable(char c) {
		//0x00 to 0x1F and 0x7F are ASCII control characters
		return c < 0x1F || c == 0x7F;
	}*/
}
