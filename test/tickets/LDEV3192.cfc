component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV3192");
		if(!directoryExists(uri)) {
			directorycreate(uri);
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3192", function() {
			it(title = "directorycreate() with argument ignoreexists", body = function( currentSpec ) {
				directoryCreate( uri, true, true);
				expect(directoryExists(uri)).tobe(true);
			});

			it(title = "cfdirectory action=create with attribute nameconflict=skip", body = function( currentSpec ) {
				try {
					cfdirectory( action="create", directory=uri, nameconflict="skip");
					res = directoryExists(uri);
				}
				catch(any e) {
					res = e.message;
				}
				expect(res).tobe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	function afterAll() {
		if(directoryExists(uri)) {
			directorydelete(uri,true);
		}
	}
}