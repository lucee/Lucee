component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-4081", function() {
			it( title = "Checking filewrite() to include the path in error if path not exist", body=function( currentSpec ) {
				var testFile = replace("#getDirectoryFromPath(getCurrenttemplatepath())#path_to_not_exist\testFile.txt","/","\","all");
				try {
					fileWrite(testFile, "data")
				}
				catch(any e){
					var result = replace(e.message,"/","\","all");
				}
				expect( result ).toInclude( testFile );
			});
		});
	}
} 
