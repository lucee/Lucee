component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run( testResults , testBox ) {
		describe( "test case for FileAppend", function() {
			it(title = "Checking with FileAppend", body = function( currentSpec ) {
				_file=dir&"test.txt";
				fileWrite(_file,"ABC");
				fileAppend(_file,"DEF","UTF-8");
				assertEquals("ABCDEF",trim(fileRead(_file)));
				_another_file = fileOpen(dir&"test.txt");
				fileAppend(_file,"GHI","UTF-8");
				assertEquals("ABCDEFGHI",trim(fileRead(_file)));
			});
		});
	}

	function afterAll(){
		directorydelete(dir,true);
	}
}
