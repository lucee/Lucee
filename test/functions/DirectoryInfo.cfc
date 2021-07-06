component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI( "LDEV3294" );
		directoryCreate( uri );
		variables.DicFuncInfo = directoryinfo( uri );
		directory action="info" directory="#uri#" name="variables.DicTagInfo";
	}
	
	function isLinux() {
		return server.os.name != "Linux";
	}

	function afterAll(){
		directorydelete( uri , true );
	}

	function run( testResults , testBox ) {
		describe( "test case for directoryInfo", function() {
			it( title = "Checking with directoryInfo function", body = function( currentSpec ) {
				expect( DicFuncInfo.directoryName).toBe( listLast( uri , "/\" ) );
				expect( structKeyExists( DicFuncInfo , "directoryCreated") ).toBeTrue();
				expect( structKeyExists( DicFuncInfo , "dateLastModified") ).toBeTrue();
				expect( DicFuncInfo.isReadable ).toBeTrue();
			});
			it( title = "Checking mode key on linux directoryInfo function", skip="#isLinux()#", body = function( currentSpec ) {
				expect( structKeyExists( DicFuncInfo, "mode" ) ).toBeTrue();
			});
		});

		describe( "test case for cfdirectory action=info", function() {
			it(title = "Checking with cfdirectory action=info", body = function( currentSpec ) {
				expect( DicTagInfo.directoryName).toBe( listLast( uri , "/\" ) );
				expect( structKeyExists( DicTagInfo , "directoryCreated") ).toBeTrue();
				expect( structKeyExists( DicTagInfo , "dateLastModified") ).toBeTrue();
				expect( DicTagInfo.isReadable ).toBeTrue();
			});
			it( title = "Checking mode key on linux cfdirectory action=info", skip="#isLinux()#", body = function( currentSpec ) {
				expect( structKeyExists( DicTagInfo, "mode" ) ).toBeTrue();
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI&""&calledName;
	}
}