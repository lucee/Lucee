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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	//public function setUp(){}

	public void function testAppCFC(){
		_testApp("LDEV0809/appcfc/index.cfm");
	}

	public void function testAppCFM(){
		_testApp("LDEV0809/appcfm/index.cfm");
	}

	private void function _testApp(string path){
		local.result=_InternalRequest(
			template:createURI(path));
		
		local.arr=result.headers['Set-Cookie'];
		local.str='';
		loop array=arr item="local.entry" {
			systemOutput(entry,1,1);
			if(find('cfid=',entry)) str=entry;
		}
		systemOutput(str,1,1);
		assertTrue(len(str)>0);
		local.sct=toStruct(str);
		assertFalse(structKeyExists(sct,'HTTPOnly'));
		assertTrue(structKeyExists(sct,'Secure'));
		assertTrue(structKeyExists(sct,'Domain'));
		assertEquals('.domain.com',sct.domain);
		assertTrue(structKeyExists(sct,'Expires'));
		local.res=parseDateTime(sct.expires);
		local.d1=dateAdd('s',10,now());
		local.d2=dateAdd('s',11,now());
		assertTrue(d1==res || d2==res);
		//assertTrue(dateAdd('s',10,now())==res || dateAdd('s',11,now())==res);
	}


	public void function testAppCFCOriginal(){
		_testAppOriginal("LDEV0809/appcfc/index.cfm");
	}
	public void function testAppCFMOriginal(){
		_testAppOriginal("LDEV0809/appcfm/index.cfm");
	}

	private void function _testAppOriginal(path){
		local.result=_InternalRequest(
			template:createURI(path),
			urls:{original:true});
		
		local.arr=result.headers['Set-Cookie'];
		local.str='';
		loop array=arr item="local.entry" {
			if(find('cfid=',entry)) str=entry;
		}
		assertTrue(len(str)>0);
		local.sct=toStruct(str);
		assertTrue(structKeyExists(sct,'HTTPOnly'));
		assertFalse(structKeyExists(sct,'Secure'));
		assertFalse(structKeyExists(sct,'Domain'));
		assertTrue(structKeyExists(sct,'Expires'));
		assertTrue(dateAdd('d',10,now())<parseDateTime(sct.expires));
	}

	private struct function toStruct(string str){
		local.arr=listToArray(str,';');
		local.sct={};
		loop array=arr item="local.entry" {
			sct[trim(listFirst(entry,'='))]=trim(listLast(entry,'='));
		}
		return sct;
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
</cfscript>