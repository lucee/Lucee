/**
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi" {

	function run( testResults , testBox ) {

		describe( 'LDEV-567' , function() {

			describe( 'test encodeFor functions' , function() {
				// CSS
				it( 'encodeForCSS(".hi {aa:1}")' , function() {
					 
					expect(
						encodeForCSS(".hi {aa:1}")
					).toBe(
						"\2e hi\20 \7b aa\3a 1\7d "
					);
				});

				it( 'encodeForCSS(".hi {aa:1}",true)' , function() {
					 
					expect(
						encodeForCSS(".hi {aa:1}",true)
					).toBe(
						"\2e hi\20 \7b aa\3a 1\7d "
					);
				});


				// DN
				it( 'encodeForDN("####")' , function() {
					 
					expect(
						encodeForDN("####")
					).toBe(
						"\####"
					);
				});

				it( 'encodeForDN("####",true)' , function() {
					 
					expect(
						encodeForDN("####",true)
					).toBe(
						"\####"
					);
				});

				// HTML
				it( 'encodeForHTML("<br/>")' , function() {
					 
					expect(
						encodeForHTML("<br/>")
					).toBe(
						"&lt;br&##x2f;&gt;"
					);
				});

				it( 'encodeForHTML("<br/>",true)' , function() {
					 
					expect(
						encodeForHTML("<br/>",true)
					).toBe(
						"&lt;br&##x2f;&gt;"
					);
				});

				// HTML attrs
				it( 'encodeForHTMLAttribute("<q"">")' , function() {
					 
					expect(
						encodeForHTMLAttribute("<q"">")
					).toBe(
						"&lt;q&quot;&gt;"
					);
				});

				it( 'encodeForHTMLAttribute("<q"">",true)' , function() {
					 
					expect(
						encodeForHTMLAttribute("<q"">",true)
					).toBe(
						"&lt;q&quot;&gt;"
					);
				});

				// JS
				it( 'encodeForJavaScript(...)' , function() {
					 
					expect(
						encodeForJavaScript("""'")
					).toBe(
						"\x22\x27"
					);
				});

				it( 'encodeForJavaScript(...,true)' , function() {
					 
					expect(
						encodeForJavaScript("""'",true)
					).toBe(
						"\x22\x27"
					);
				});

				// URL
				it( 'encodeForURL("&s=")' , function() {
					 
					expect(
						encodeForURL("&s=")
					).toBe(
						"%26s%3D"
					);
				});

				it( 'encodeForURL("&s=",true)' , function() {
					 
					expect(
						encodeForURL("&s=",true)
					).toBe(
						"%26s%3D"
					);
				});

				// XML
				it( 'encodeForXML("<br/>")' , function() {
					 
					expect(
						encodeForXML("<br/>")
					).toBe(
						"&##x3c;br&##x2f;&##x3e;"
					);
				});

				it( 'encodeForXML("<br/>",true)' , function() {
					 
					expect(
						encodeForXML("<br/>",true)
					).toBe(
						"&##x3c;br&##x2f;&##x3e;"
					);
				});

				// XML Attrs
				it( 'encodeForXMLAttribute("""")' , function() {
					 
					expect(
						encodeForXMLAttribute("""")
					).toBe(
						"&##x22;"
					);
				});

				it( 'encodeForXMLAttribute("""",true)' , function() {
					 
					expect(
						encodeForXMLAttribute("""",true)
					).toBe(
						"&##x22;"
					);
				});

				// XPath
				it( 'encodeForXPath("/")' , function() {
					 
					expect(
						encodeForXPath("/")
					).toBe(
						"&##x2f;"
					);
				});

				it( 'encodeForXPath("/",true)' , function() {
					 
					expect(
						encodeForXPath("/",true)
					).toBe(
						"&##x2f;"
					);
				});

				
				

			});

		});

	}

}