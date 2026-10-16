component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="LDEV-6322 maven resolver routes snapshots to snapshot-capable repositories", body=function() {

			it( title="mavenLoad of a -SNAPSHOT version throws clean error when no snapshot-capable repo configured", body=function( currentSpec ) {
				try {
					mavenLoad( [
						"com.example.ldev6322:no-such-artifact:1.0.0-SNAPSHOT"
					] );
					fail( "expected mavenLoad to throw — snapshot lookup with no snapshot-capable repo should be rejected" );
				}
				catch ( any e ) {
					expect( e.message ).toInclude( "snapshot-capable" );
				}
			});

			it( title="mavenLoad of a release version still works", body=function( currentSpec ) {
				var l = len( mavenLoad( [
					"org.apache.commons:commons-lang3:3.12.0"
				] ) );
				expect( l ).toBe( 1 );
			});

		});
	}
}
