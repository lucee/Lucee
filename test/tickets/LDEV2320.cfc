component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function beforeAll(){
		variables.uri = createURI("LDEV2320");
		if(!directoryExists(uri&'\zipOne')){
			directorycreate(uri&'\zipOne')
		}
		if(!directoryExists(uri&'\zipTwo')){
			directorycreate(uri&'\zipTwo')
		}
		filewrite(uri&'\zipTwo\ziptwo.txt','I am from text file');
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2320", function(){
			it( title = "Zipping empty folder throws an errors", body = function( currentSpec ) {
				zip action = 'zip' source = uri&'\zipOne' file = uri&'zipfolder.zip';
				expect(iszipfile(uri&'ziponefolder.zip')).tobe(true);
			});

			it( title = "Zipping a folder with file", body = function( currentSpec ) {
				zip action = 'zip' source = uri&'\zipTwo' file = uri&'\ziptwofolder.zip';
				expect(iszipfile(uri&'\ziptwofolder.zip')).tobe(true);
			});
		});
	}

	function afterAll(){
		if(directoryExists(uri)){
			directoryDelete(uri,true);
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}	