component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {
	function beforeAll(){
		variables.testDir = server._getTempDir( "LDEV3675" );
		variables.testRamDir = "ram://LDEV3675";
		if ( !directoryExists( testRamDir ) ){
			DirectoryCreate( testRamDir, true );
		}
	}

	function afterAll(){
		if ( directoryExists( testDir ) ){
			DirectoryDelete( testDir, true );
		}
		if ( directoryExists( testRamDir ) ){
			DirectoryDelete( testRamDir, true );
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-3675 (local file system)", function() {
			it(title="create 1,000 random files using getTempFile (threaded) (local file system)", body = function( currentSpec ) {
				var arr = [];
				arraySet( arr, 1, 1000, "temp" );
				var failed = 0;
				arr.each(function(){
					try {
						getTempFile( variables.testDir, "ldev3675-file-threaded", "jpg" );
					} catch( e ){
						failed++;
					}
				}, true);
				expect ( failed ).toBe( 0 );
			});

			it(title="create 1,000 random files using getTempFile (unthreaded) (local file system)", body = function( currentSpec ) {
				var failed = 0;
				loop times=1000 {
					try {
						getTempFile( variables.testDir, "ldev3675-file-unthreaded", "jpg" );
					} catch( e ){
						failed++;
					}
				};
				expect( failed ).toBe( 0 );
			});
		});

		describe( "Test suite for LDEV-3675 (ram file system)", function() {
			it(title="create 100,000 random files using getTempFile (threaded) (ram drive)", body = function( currentSpec ) {
				var arr = [];
				arraySet( arr, 1, 100000, "temp" );
				var failed = 0;
				arr.each(function(){
					try {
						getTempFile( variables.testRamDir, "ldev3675-ram-threaded", "jpg" );
					} catch( e ){
						failed++;
					}
				}, true);
				expect ( failed ).toBe( 0 );
			});

			it(title="create 100,000 random files using getTempFile (unthreaded) (ram drive) ", body = function( currentSpec ) {
				var failed = 0;
				loop times=100000 {
					try {
						getTempFile( variables.testRamDir, "ldev3675-ram-unthreaded", "jpg" );
					} catch( e ){
						failed++;
					}
				};
				expect( failed ).toBe( 0 );
			});
		});
	}
}