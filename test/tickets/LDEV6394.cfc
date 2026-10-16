component extends="org.lucee.cfml.test.LuceeTestCase"	{

	// cfthread spawned from inside a parallel arrayMap closure must be reliably joinable
	// from the same closure. Each closure runs on its own clone PC but they all share
	// the same root PC's thread registry, so concurrent spawns race the registry writes.

	public void function testParallelCfthreadJoinInClosure(){
		var N = 16;
		var input = [];
		for ( i = 1; i <= N; i++ ) arrayAppend( input, i );

		var results = arrayMap( input, function( idx ){
			var tname = "ldev6394_" & arguments.idx;
			thread name = "#tname#" v = "#arguments.idx#" {
				thread.r = "value-" & attributes.v;
			}
			thread action = "join" name = "#tname#";
			return cfthread[ tname ].r ?: "MISSING";
		}, true );

		assertEquals( N, arrayLen( results ) );
		for ( i = 1; i <= N; i++ ) {
			assertEquals( "value-" & i, results[ i ] );
		}
	}

	// arrayEach with parallel=true goes through the same UDFCaller2 clone path as arrayMap.
	// Collect into a thread-safe Java map because arrayEach has no return value.
	public void function testParallelArrayEachCfthread(){
		var N = 16;
		var input = [];
		for ( i = 1; i <= N; i++ ) arrayAppend( input, i );
		var collector = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();

		arrayEach( input, function( idx ){
			var tname = "ldev6394_each_" & arguments.idx;
			thread name = "#tname#" v = "#arguments.idx#" {
				thread.r = "each-" & attributes.v;
			}
			thread action = "join" name = "#tname#";
			collector.put( javaCast( "string", tname ), cfthread[ tname ].r ?: "MISSING" );
		}, true );

		assertEquals( N, collector.size() );
		for ( i = 1; i <= N; i++ ) {
			assertEquals( "each-" & i, collector.get( "ldev6394_each_" & i ) );
		}
	}

	// parallel arrayMap → cfthread → parallel arrayMap. Two clone hops + concurrent registry writes
	// from both the outer thread spawns and the inner parallel closures.
	public void function testNestedParallelThreadParallel(){
		var N = 6;
		var input = [];
		for ( i = 1; i <= N; i++ ) arrayAppend( input, i );

		var results = arrayMap( input, function( outerIdx ){
			var tname = "ldev6394_deep_" & arguments.outerIdx;
			thread name = "#tname#" oidx = "#arguments.outerIdx#" {
				thread.inner = arrayMap( [ 1, 2, 3 ], function( innerIdx ){
					return "d-" & attributes.oidx & "-" & arguments.innerIdx;
				}, true );
			}
			thread action = "join" name = "#tname#";
			return cfthread[ tname ].inner ?: [];
		}, true );

		assertEquals( N, arrayLen( results ) );
		var flat = [];
		for ( inner in results ) for ( v in inner ) arrayAppend( flat, v );
		assertEquals( N * 3, arrayLen( flat ) );
		for ( i = 1; i <= N; i++ ) {
			for ( j = 1; j <= 3; j++ ) {
				assertEquals( "d-" & i & "-" & j, results[ i ][ j ] );
			}
		}
	}
}
