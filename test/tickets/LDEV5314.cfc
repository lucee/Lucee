component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function beforeAll() {
		variables.info = createObject( "java", "lucee.runtime.engine.InfoImpl" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5314 InfoImpl.parseDateTime() with short zone names in the build date", function() {

			it( title="IST is India Standard Time (Asia/Kolkata)", body=function( currentSpec ) {
				expect( info.parseDateTime( "2025/02/10 14:45:05 IST" ) )
					.toBe( info.parseDateTime( "2025/02/10 09:15:05 UTC" ) );
			});

			it( title="EST and PST", body=function( currentSpec ) {
				expect( info.parseDateTime( "2025/02/10 14:45:05 EST" ) )
					.toBe( info.parseDateTime( "2025/02/10 19:45:05 UTC" ) );
				expect( info.parseDateTime( "2025/02/10 14:45:05 PST" ) )
					.toBe( info.parseDateTime( "2025/02/10 22:45:05 UTC" ) );
			});

			it( title="region ids still work", body=function( currentSpec ) {
				expect( info.parseDateTime( "2025/02/10 14:45:05 Europe/Zurich" ) )
					.toBe( info.parseDateTime( "2025/02/10 13:45:05 UTC" ) );
			});

			it( title="a zone name that is no zone id (CEST) uses the fallback parser", body=function( currentSpec ) {
				expect( info.parseDateTime( "2025/07/10 14:45:05 CEST" ) )
					.toBe( info.parseDateTime( "2025/07/10 12:45:05 UTC" ) );
			});
		});
	}
}
