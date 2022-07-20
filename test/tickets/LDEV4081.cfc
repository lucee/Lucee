component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-4081", function() {
			it( title = "Checking filewrite() to include the path in error if path not exist", body=function( currentSpec ) {
				var testFile = "#getDirectoryFromPath(getCurrenttemplatepath())#path_to_not_exist\testFile.txt";
				writeDump(testfile); 
				try {
					fileWrite(testFile, "data")
				}
				catch(any e){
					var result = e.message;
				}
				expect(findNoCase(testFile, result)).toBeGT(0);
			});
		});
	}
} 