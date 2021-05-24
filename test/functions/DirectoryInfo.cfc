component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
		variables.uri = createURI( "LDEV3294" );
		directoryCreate( uri );
	}	

	function afterAll(){
		directorydelete( uri , true );
	}

	function run( testResults , testBox ) {
		describe( "test case for directoryInfo", function() {
			it( title = "Checking with directoryInfo function", body = function( currentSpec ) {
				local.info = directoryinfo( uri );
				expect( info.directoryName).toBe( listLast( uri , "/\" ) );
				expect( structKeyExists( info , "directoryCreated") ).toBeTrue();
				expect( structKeyExists( info , "dateLastModified") ).toBeTrue();
				expect( info.isReadable ).toBeTrue();
			});
		});

		describe( "test case for cfdirectory action=info", function() {
			it(title = "Checking with cfdirectory action=info", body = function( currentSpec ) {
				```
					<cfdirectory action="info" directory="#uri#" name="local.info">
				```
				expect( info.directoryName).toBe( listLast( uri , "/\" ) );
				expect( structKeyExists( info , "directoryCreated") ).toBeTrue();
				expect( structKeyExists( info , "dateLastModified") ).toBeTrue();
				expect( info.isReadable ).toBeTrue();
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI&""&calledName;
	}
}