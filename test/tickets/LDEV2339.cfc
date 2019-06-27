component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV2339\";
		if(!directoryExists(dir)) {
			directoryCreate(dir&'path1');
			directoryCreate(dir&'path2');
		}
		fileWrite(dir&"path1\newfile.txt","This is new file");
		FileSetAttribute(dir&"path1\newfile.txt","readonly");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2339", body=function() {
			it( title='FileMove() is unable to move a file with the read-only file attribute',body=function( currentSpec ) {
				FileMove(dir&"path1\newfile.txt",dir&"path2");
				assertEquals("YES",FileExists(dir&"path2\newfile.txt"));
				assertEquals("NO",FileExists(dir&"path1\newfile.txt"));
			});
		});
	}

	function afterAll() {
		if(directoryExists(dir)) {
			directoryDelete(dir,true);
		}
	}
}