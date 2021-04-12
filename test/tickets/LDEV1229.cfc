component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {

		describe( title="Test suite for LDEV-1229 with mysql",  skip=checkMySqlEnvVarsAvailable(), body=function() {
			it(title="checking property tag, with the attribute cascade = 'all-delete-orphan' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1229/index.cfm");
				var result = _InternalRequest(
					template:uri
					,urls:{db:'mysql'}
				);
				expect(result.filecontent.trim()).toBe(1);
			});
		});

		describe( title="Test suite for LDEV-1229 with h2",  body=function() {
			it(title="checking property tag, with the attribute cascade = 'all-delete-orphan' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1229/index.cfm");
				var result = _InternalRequest(
					template:uri
					,urls:{db:'h2'}
				);
				expect(result.filecontent.trim()).toBe(1);
			});
		});


	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		var mysql = server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}
}
