component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function run( testResults, testBox ) {

		describe( "LDEV-5017: isJson / deserializeJSON with unquoted bare values", function() {

			it( title="isJson returns false for an unquoted bare value", body=function( currentSpec ) {
				var foo = "bar";
				expect( isJson( '{"a": foo}' ) ).toBeFalse();
				expect( isJson( '[foo]' ) ).toBeFalse();
				expect( isJson( '{"a": [1, foo]}' ) ).toBeFalse();
			});

			it( title="deserializeJSON throws for an unquoted bare value", body=function( currentSpec ) {
				var bar = "foo";
				expect( function() {
					deserializeJSON( '[bar]' );
				}).toThrow();
				expect( function() {
					deserializeJSON( '{"a": bar}' );
				}).toThrow();
				expect( function() {
					deserializeJSON( '{"a": {"b": bar}}' );
				}).toThrow();
				expect( function() {
					deserializeJSON( '{"a": bar}', true, "json5" );
				}).toThrow();
			});

			it( title="true, false and null are still allowed unquoted", body=function( currentSpec ) {
				expect( isJson( '{"a":true,"b":null,"c":1}' ) ).toBeTrue();
				var res = deserializeJSON( '{"a":true,"b":null,"c":1,"d":false}' );
				expect( res.a ).toBeTrue();
				expect( res.d ).toBeFalse();
				expect( res.c ).toBe( 1 );
				expect( isNull( res.b ) ).toBeTrue();

				var arr = deserializeJSON( '[1, "x", true, false, null, {"k": [2]}]' );
				expect( arrayLen( arr ) ).toBe( 6 );
				expect( arr[ 6 ].k[ 1 ] ).toBe( 2 );
			});

			it( title="unquoted keys (json5) still work", body=function( currentSpec ) {
				expect( isJson( '{a:1}' ) ).toBeTrue();
				var res = deserializeJSON( '{a:1, b:"x", c:true}' );
				expect( res.a ).toBe( 1 );
				expect( res.b ).toBe( "x" );
				expect( res.c ).toBeTrue();
			});

			it( title="evaluate with variables is not affected", body=function( currentSpec ) {
				var foo = "bar";
				expect( evaluate( "foo" ) ).toBe( "bar" );
			});

		});
	}

}
