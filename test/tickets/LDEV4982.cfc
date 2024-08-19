component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-4982", function() {
			it( title = "Checking {lucee-config-file}", body=function( currentSpec ) {
				var cfconfigPath = expandPath( "{lucee-config-file}" );
				systemOutput(chr(10) & cfconfigPath, true)
				expect( fileExists( cfconfigPath ) ).toBeTrue();
				var json= fileRead( cfconfigPath );
				expect( isJson( json ) ).toBeTrue();
			});
		});
	}

} 
