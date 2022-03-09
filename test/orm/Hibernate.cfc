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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	//public function setUp(){}

	public void function testSimple(){
		local.uri=createURI("simple/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("",trim(result.fileContent));
	}

	public void function testClearSession(){
		local.uri=createURI("clearSession/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("",trim(result.fileContent));
	}

	public void function testMany2One(){
		local.uri=createURI("many2one/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);

		var ref=evaluate(trim(result.fileContent));
		if(isArray(ref))ref=ref[1];

		
		assertTrue(isValid('component',ref));
		assertEquals(1,ref.getId());
		
		var code=ref.getCode();
		assertTrue(isValid('component',code));
		assertEquals(1,code.getId());
		assertEquals("a",code.getCode());

	}

	public void function testMany2Many(){
		local.uri=createURI("many2many/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("moduleLangs:1;2;Tags:1;2;",trim(result.fileContent));
	}
	public void function testTransactionSave(){
		local.uri=createURI("transSave/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("1",trim(result.fileContent));
	}
	public void function testTransactionSaveExCommit(){
		local.uri=createURI("transSaveExCommit/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("1",trim(result.fileContent));
	}
	public void function testTransactionSaveFlush(){
		local.uri=createURI("transSaveFlush/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("1",trim(result.fileContent));
	}
	/*public void function testTransactionSavepoint(){
		local.uri=createURI("transSavepoint/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("1",trim(result.fileContent));
	}*/
	public void function testTransactionRollback(){
		local.uri=createURI("transRollback/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals("0",trim(result.fileContent));
	}

	// ormSettings dialects tests
	public void function testDialectMYSQL(){
		local.uri=createURI("testDialects/testMysql/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals(true, findNoCase("mysql", result.fileContent) > 0);
	}

	public void function testDialectMSSQL(){
		local.uri=createURI("testDialects/testMssql/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals(true, findNoCase("SQLServer", result.fileContent) > 0);
	}

	public void function testDialectPostgres(){
		local.uri=createURI("testDialects/testPostgres/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals(true, findNoCase("postgres", result.fileContent) > 0);
	}
	
	public void function testDialectH2(){
		local.uri=createURI("testDialects/testH2/index.cfm");
		local.result=_InternalRequest(uri);
		assertEquals(200,result.status);
		assertEquals(true, findNoCase("H2", result.fileContent) > 0);
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
</cfscript>