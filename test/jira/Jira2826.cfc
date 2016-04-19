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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	
	function testNoFilter() {

		directory action="list" directory="#getDir()#" name="qDir";
		assertTrue(qDir.recordCount == 5);
	} 
	
	function testSimpleFilter() {
		
		directory action="list" directory="#getDir()#" name="qDir" filter="*.html";
		assertTrue(qDir.recordCount == 1);
	} 
	
	function testDefaultDelimiter() {
		
		directory action="list" directory="#getDir()#" name="qDir" filter="*.html|*.txt";
		assertTrue(qDir.recordCount == 4);
	} 
	
	function testBadDelimiter() {
		
		directory action="list" directory="#getDir()#" name="qDir" filter="*.html;*.txt";
		assertTrue(qDir.recordCount == 0);
	} 

	function testPassedDelimiter() {
		
		directory action="list" directory="#getDir()#" name="qDir" filter="*.html;*.txt" filterDelimiters="|;,";
		assertTrue(qDir.recordCount == 4);
	} 


	function testUDFFilter() {		
		
		directory action="list" directory="#getDir()#" name="qDir" filter="#function (path) { return listLast(arguments.path, '.') == "txt"; }#";
		assertTrue(qDir.recordCount == 3);
	} 
	

	private function getDir() {

		return listFirst( getCurrentTemplatePath(), '.' ) & "/src";
	}

}