component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-4081", function() {
			it( title = "Checking filewrite() to include the path in error if path not exist", body=function( currentSpec ) {
				var testFile = replace("#getDirectoryFromPath(getCurrenttemplatepath())#path_to_not_exist\testFile.txt","/","\","all");
				try {
					fileWrite(testFile, "data");
				}
				catch(any e){
					var result = replace(e.message,"/","\","all");
				}
				expect( result ).toInclude( testFile );
			});

			it( title = "Checking fileRead() to include the path in error if path not exist", body=function( currentSpec ) {
				var testFile = replace("#getDirectoryFromPath(getCurrenttemplatepath())#path_to_not_exist\testFile.txt","/","\","all");
				try {
					fileRead(testFile, "data");
				}
				catch(any e){
					var result = replace(e.message,"/","\","all");
				}
				expect( result ).toInclude( testFile );
			});
		});
	}
} 
