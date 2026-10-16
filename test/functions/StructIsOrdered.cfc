component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function beforeAll() {
		variables.orderedStruct = structNew( "ordered" );
		variables.orderedStruct.a = 1;
		variables.orderedStruct.b = 2;
		variables.unorderedStruct = structNew();
		variables.unorderedStruct.a = 1;
		variables.unorderedStruct.b = 2;
		variables.emptyOrderedStruct = structNew( "ordered" );
		variables.emptyUnorderedStruct = structNew();
		variables.orderedStructLiteral = [ one=1, two=2, three=6, four=4 ];
		variables.unorderedStructLiteral = { one=1, two=2, three=6, four=4 };
	}

	function run( testResults, testBox ) {

		describe( "Testcases for StructIsOrdered() BIF", function() {
			it( title="should return true for ordered struct", body=function( currentSpec ) {
				expect( structIsOrdered( variables.orderedStruct ) ).toBeTrue();
			});

			it( title="should return false for unordered struct", body=function( currentSpec ) {
				expect( structIsOrdered( variables.unorderedStruct ) ).toBeFalse();
			});

			it( title="should return false for unordered struct (literal syntax)", body=function( currentSpec ) {
				expect( structIsOrdered( variables.unorderedStructLiteral ) ).toBeFalse();
			});

			it( title="should return true for ordered struct (literal syntax)", body=function( currentSpec ) {
				expect( structIsOrdered( variables.orderedStructLiteral ) ).toBeTrue();
			});

			it( title="should return true for empty ordered struct", body=function( currentSpec ) {
				expect( structIsOrdered( variables.emptyOrderedStruct ) ).toBeTrue();
			});

			it( title="should return false for empty unordered struct", body=function( currentSpec ) {
				expect( structIsOrdered( variables.emptyUnorderedStruct ) ).toBeFalse();
			});

			it( title="should return true for linked struct", body=function( currentSpec ) {
				expect( structIsOrdered( structNew( "linked" ) ) ).toBeTrue();
			});

			it( title="should return true for ordered-casesensitive struct", body=function( currentSpec ) {
				var orderedCaseSensitive = structNew( "ordered-casesensitive" );
				orderedCaseSensitive[ "B" ] = 1;
				orderedCaseSensitive[ "a" ] = 2;
				expect( structIsOrdered( orderedCaseSensitive ) ).toBeTrue();
			});

			it( title="should return true for ordered struct with an onMissingKey listener", body=function( currentSpec ) {
				var orderedWithListener = structNew( "ordered", function( struct, key ) {
					return key;
				});
				expect( structIsOrdered( orderedWithListener ) ).toBeTrue();
			});

			it( title="should return false for normal, casesensitive, synchronized, weak, soft and max structs", body=function( currentSpec ) {
				for ( var type in [ "normal", "casesensitive", "synchronized", "weak", "soft", "max:5" ] ) {
					expect( structIsOrdered( structNew( type ) ) ).toBeFalse( type );
				}
			});

			it( title="should keep the ordered type for a duplicated ordered struct", body=function( currentSpec ) {
				expect( structIsOrdered( duplicate( variables.orderedStruct ) ) ).toBeTrue();
				expect( structIsOrdered( duplicate( variables.unorderedStruct ) ) ).toBeFalse();
			});

			it( title="should keep the ordered type for a duplicated ordered-casesensitive struct", body=function( currentSpec ) {
				var orderedCaseSensitive = structNew( "ordered-casesensitive" );
				orderedCaseSensitive[ "B" ] = 1;
				orderedCaseSensitive[ "a" ] = 2;
				var copy = duplicate( orderedCaseSensitive );
				expect( structIsOrdered( copy ) ).toBeTrue();
				expect( structKeyList( copy ) ).toBe( "B,a" );
			});

			it( title="should return true for a sorted struct created with structToSorted()", body=function( currentSpec ) {
				var sorted = structToSorted( { c=3, a=1, b=2 } );
				expect( structIsOrdered( sorted ) ).toBeTrue();
			});

			it( title="should return true for Java insertion ordered and sorted maps, false for a hash map", body=function( currentSpec ) {
				// only Java maps are backed by these map types
				expect( structIsOrdered( createObject( "java", "java.util.LinkedHashMap" ).init() ) ).toBeTrue();
				expect( structIsOrdered( createObject( "java", "java.util.TreeMap" ).init() ) ).toBeTrue();
				expect( structIsOrdered( createObject( "java", "java.util.HashMap" ).init() ) ).toBeFalse();
			});

			it( title="should throw a cast error for a value that is not a struct", body=function( currentSpec ) {
				var error = "";
				try {
					structIsOrdered( "not a struct" );
				}
				catch ( any e ) {
					error = e;
				}
				expect( isSimpleValue( error ) ).toBeFalse( "structIsOrdered() should throw for a string" );
				expect( error.type ).toBe( "expression" );
				expect( error.message ).toInclude( "struct" );
			});
		});

		describe( "Testcases for isOrdered() - Member Function", function() {
			it( title="should return true for ordered struct (member)", body=function( currentSpec ) {
				expect( variables.orderedStruct.isOrdered() ).toBeTrue();
			});

			it( title="should return false for unordered struct (member)", body=function( currentSpec ) {
				expect( variables.unorderedStruct.isOrdered() ).toBeFalse();
			});

			it( title="should return false for unordered struct (literal syntax) (member)", body=function( currentSpec ) {
				expect( variables.unorderedStructLiteral.isOrdered() ).toBeFalse();
			});

			it( title="should return true for ordered struct (literal syntax) (member)", body=function( currentSpec ) {
				expect( variables.orderedStructLiteral.isOrdered() ).toBeTrue();
			});

			it( title="should return true for empty ordered struct (member)", body=function( currentSpec ) {
				expect( variables.emptyOrderedStruct.isOrdered() ).toBeTrue();
			});

			it( title="should return false for empty unordered struct (member)", body=function( currentSpec ) {
				expect( variables.emptyUnorderedStruct.isOrdered() ).toBeFalse();
			});

			it( title="should return true for ordered-casesensitive struct (member)", body=function( currentSpec ) {
				expect( structNew( "ordered-casesensitive" ).isOrdered() ).toBeTrue();
			});

			it( title="should return true for ordered struct with an onMissingKey listener (member)", body=function( currentSpec ) {
				var orderedWithListener = structNew( "ordered", function( struct, key ) {
					return key;
				});
				expect( orderedWithListener.isOrdered() ).toBeTrue();
			});
		});

	}
}
