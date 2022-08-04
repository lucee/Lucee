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
	//variables.base64="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";
	
	public function setUp() localmode="true"{
		variables.image=imagenew("../artifacts/image.jpg");	
	}

	public void function testImage() localmode="true"{
		ImageCrop(image,10,10,100,100);
	} 

} 
</cfscript>