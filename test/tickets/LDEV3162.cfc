component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function run( testResults, testBox ) {

		describe( title="LDEV-3162 structKeyFirst() and structKeyLast()", body=function() {

			describe( "ordered struct", function() {

				it( title="returns the first and the last key", body=function( currentSpec ) {
					var sct = structNew( "ordered" );
					sct[ "zebra" ] = 1;
					sct[ "apple" ] = 2;
					sct[ "mango" ] = 3;
					expect( structKeyFirst( sct ) ).toBe( "zebra" );
					expect( structKeyLast( sct ) ).toBe( "mango" );
				});

				it( title="returns the key with its original case", body=function( currentSpec ) {
					var sct = structNew( "ordered" );
					sct[ "FirstKey" ] = 1;
					sct[ "LastKey" ] = 2;
					expect( structKeyFirst( sct ) ).toBeWithCase( "FirstKey" );
					expect( structKeyLast( sct ) ).toBeWithCase( "LastKey" );
				});

				it( title="first and last are the same key for a struct with a single key", body=function( currentSpec ) {
					var sct = [ only: 1 ];
					expect( structKeyFirst( sct ) ).toBe( "only" );
					expect( structKeyLast( sct ) ).toBe( "only" );
				});

				it( title="works with the ordered struct literal and the linked type", body=function( currentSpec ) {
					var literal = [ one: 1, two: 2, three: 3 ];
					expect( structKeyFirst( literal ) ).toBe( "one" );
					expect( structKeyLast( literal ) ).toBe( "three" );

					var linked = structNew( "linked" );
					linked.b = 1;
					linked.a = 2;
					expect( structKeyFirst( linked ) ).toBe( "b" );
					expect( structKeyLast( linked ) ).toBe( "a" );
				});

				it( title="works with an ordered case sensitive struct", body=function( currentSpec ) {
					var sct = structNew( "ordered-casesensitive" );
					sct[ "b" ] = 1;
					sct[ "B" ] = 2;
					sct[ "a" ] = 3;
					expect( structKeyFirst( sct ) ).toBeWithCase( "b" );
					expect( structKeyLast( sct ) ).toBeWithCase( "a" );
				});

				it( title="works with an ordered struct with an onMissingKey listener", body=function( currentSpec ) {
					var sct = structNew( "ordered", function( struct, key ) { return key; } );
					sct.x = 1;
					sct.y = 2;
					expect( structKeyFirst( sct ) ).toBe( "x" );
					expect( structKeyLast( sct ) ).toBe( "y" );
				});

				it( title="follows changes to the struct", body=function( currentSpec ) {
					var sct = [ a: 1, b: 2, c: 3 ];
					structDelete( sct, "a" );
					expect( structKeyFirst( sct ) ).toBe( "b" );
					structDelete( sct, "c" );
					expect( structKeyLast( sct ) ).toBe( "b" );
					sct.d = 4;
					expect( structKeyLast( sct ) ).toBe( "d" );
					expect( structKeyFirst( sct ) ).toBe( "b" );
				});

				it( title="supports keys with a null value", body=function( currentSpec ) {
					var sct = [ a: 1 ];
					sct.b = nullValue();
					expect( structKeyLast( sct ) ).toBe( "b" );
				});

				it( title="returns the same keys as structKeyArray()", body=function( currentSpec ) {
					var sct = structNew( "ordered" );
					loop from=1 to=50 index="local.i" {
						sct[ "key" & ( 51 - i ) ] = i;
					}
					var keys = structKeyArray( sct );
					expect( structKeyFirst( sct ) ).toBe( keys[ 1 ] );
					expect( structKeyLast( sct ) ).toBe( keys[ keys.len() ] );
				});
			});

			describe( "sorted struct", function() {

				it( title="works with the result of structToSorted()", body=function( currentSpec ) {
					var unsorted = structNew( "ordered" );
					unsorted[ "dd" ] = 1;
					unsorted[ "aa" ] = 2;
					unsorted[ "cc" ] = 3;
					var sorted = structToSorted( unsorted, "text", "asc" );
					expect( structKeyFirst( sorted ) ).toBe( "aa" );
					expect( structKeyLast( sorted ) ).toBe( "dd" );
					sorted = structToSorted( unsorted, "text", "desc" );
					expect( structKeyFirst( sorted ) ).toBe( "dd" );
					expect( structKeyLast( sorted ) ).toBe( "aa" );
				});

				it( title="works with a java sorted map", body=function( currentSpec ) {
					var map = createObject( "java", "java.util.TreeMap" );
					map.put( "pear", 1 );
					map.put( "banana", 2 );
					map.put( "cherry", 3 );
					expect( structKeyFirst( map ) ).toBe( "banana" );
					expect( structKeyLast( map ) ).toBe( "pear" );
				});
			});

			describe( "member functions", function() {

				it( title="struct.keyFirst() and struct.keyLast()", body=function( currentSpec ) {
					var sct = [ first: 1, middle: 2, last: 3 ];
					expect( sct.keyFirst() ).toBe( "first" );
					expect( sct.keyLast() ).toBe( "last" );
				});

				it( title="member functions chained on a sorted struct", body=function( currentSpec ) {
					var sct = { c: 1, a: 2, b: 3 };
					expect( sct.toSorted().keyFirst() ).toBe( "a" );
					expect( sct.toSorted().keyLast() ).toBe( "c" );
				});

				it( title="member functions throw on an unordered struct", body=function( currentSpec ) {
					var sct = { a: 1, b: 2 };
					expect( function() { sct.keyFirst(); } ).toThrow( "expression" );
					expect( function() { sct.keyLast(); } ).toThrow( "expression" );
				});
			});

			describe( "unordered struct", function() {

				it( title="structKeyFirst() throws, the order of the keys is undefined", body=function( currentSpec ) {
					var sct = { a: 1, b: 2 };
					expect( function() { structKeyFirst( sct ); } ).toThrow( "expression", "not ordered" );
				});

				it( title="structKeyLast() throws, the order of the keys is undefined", body=function( currentSpec ) {
					var sct = structNew();
					sct.a = 1;
					sct.b = 2;
					expect( function() { structKeyLast( sct ); } ).toThrow( "expression", "not ordered" );
				});

				it( title="throws that the struct is not ordered, also when the struct is empty", body=function( currentSpec ) {
					expect( function() { structKeyFirst( {} ); } ).toThrow( "expression", "not ordered" );
					expect( function() { structKeyLast( {} ); } ).toThrow( "expression", "not ordered" );
				});

				it( title="throws for a scope", body=function( currentSpec ) {
					expect( function() { structKeyFirst( variables ); } ).toThrow( "expression", "not ordered" );
					expect( function() { structKeyLast( url ); } ).toThrow( "expression", "not ordered" );
				});
			});

			describe( "empty struct", function() {

				it( title="structKeyFirst() throws like arrayFirst() does for an empty array", body=function( currentSpec ) {
					expect( function() { arrayFirst( [] ); } ).toThrow( "expression", "is empty" );
					expect( function() { structKeyFirst( structNew( "ordered" ) ); } ).toThrow( "expression", "struct is empty" );
				});

				it( title="structKeyLast() throws like arrayLast() does for an empty array", body=function( currentSpec ) {
					expect( function() { arrayLast( [] ); } ).toThrow( "expression", "is empty" );
					expect( function() { structKeyLast( [:] ); } ).toThrow( "expression", "struct is empty" );
				});

				it( title="throws when the last key is removed", body=function( currentSpec ) {
					var sct = [ a: 1 ];
					structDelete( sct, "a" );
					expect( function() { structKeyFirst( sct ); } ).toThrow( "expression", "struct is empty" );
					expect( function() { structKeyLast( sct ); } ).toThrow( "expression", "struct is empty" );
				});

				it( title="throws for an empty sorted java map", body=function( currentSpec ) {
					var map = createObject( "java", "java.util.TreeMap" );
					expect( function() { structKeyFirst( map ); } ).toThrow( "expression", "struct is empty" );
					expect( function() { structKeyLast( map ); } ).toThrow( "expression", "struct is empty" );
				});
			});

			describe( "invalid argument", function() {

				it( title="throws if the argument is not a struct", body=function( currentSpec ) {
					expect( function() { structKeyFirst( "abc" ); } ).toThrow();
					expect( function() { structKeyLast( 123 ); } ).toThrow();
				});
			});
		});
	}
}
