component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function beforeAll(){
		variables.uri = createURI("LDEV2320");
		if(!directoryExists(uri&'\zipOne\sub')){
			directorycreate(uri&'\zipOne\sub')
		}
		if(!directoryExists(uri&'\zipTwo')){
			directorycreate(uri&'\zipTwo')
		}
		filewrite(uri&'\zipTwo\ziptwo.txt','I am from text file');
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2320", function(){
			/*it( title = "complete empty zip", body = function( currentSpec ) {
				zip action = 'zip' source = uri&'\zipOne\sub' file = uri&'zipempty.zip';
				expect(iszipfile(uri&'zipempty.zip')).tobe(true);
			});*/
			
			it( title = "Zipping empty folder", body = function( currentSpec ) {
				zip action = 'zip' source = uri&'\zipOne' file = uri&'zipfolder.zip';
				expect(iszipfile(uri&'zipfolder.zip')).tobe(true);
			});

			it( title = "Zipping a folder with file", body = function( currentSpec ) {
				zip action = 'zip' source = uri&'\zipTwo' file = uri&'\zipfile.zip';
				expect(iszipfile(uri&'\zipfile.zip')).tobe(true);
			});
		});
	}

	function afterAll(){
		if(directoryExists(uri)){
			directoryDelete(uri,true);
		}
	}

	private string function createURI(string calledName){
		var curr=getDirectoryFromPath(getCurrenttemplatepath())
		return curr&"/"&calledName&"/";
	}
}	