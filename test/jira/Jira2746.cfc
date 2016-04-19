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
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	public void function testRegular() localmode="modern"{
		meta=getmetaData(localmodeOn);
		assertEquals("modern",meta.localmode);
		meta=getmetaData(localmodeOff);
		assertEquals("classic",meta.localmode);
		meta=getmetaData(localmodeNone);
		assertEquals(true,isNull(meta.localmode));
		
	}
	
	public void function testSerializedAndEvaluatedAgain() localmode="modern"{
		
		meta=getmetaData(objectLoad(objectSave(localmodeOn)));
		assertEquals("modern",meta.localmode);
		meta=getmetaData(objectLoad(objectSave(localmodeOff)));
		assertEquals("classic",meta.localmode);
		
	}
	
	private void function localmodeOn() localmode="modern"{}
	private void function localmodeOff() localmode="classic"{}
	private void function localmodeNone() {}
} 
</cfscript>