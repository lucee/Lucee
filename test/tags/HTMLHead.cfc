<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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


	public void function testAppendText(){
		local.uri=createURI("HTMLHead/append-text.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{first}{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}
	public void function testAppendBody(){
		local.uri=createURI("HTMLHead/append-body.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{first}{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}

	public void function testWriteText(){
		local.uri=createURI("HTMLHead/write-text.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}
	public void function testWriteBody(){
		local.uri=createURI("HTMLHead/write-body.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}
	public void function testResetText(){
		local.uri=createURI("HTMLHead/reset-text.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}
	public void function testResetBody(){
		local.uri=createURI("HTMLHead/reset-body.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{second}</head><body></body></html>',
			trim(result.filecontent)
		);
	}
	public void function testRead(){
		local.uri=createURI("HTMLHead/read.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(
			'<html><head>{first}</head><body>{first}</body></html>',
			trim(result.filecontent)
		);
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
} 
</cfscript>