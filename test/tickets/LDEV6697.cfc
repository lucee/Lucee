component extends="org.lucee.cfml.test.LuceeTestCase" labels="wddx" {

	function run( testResults, testBox ) {
		describe( "LDEV-6697 WDDX deserialization preserves struct key order", function() {

			it( "keeps the key order of an ordered struct", function() {
				var src = [:];
				var expected = [];
				// reverse alphabetical with mixed names, so neither hash nor sorted order matches by accident
				for ( var i = 50; i >= 1; i-- ) {
					var key = "k" & chr( 64 + ( i % 26 ) + 1 ) & i;
					src[ key ] = i;
					arrayAppend( expected, key );
				}
				wddx action="cfml2wddx" input=src output="local.packet";
				wddx action="wddx2cfml" input=packet output="local.result";

				expect( isStruct( result ) ).toBeTrue();
				expect( structKeyArray( result ) ).toBe( expected );
				for ( var key in expected ) {
					expect( result[ key ] ).toBe( src[ key ] );
				}
			});

			it( "keeps the key order of nested structs", function() {
				var src = [ z: 1, y: [ c: 1, b: 2, a: 3 ], x: 3, w: [ q: 1, p: 2 ] ];
				wddx action="cfml2wddx" input=src output="local.packet";
				wddx action="wddx2cfml" input=packet output="local.result";

				expect( structKeyList( result ) ).toBe( structKeyList( src ) );
				expect( uCase( structKeyList( result ) ) ).toBe( "Z,Y,X,W" );
				expect( uCase( structKeyList( result.y ) ) ).toBe( "C,B,A" );
				expect( uCase( structKeyList( result.w ) ) ).toBe( "Q,P" );
			});

			it( "keeps the key order of a struct inside an array", function() {
				var src = [ [ zeta: 1, alpha: 2, mu: 3 ] ];
				wddx action="cfml2wddx" input=src output="local.packet";
				wddx action="wddx2cfml" input=packet output="local.result";

				expect( uCase( structKeyList( result[ 1 ] ) ) ).toBe( "ZETA,ALPHA,MU" );
			});
		});
	}
}
