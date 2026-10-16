component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults, testBox ) {
		describe( "LDEV-6588 CFML functions as Java functional interfaces (LDEV-2075)", function() {

			it( title="closure passed directly where java.util.function.Function is expected", body=function( currentSpec ) {
				var result = createObject( "java", "java.util.Optional" ).of( "a" ).map( function( v ) {
					return v & "!";
				} ).get();
				expect( result ).toBe( "a!" );
			});

			it( title="lambda passed directly where java.util.function.Function is expected", body=function( currentSpec ) {
				var result = createObject( "java", "java.util.Optional" ).of( "a" ).map( ( v ) => v & "!" ).get();
				expect( result ).toBe( "a!" );
			});

			it( title="named function passed directly where java.util.function.Function is expected", body=function( currentSpec ) {
				var result = createObject( "java", "java.util.Optional" ).of( "a" ).map( appendBang ).get();
				expect( result ).toBe( "a!" );
			});

			it( title="javaCast closure to java.util.function.Function", body=function( currentSpec ) {
				var f = javaCast( "java.util.function.Function", function( v ) {
					return v & "!";
				} );
				expect( isInstanceOf( f, "java.util.function.Function" ) ).toBeTrue();
				expect( f.apply( "a" ) ).toBe( "a!" );
				expect( createObject( "java", "java.util.Optional" ).of( "a" ).map( f ).get() ).toBe( "a!" );
			});

			it( title="closure passed directly where java.lang.Runnable is expected (void return)", body=function( currentSpec ) {
				var state = { ran: false };
				var t = createObject( "java", "java.lang.Thread" ).init( function() {
					state.ran = true;
				} );
				t.run();
				expect( state.ran ).toBeTrue();
			});

			it( title="javaCast closure to java.lang.Runnable (void return)", body=function( currentSpec ) {
				var state = { ran: false };
				var r = javaCast( "java.lang.Runnable", function() {
					state.ran = true;
				} );
				r.run();
				expect( state.ran ).toBeTrue();
			});

			it( title="closure passed directly where java.util.function.IntUnaryOperator is expected (primitive return)", body=function( currentSpec ) {
				var result = createObject( "java", "java.util.stream.IntStream" ).range( 1, 4 ).map( function( i ) {
					return i * 2;
				} ).sum();
				expect( result ).toBe( 12 );
			});

		});
	}

	private function appendBang( v ) {
		return arguments.v & "!";
	}

}
