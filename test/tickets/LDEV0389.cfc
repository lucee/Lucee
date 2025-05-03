component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.tempDir = getTempDirectory() & createUUID();
		if ( !DirectoryExists( tempDir ) ){
			DirectoryCreate( tempDir );
		}

		variables.testFile = '#tempDir##server.separator.file#ldev0389.txt';
		if ( !FileExists( variables.testFile ) ){
			FileWrite( variables.testFile, "hello world");
		}
	}

	function afterAll(){
		if ( FileExists( variables.testFile ) ){
			FileDelete( variables.testFile );
		}
		if ( !DirectoryExists( tempDir ) ){
			DirectoryDelete( tempDir );
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-389", function() {
			it("Checking directoryList, callback function with no arguments", function( currentSpec ) {
				try {
					var result = directoryList( variables.tempDir, true, "array", function(){
						return true;
					});
				} catch ( any e ){
					result[1] = e.stacktrace;
				}
				expect( result[ 1 ] ).toBe( variables.testFile );
			});

			it("Checking directoryList, callback function with single argument", function( currentSpec ) {
				try {
					var result = directoryList( variables.tempDir, true, "array", function(a){
						return true;
					});
				} catch ( any e ){
					result[1] = e.stacktrace;
				}
				expect( result[ 1 ] ).toBe( variables.testFile );
			});

			// this isn't supported, throws UDF filter has too many arguments [2], should have at maximum 1 argument
			it(title="Checking directoryList, callback function with two arguments", skip=true, body=function( currentSpec ) {
				try {
					var result = directoryList( variables.tempDir, true, "array", function( a, b ){
						return true;
					});
				} catch ( any e ){
					result[1] = e.stacktrace;
				}
				expect( result[ 1 ] ).toBe( variables.testFile );
			});
		});
	}

}