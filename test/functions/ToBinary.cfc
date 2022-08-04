<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	public void function test() localmode="true"{
		// load image
		img=imageNew("../artifacts/image.jpg");
		assertTrue(isImage(img));
		
		// to binary
		barr=toBinary(img);
		assertTrue(isBinary(barr));
		
		// to base 64
		b64=ToBase64(barr);
		
		// back to binary
		barr2 = toBinary(b64);
		assertTrue(isBinary(barr2));

		// back to image
		img2=imageNew(barr2);
		assertTrue(isImage(img2));

	} 

} 
</cfscript>