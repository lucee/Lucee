component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2872/");
		if(!directoryExists("#uri#/filewrite/")) {
			directorycreate("#uri#/filewrite/");
			filewrite(file="#uri#/filewrite/fileExists.txt", data="Content that you need to write.");
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2872", function() {
			it(title = "cffile action=write with nameconflict=makeunique", body = function( currentSpec ) {
				```
				<cffile action="write" file="#uri#/filewrite/fileExists.txt" output="overwrite#randrange(0,100)#" nameconflict="makeunique">

				```
				len = arraylen(directorylist("#uri#/filewrite/"));
				expect(len).toBeGT(1, len);
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