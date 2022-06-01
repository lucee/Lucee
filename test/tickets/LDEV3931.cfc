component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrenttemplatepath()) & "LDEV3931";
		if(!directoryExists(path)) directoryCreate(path)
		variables.file = "#path#\test.txt";
	}
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3931", function() {	
			it( title="checking file locking issue in isImageFile()", body=function( currentSpec ) {
				fileWrite(variables.file , "This is test file");
				isImageFile( variables.file ); // checking not an image file in isImageFile()
				try {
					fileDelete(variables.file);
					var result = "File deleted successfully";
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).toBe("File deleted successfully");
				expect(fileExists(variables.file)).toBeFalse();
			});
		});
	}

	function afterAll() {
		if (fileExists(variables.file)) { 
			var javaIoFile = createObject("java","java.io.File").init(variables.file);
			javaIoFile.deleteOnExit();
		} 
	}
}