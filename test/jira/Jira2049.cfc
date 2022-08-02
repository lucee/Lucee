<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql,orm" {

	//public function setUp(){}

	public void function testMySql()  skip="notHasMySQLCredentials"{
		//if(!hasMySQLCredentials()) return;
		local.uri=createURI("Jira2049/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		res=evaluate(trim(result.fileContent));

		assertTrue(isQuery(res.book));
		assertEquals(1,res.book.bookId);
		assertEquals(1,res.book.authorID);

		assertTrue(isQuery(res.author));
		assertEquals(1,res.author.authorID);
		assertEquals("Susi",res.author.authorName);
	}

	public void function testH2_1(){
		local.uri=createURI("Jira2049.1/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("",trim(result.fileContent));
	}

	public void function testH2_2(){
		local.uri=createURI("Jira2049.2/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("",trim(result.fileContent));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	public boolean function notHasMySQLCredentials() {
		return (structCount(server.getDatasource("mysql")) eq 0);	
	}
	
} 
</cfscript>