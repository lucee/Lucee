component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "cfinclude path test", function(){

			it( "test including file from outside webroot", function(){
				var f=getTempFile(getTempDirectory(), "ldev-4601-cfinclude-path", "cfm");
				fileWrite( f, '<cfset ldev4601=true>' );
				expect( fileExists(f) ).toBeTrue();

				cfinclude(template=f);
				expect( ldev4601 ).toBeTrue();
			});

			it( "test including file from inside webroot", function(){
				try {
					var f=getTempFile(getDirectoryFromPath(getCurrentTemplatePath()), "ldev-4601-cfinclude-path", "cfm");
					fileWrite( f, '<cfset ldev4601=true>' );
					expect( fileExists(f) ).toBeTrue();

					cfinclude(template=f);
					expect( ldev4601 ).toBeTrue();
				} finally {
					if (FileExists( f ) )
						FileDelete( f )
				}
			});


		} );
	}

}
