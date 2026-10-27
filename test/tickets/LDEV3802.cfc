component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri=createURI("LDEV3802");
		variables.file = GetDirectoryFromPath(getcurrentTemplatepath())&'LDEV3802\result.txt';
		afterAll();
	}

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3802", function() {	
			it( title="Calling CFC using relative path inside the long running thread", body=function( currentSpec ) {
				_InternalRequest(
					template : "#uri#\LDEV3802.cfm"
				);
				// the thread outlives the request (it sleeps first), so wait for it to write the result instead of a fixed sleep
				var waited = 0;
				while ( !fileExists( file ) || !len( trim( fileRead( file ) ) ) ) {
					if ( waited >= 10000 ) break;
					sleep( 50 );
					waited += 50;
				}
				expect( fileExists( file ) ).toBeTrue( "thread did not write [#file#] within #waited#ms" );
				expect(trim(fileRead(file))).tobe("success");
			});	
		});
	}

	function afterAll() {
		if (fileExists(file)) fileDelete(file);
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
