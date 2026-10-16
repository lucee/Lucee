component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( title="parallel iteration reuses pooled PageContext clones", body=function() {

			// large enough that a small concurrency limit forces clones to be reused across elements
			var data = [];
			for ( var i = 1; i <= 200; i++ ) data.append( i );

			var expectedDoubled = data.map( function( v ){ return v * 2; } );

			it(title="arrayMap returns correct values with parallel='thread' and a small pool", body = function( currentSpec ) {
				var res = arrayMap( data, function( v ){ return v * 2; }, "thread", 4 );
				assertEquals( expectedDoubled.toList(), res.toList() );
			});

			it(title="arrayMap returns correct values with parallel='virtual' (unbounded)", body = function( currentSpec ) {
				var res = arrayMap( data, function( v ){ return v * 2; }, "virtual" );
				assertEquals( expectedDoubled.toList(), res.toList() );
			});

			it(title="arrayMap returns correct values with parallel='virtual' and a bounded concurrency", body = function( currentSpec ) {
				var res = arrayMap( data, function( v ){ return v * 2; }, "virtual", 3 );
				assertEquals( expectedDoubled.toList(), res.toList() );
			});

			it(title="arrayEach captures per-element output in order across reused clones", body = function( currentSpec ) {
				var expected = "";
				for ( var v in data ) expected &= "[" & v & "]";

				var out = "";
				savecontent variable="out" {
					arrayEach( data, function( v ){ writeOutput( "[" & v & "]" ); }, "thread", 4 );
				}
				assertEquals( expected, out );

				var outV = "";
				savecontent variable="outV" {
					arrayEach( data, function( v ){ writeOutput( "[" & v & "]" ); }, "virtual", 4 );
				}
				assertEquals( expected, outV );
			});

			it(title="arrayFilter keeps order and correctness across reused clones", body = function( currentSpec ) {
				var even = data.filter( function( v ){ return v % 2 == 0; } );
				assertEquals( even.toList(), arrayFilter( data, function( v ){ return v % 2 == 0; }, "thread", 4 ).toList() );
				assertEquals( even.toList(), arrayFilter( data, function( v ){ return v % 2 == 0; }, "virtual", 4 ).toList() );
			});

			it(title="a closure throwing in parallel still surfaces the error and cleans up", body = function( currentSpec ) {
				var threw = false;
				try {
					arrayEach( data, function( v ){ if ( v == 100 ) throw "boom"; }, "thread", 4 );
				}
				catch ( any e ) {
					threw = true;
				}
				assertTrue( threw, "expected the thrown error to propagate out of the parallel arrayEach" );
				// the pool/context must still be usable afterwards
				assertEquals( expectedDoubled.toList(), arrayMap( data, function( v ){ return v * 2; }, "thread", 4 ).toList() );
			});
		});
	}
}
