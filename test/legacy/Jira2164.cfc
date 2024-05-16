/**
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
 **/
component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"	{

	function testZipNoFilter() {
		zip action="zip" source="#getDir()#/src/" file="#getDir()#/testZipNoFilter.zip";

		zip action="list" file="#getDir()#/testZipNoFilter.zip" name="local.qZip";
		assertEquals(4,qZip.recordCount);
	}

	function testZipPatternFilter() {
		zip action="zip" source="#getDir()#/src/" file="#getDir()#/testZipPatternFilter.zip" filter="*.html";

		zip action="list" file="#getDir()#/testZipPatternFilter.zip" name="local.qZip";
		assertEquals(2,qZip.recordCount);
	}

	function testZipUDFFilter() {
		zip action="zip" source="#getDir()#/src/" file="#getDir()#/testZipUDFFilter.zip" filter="#function(name) { trace(name); return listLast(name, '.') == "html" }#";

		zip action="list" file="#getDir()#/testZipUDFFilter.zip" name="local.qZip";
		assertEquals(2,qZip.recordCount);
	}

	function testZipParamUDFFilter() {
		zip action="zip" file="#getDir()#/testZipParamUDFFilter.zip" {
			zipparam source="#getDir()#/src/" filter="#function(name) { trace(name); return listLast(name, '.') == "html" }#";
		}

		zip action="list" file="#getDir()#/testZipParamUDFFilter.zip" name="local.qZip";
		assertEquals(2,qZip.recordCount);
	}

	function testListUDFFilter() {
		zip action="zip" source="#getDir()#/src/" file="#getDir()#/testListUDFFilter.zip";

		zip action="list" file="#getDir()#/testListUDFFilter.zip" name="local.qZip" filter="#function(name) { trace("LIST: "&name); return listLast(name, '.') == "html" }#";
		assertEquals(2,qZip.recordCount);
	}


	function testDeleteUDFFilter() {

		zip action="zip" source="#getDir()#/src/" file="#getDir()#/testDeleteUDFFilter.zip";
		zip action="list" file="#getDir()#/testDeleteUDFFilter.zip" name="local.qZip1";
		assertEquals(4,qZip1.recordCount);

		zip action="delete" file="#getDir()#/testDeleteUDFFilter.zip" filter="#function(name) { trace("DELETE: "&name); return listLast(name, '.') == "html" }#";
		zip action="list" file="#getDir()#/testDeleteUDFFilter.zip" name="local.qZip2";
		assertEquals(2,qZip2.recordCount);
	}


	private function getDir() {

		return listFirst( getCurrentTemplatePath(), '.' );
	}

	public function setUp(){

	}

	public function beforeTests() {
		var dir=getDir();
		if(directoryExists(dir))directoryDelete(dir,true);
		directory directory="#dir#/src" action="create" mode="777";
		file action="write" file="#dir#/src/test1.txt"{echo("hello1");}
		file action="write" file="#dir#/src/test2.html"{echo("hello1");}
		
		directory directory="#dir#/src/sub" action="create" mode="777";
		file action="write" file="#dir#/src/sub/test3.txt"{echo("hello3");}
		file action="write" file="#dir#/src/sub/test4.html"{echo("hello3");}
	}

	public function afterTests() {
		directoryDelete("#getDir()#",true);
	}
}