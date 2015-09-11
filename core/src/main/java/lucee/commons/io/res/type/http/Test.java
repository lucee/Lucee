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
package lucee.commons.io.res.type.http;

import java.io.IOException;
import java.nio.charset.Charset;

import lucee.aprint;
import lucee.commons.io.IOUtil;

public class Test {
	public static void main(String[] args) throws IOException {
		HTTPResourceProvider p=(HTTPResourceProvider) new HTTPResourceProvider().init("https", null);
		HTTPResource res = (HTTPResource) p.getResource("https://www.google.com/reader/public/atom/user%2F11740182374692118732%2Flabel%2Fweb-rat.com");
		aprint.out("1");
		aprint.out(res.length());
		aprint.out("2");
		aprint.out(res.exists());
		aprint.out("3");
		aprint.out(res.isFile());
		aprint.out("4");
		aprint.out(IOUtil.toString(res,(Charset)null).length());
		
	}
}