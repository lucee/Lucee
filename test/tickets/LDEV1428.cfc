 <cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"	{

	public void function testDelete(){
		local.uri=createURI("LDEV1428/test.cfm");
		local.result=_InternalRequest(template:uri, forms:{Scene=1});
		assertEquals(200,result.status);
		assertEquals("true|",trim(result.fileContent));
	}

	public void function testDeleteByID(){
		local.uri=createURI("LDEV1428/test.cfm");
		local.result=_InternalRequest(template:uri, forms:{Scene=2});
		assertEquals(200,result.status);
		assertEquals("true|",trim(result.fileContent));
	}

	public void function testDeleteWhere(){
		local.uri=createURI("LDEV1428/test.cfm");
		local.result=_InternalRequest(template:uri, forms:{Scene=3});
		assertEquals(200,result.status);
		assertEquals("0|",trim(result.fileContent));
	}

	public void function testAllFunctions(){
		local.uri=createURI("LDEV1428/test.cfm");
		local.result=_InternalRequest(template:uri, forms:{Scene=4});
		assertEquals(200,result.status);
		assertEquals("true|true|0|",trim(result.fileContent));
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
</cfscript>
