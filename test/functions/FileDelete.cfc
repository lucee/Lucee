component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		variables.name = ListFirst(ListLast(getCurrentTemplatePath(), "\/"), ".");
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
		if(directoryExists(dir)) {
			afterAll();
		}
		cfdirectory(directory="#dir#" action="create" mode="777");
	}

	function run() {
		describe( "testcase for FileDelete()", function() {
			it(title = "Checking with FileDelete()", body = function( currentSpec ) {
				var src = variables.dir&"test.txt";
				fileWrite(src, "text");
				fileDelete(src);
				expect(FileExists(src)).toBeFalse();
			});
		});
	}
	
	function afterAll() {
		directoryDelete(variables.dir, true);
	}

}