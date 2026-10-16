component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="LDEV-6322 maven resolver routes snapshots to snapshot-capable repositories", body=function() {

			it( title="mavenLoad of a non-existent -SNAPSHOT throws a clean error (not an NPE)", body=function( currentSpec ) {
				// Lucee 8 ships a default snapshot-capable repository, so the snapshot is correctly routed there;
				// since the artifact does not exist it must fail with a clean download error rather than an NPE.
				try {
					mavenLoad( [
						"com.example.ldev6322:no-such-artifact:1.0.0-SNAPSHOT"
					] );
					fail( "expected mavenLoad to throw — the snapshot artifact does not exist in any configured repository" );
				}
				catch ( any e ) {
					expect( e.message ).toInclude( "Failed to download" );
					expect( e.message ).notToInclude( "NullPointerException" );
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
