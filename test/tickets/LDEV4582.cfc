component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	
	function beforeAll(){
		variables.uri = createURI("LDEV4582");
		variables.logDir = getDirectoryFromPath(getCurrentTemplatepath()) & "LDEV4582#server.separator.file#logs";
		systemOutput( "", true);
		cleanup();
	}

	function afterAll(){
		cleanup();
	}

	private function cleanup(){
		if ( directoryExists ( variables.logDir ) )
			directoryDelete( variables.logDir )
	}

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4582 Invalid mappings are ignored by expandpath", function() {
			it( title="mapping dir doesn't exist", body=function( currentSpec ) {
				cleanup();
				var result = _InternalRequest(
					template : "#uri#/index.cfm",
					url: {
						name: "dir missing"
					}
				);
				expect( result.fileContent.trim() ).toBe( logdir );
			});

			it( title="mapping dir already exists", body=function( currentSpec ) {
				if ( !directoryExists ( logDir ) )
					directoryCreate( logDir );

				var result = _InternalRequest(
					template : "#uri#/index.cfm",
					url: {
						name: "mapping dir exists"
					}
				);
				expect( result.fileContent.trim() ).toBe( logDir );
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
