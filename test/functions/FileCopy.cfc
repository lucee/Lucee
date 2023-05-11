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
		describe( "test case for FileCopy", function() {
			it(title = "Checking with FileCopy", body = function( currentSpec ) {
				srcDir=dir&"src/";
				trgDir=dir&"trg/";
				cfdirectory(directory="#srcDir#" action="create" mode="777");
				cfdirectory(directory="#trgDir#" action="create" mode="777");
				// define paths
				src=srcDir&"test.txt";
				dest1=trgDir&"testx.txt";
				dest3=trgDir&'test.txt';
				fileWrite(src,"Susi");
				assertEquals(false,FileExists(dest1));
				assertEquals(false,FileExists(dest3));
				// copy with destination file
				fileCopy(src,dest1);
				// copy with destination dir
				fileCopy(src,trgDir);
				assertEquals(true,FileExists(dest1));
				assertEquals(true,FileExists(dest3));
			});
		});
	}
	function afterAll(){
		directorydelete(dir,true);
	}
}