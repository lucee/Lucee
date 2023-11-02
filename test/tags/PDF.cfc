<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	public void function testPDFParam(){
		try {
			document format="pdf" pagetype="A4" orientation="portrait" filename="test1.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			assertTrue(isPDFFile("test1.pdf"));
			document format="pdf" pagetype="A4" orientation="portrait" filename="test2.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			assertTrue(isPDFFile("test2.pdf"));

			pdf action="merge"  destination="test0.pdf" overwrite="yes" {
				pdfparam source="test1.pdf" pages="1";
				pdfparam source="test2.pdf" pages="1";
			}
			assertTrue(isPDFFile("test0.pdf"));

			pdf action="read" name="local.myPDF" source="test0.pdf" ;
			assertEquals(2,myPDF.TotalPages);
		}
		finally {
			if(fileExists("test1.pdf"))fileDelete("test1.pdf");
			if(fileExists("test2.pdf"))fileDelete("test2.pdf");
			if(fileExists("test0.pdf"))fileDelete("test0.pdf");
		}
	}

	public void function testPDFProtect(){
		try {
			document format="pdf" pagetype="A4" orientation="portrait" filename="test-protect.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			assertTrue(isPDFFile("test-protect.pdf"));

			pdf action="protect" encrypt="AES_128" source="test-protect.pdf" newUserPassword="PDFPassword";
		}
		finally {
			if(fileExists("test-protect.pdf"))fileDelete("test-protect.pdf");
		}
	}


	public void function testPDFOpen(){
		try {
			document format="pdf" pagetype="A4" orientation="portrait" filename="test-protect2.pdf" overwrite="true" {
				echo("<p>This is where mickey mouse lives</p>");
			}
			pdf action="protect" encrypt="AES_128" source="test-protect2.pdf" newUserPassword="PDFPassword";
			cfpdf(action="open" source="test-protect2.pdf" password="PDFPassword" destination="test-unprotect.pdf" overwrite="yes");
			cfpdf(action="read" source="test-unprotect.pdf" name="local.pdf");
		}
		finally {
			if(fileExists("test-protect2.pdf"))fileDelete("test-protect2.pdf");
			if(fileExists("test-unprotect.pdf"))fileDelete("test-unprotect.pdf");
		}
	}
} 
</cfscript>