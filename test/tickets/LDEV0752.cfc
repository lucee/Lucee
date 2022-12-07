component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql,orm" {
	function run( testResults , testBox ) {
		if(hasCredentials()) {
			describe( "Testing ORMReload with multiple foreign key props to point single column", function() {
				it('Second property without insert="false" & update="false"',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals('Repeated column in mapping for entity: SupportTicket column: companyUserID (should be mapped with insert="false" update="false")',result.filecontent.trim());
				});
				it('Second property with insert="false" & update="false"',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				});
				it('Removed second property',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=3}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				});
			});
		}
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		return structCount(server.getDatasource("mysql"));
	}
}