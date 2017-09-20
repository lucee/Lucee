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

import org.apache.axis.encoding.ser.SimpleDeserializer;

 class StringDeserializer extends SimpleDeserializer {
	/*private static final Map<Pattern, String> replacements;
	static {
		replacements = new HashMap<Pattern, String>();
		for (char c = 0x00; c <= 0x1F; ++c) {
			replacements.put(Pattern.compile(StringSerializer.xmlCodeForChar(c)), Character.toString(c));
		}
		replacements.put(Pattern.compile(StringSerializer.xmlCodeForChar((char) 0x7F)), Character.toString((char) 0x7F));
	}*/

	public StringDeserializer(Class javaType, QName xmlType) {
		super(javaType, xmlType);
	}

	/*@Override
	public Object makeValue(String source) throws Exception {
		String val = source; 

		for (Pattern pattern : replacements.keySet()) {
			val = pattern.matcher(val).replaceAll(replacements.get(pattern));
		}

		return val; 
	}*/
}