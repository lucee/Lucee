component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {
	function beforeAll(){
		variables.testDir = server._getTempDir( "LDEV3675" );
	}

	function afterAll(){
		if ( directoryExists( testDir ) ){
			DirectoryDelete( testDir, true );
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1388", function() {
			it(title="create 1000 random files using getTempFile (threaded) ", body = function( currentSpec ) {
				var arr = [];
				arraySet( arr, 1, 1000, "temp" );
				var failed = 0;
				arr.each(function(){
					try {
						getTempFile( variables.testDir, "ldev3675", "jpg" );
					} catch( e ){
						failed++;
					}
				}, true);
				expect ( failed ).toBe( 0 );
			});

			it(title="create 1000 random files using getTempFile (unthreaded) ", body = function( currentSpec ) {
				var failed = 0;
				loop times=1000 {
					try {
						getTempFile( variables.testDir, "ldev3675", "jpg" );
					} catch( e ){
						failed++;
					}
				};
				expect( failed ).toBe( 0 );
			});
		});
	}
}