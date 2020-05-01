component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV2349\";
		if(!directoryexists(dir)) {
			directorycreate(dir&'sourcePath');
			directorycreate(dir&'desPath');
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2349", body=function() {
			it( title="FileCopy- Destination file losses the original access mode",body=function( currentSpec ) {
				filewrite(dir&"sourcePath\newfile.txt","This is new file");
				filesetattribute(dir&"sourcePath\newfile.txt","readonly");
				filecopy(dir&"sourcePath\newfile.txt",dir&"desPath");
				assertEquals("FALSE",getfileinfo(dir&"sourcePath\newfile.txt").canwrite);
				assertEquals("FALSE",getfileinfo(dir&"desPath\newfile.txt").canwrite);
			});

			it( title="FileCopy- Destination file losses the original access mode",body=function( currentSpec ) {
				filewrite(dir&"sourcePath\newfile1.txt","This is new file");
				filesetattribute(dir&"sourcePath\newfile1.txt","hidden");
				filecopy(dir&"sourcePath\newfile1.txt",dir&"desPath");
				assertEquals("TRUE",getfileinfo(dir&"sourcePath\newfile1.txt").ishidden);
				assertEquals("TRUE",getfileinfo(dir&"desPath\newfile1.txt").ishidden);
			});

			it( title="FileCopy- Destination file losses the original access mode",body=function( currentSpec ) {
				filewrite(dir&"sourcePath\newfile2.txt","This is new file");
				filesetattribute(dir&"sourcePath\newfile2.txt","normal");
				filecopy(dir&"sourcePath\newfile2.txt",dir&"desPath");
				assertEquals("FALSE",getfileinfo(dir&"sourcePath\newfile2.txt").ishidden);
				assertEquals("FALSE",getfileinfo(dir&"desPath\newfile2.txt").ishidden);
				assertEquals("TRUE",getfileinfo(dir&"sourcePath\newfile2.txt").canwrite);
				assertEquals("TRUE",getfileinfo(dir&"desPath\newfile2.txt").canwrite);
			});
		});
	}

	function afterAll() {
		if(directoryexists(dir)) {
			directorydelete(dir,true);
		}
	}
}