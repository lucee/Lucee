component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6135 cfthread join with concurrent exceptions", function() {

			it( "threads throwing java exceptions should all complete and join", function() {
				// Phase 1: spawn threads that hammer NativeException.getInstance()
				// to corrupt the unsynchronized static LRU cache
				var poisonCount = 200;
				var poisonNames = [];
				loop from="1" to="#poisonCount#" index="local.i" {
					var tName = "poison_#i#";
					poisonNames.append( tName );
					thread name="#tName#" {
						sleep( 100 );
						loop from="1" to="100" index="local.j" {
							try {
								createObject( "java", "java.lang.Integer" ).parseInt( "bad_##j##" );
							}
							catch( any e ) {}
						}
					}
				}
				thread action="join" name="#poisonNames.toList()#" timeout="30000";

				// Phase 2: now the LRU may be corrupted — spawn threads that
				// throw exceptions and verify join still completes them all
				var threadCount = 200;
				var names = [];
				loop from="1" to="#threadCount#" index="local.i" {
					var tName = "worker_#i#";
					names.append( tName );
					thread name="#tName#" {
						sleep( randRange( 50, 200 ) );
						try {
							createObject( "java", "java.lang.Integer" ).parseInt( "fail" );
						}
						catch( any e ) {}
						thread.finished = true;
					}
				}

				thread action="join" name="#names.toList()#" timeout="60000";

				loop array="#names#" item="local.tName" {
					var ts = cfthread[ tName ];
					expect( ts.STATUS ).toBe( "COMPLETED",
						"Thread [#tName#] was [#ts.STATUS#] - LRU corruption caused exception avalanche?" );
					expect( ts.finished ?: false ).toBeTrue(
						"Thread [#tName#] never set finished flag" );
				}
			});

		});

	}

}
