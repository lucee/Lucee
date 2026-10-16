component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Closures defined in the pseudo-constructor body of a CFC — each instance must see its own state.

	function run( testResults, testBox ){

		describe( "closures defined in pseudo-constructor body capture per-instance variables scope", function(){

			it( "closure reads constructor-set tag — function() form", function(){
				var c = new closures.Tagged( tag="alpha" );
				expect( c.callGreeter() ).toBe( "hello-alpha" );
			});

			it( "closure reads constructor-set tag — arrow-lambda form", function(){
				var c = new closures.Tagged( tag="alpha" );
				expect( c.callTagReader() ).toBe( "alpha" );
			});

			it( "two siblings — each closure reads its own tag", function(){
				var a = new closures.Tagged( tag="a-tag" );
				var b = new closures.Tagged( tag="b-tag" );
				expect( a.callGreeter() ).toBe( "hello-a-tag" );
				expect( b.callGreeter() ).toBe( "hello-b-tag" );
			});

			it( "mutating one instance's tag via method updates only its own closure view", function(){
				var a = new closures.Tagged( tag="orig-a" );
				var b = new closures.Tagged( tag="orig-b" );
				a.setTag( "mutated-a" );
				expect( a.callGreeter() ).toBe( "hello-mutated-a" );
				expect( b.callGreeter() ).toBe( "hello-orig-b" );
			});

		});

		describe( "per-instance dispatch survives volume and concurrency", function(){

			it( "100 sequential new instances — each closure resolves to its own tag", function(){
				for ( var i=1; i<=100; i++ ) {
					var c = new closures.Tagged( tag="tag-" & i );
					expect( c.callGreeter() ).toBe( "hello-tag-" & i );
				}
			});

			it( "concurrent new + closure invocation — each thread sees its own tag", function(){
				var threadCount = 20;
				var threadNames = [];
				for ( var i=1; i<=threadCount; i++ ) threadNames.append( "bodyClosure_" & i );
				for ( var n in threadNames ){
					thread name="#n#" tag=n {
						var c = new closures.Tagged( tag=attributes.tag );
						thread.result = c.callGreeter();
					}
				}
				thread action="join" name=arrayToList( threadNames );
				for ( var n in threadNames ){
					expect( cfthread[ n ].result ).toBe( "hello-" & n );
				}
			});

		});

	}

}
