component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV3614");
		if ( !directoryExists( uri ) ) {
			directoryCreate( uri );
		}
	}

	function testDirectoryListWithAccents( ) {
		fileWrite( uri & "/fileSimpleName.txt","" );
		fileWrite( uri & "/fileWithAccent_é.txt","" );
		directoryCreate( uri & "/dirSimpleName" );
		directoryCreate( uri & "/dirWithAccent_à");

		var all = directoryList( uri, false, "query", "", "", "all" );
		expect( all.recordcount ).toBe( 4 );
		var dir = directoryList( uri, false, "query", "", "", "dir" );
		expect( dir.recordcount ).toBe( 2 );
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	function afterAll() {
		if ( directoryExists(uri) ) {
			directoryDelete( uri, true );
		}
	}
}