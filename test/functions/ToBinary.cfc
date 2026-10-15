<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.*
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

	// valid base64 for "Hello World" with "#" in place of the "=" padding
	variables.sloppy = "SGVsbG8gV29ybGQ##";
	variables.valid = "SGVsbG8gV29ybGQ=";

	public void function testPreciseDefaultLenient() localmode="true" {
		assertEquals( "Hello World", toString( toBinary( sloppy ) ) );
		assertEquals( "Hello World", toString( toBinary( data=sloppy ) ) );
		assertEquals( "Hello World", toString( toBinary( data=sloppy, precise=false ) ) );
		assertEquals( "Hello World", toString( toBinary( sloppy, "", false ) ) );
	}

	public void function testPreciseValid() localmode="true" {
		assertEquals( "Hello World", toString( toBinary( data=valid, precise=true ) ) );
		assertEquals( "Hello World", toString( toBinary( valid, "", true ) ) );
	}

	public void function testPreciseInvalidThrows() localmode="true" {
		expect( function() {
			toBinary( data=sloppy, precise=true );
		}).toThrow( regex="invalid character \[##\]" );
		expect( function() {
			toBinary( sloppy, "", true );
		}).toThrow( regex="invalid character \[##\]" );
	}

	public void function testPreciseRejectsWhitespace() localmode="true" {
		wrapped = "SGVsbG8g" & chr( 10 ) & "V29ybGQ=";
		assertEquals( "Hello World", toString( toBinary( wrapped ) ) );
		expect( function() {
			toBinary( data=wrapped, precise=true );
		}).toThrow();
	}

	public void function testPreciseBinaryPassthrough() localmode="true" {
		bin = charsetDecode( "Hello World", "utf-8" );
		assertEquals( "Hello World", toString( toBinary( data=bin, precise=true ) ) );
	}

	public void function testPreciseWithCharsetThrows() localmode="true" {
		expect( function() {
			toBinary( data="abc", charset="utf-8", precise=true );
		}).toThrow( regex="\[precise\].*\[charset\]" );
		assertEquals( "abc", toString( toBinary( data="abc", charset="utf-8", precise=false ) ) );
	}

} 
</cfscript>