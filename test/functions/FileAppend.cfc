component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
		if(directoryExists(dir)){
			afterAll();
		}
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run( testResults , testBox ) {
		describe( "test case for FileAppend", function() {
			it(title = "Checking with FileAppend", body = function( currentSpec ) {
				_file=dir&"test.txt";
				fileWrite(_file,"ABC");
				fileAppend(_file,"DEF","UTF-8");
				assertEquals("ABCDEF",trim(fileRead(_file)));
				_fileResource = fileOpen(dir&"test.txt", "write");
				fileAppend(_fileResource, "ABCDEFGHI", "UTF-8");
				assertEquals("ABCDEFGHI", trim(fileRead(_fileResource)));
			});
		});
	}

	function afterAll(){
		directorydelete(dir,true);
	}
}
