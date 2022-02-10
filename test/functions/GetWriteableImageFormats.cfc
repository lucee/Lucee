<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" skip="true" {

	public void function testWritableImageFormats() localmode="true"{
		var imageFormats = listToArray( getWriteableImageFormats() );
		var testFile = "";
		var testImage = imageNew( "", 100, 100, "rgb", "yellow" );
		for (var imageFormat in imageFormats ){
			try {
				// test if we can write a file in this image format (image format is based on file extension )
				testFile = getTempFile( getTempDirectory(), "image-test", imageFormat );
				imageWrite ( testImage, testFile );
				expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format/extension [#imageFormat#]" );
			} finally {
				if ( fileExists( testFile ) )
					fileDelete( testFile );
			}
		}
	}

} 
</cfscript>