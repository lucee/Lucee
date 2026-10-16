<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {
	

	public void function test() localMode="modern" {
		if (noOrm()) return;
		local.uri=createURI("Jira3049/index.cfm");
		local.result=_InternalRequest(uri);
		
		assertEquals("",trim(result.filecontent));
		assertEquals(200,result.status);
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function noOrm() {
		return ( structCount( server.getTestService("orm") ) eq 0 );
	}

}
</cfscript>