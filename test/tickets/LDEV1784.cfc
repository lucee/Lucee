component extends="org.lucee.cfml.test.LuceeTestCase" labels="array,list,struct" {

	function run( testResults, testBox ) {
		describe( "LDEV-1784: natural sort types for the existing sort functions", function() {

			it( title="arraySort natural, the example from the ticket", body=function( currentSpec ) {
				var arr = [ "foo100", "foo1", "foo10", "foo2", "foo20" ];
				arraySort( arr, "natural" );
				expect( arr ).toBe( [ "foo1", "foo2", "foo10", "foo20", "foo100" ] );
			});

			it( title="array member function sort and the sort type is case insensitive", body=function( currentSpec ) {
				expect( [ "file10", "file2", "file1" ].sort( "natural" ) ).toBe( [ "file1", "file2", "file10" ] );
				expect( [ "file10", "file2", "file1" ].sort( "NATURAL" ) ).toBe( [ "file1", "file2", "file10" ] );
				expect( [ "file10", "file2", "file1" ].sort( "naturalNoCase" ) ).toBe( [ "file1", "file2", "file10" ] );
			});

			it( title="arraySort desc", body=function( currentSpec ) {
				var arr = [ "file2", "file10", "file1" ];
				arraySort( arr, "natural", "desc" );
				expect( arr ).toBe( [ "file10", "file2", "file1" ] );
			});

			it( title="natural is case sensitive, naturalnocase is not", body=function( currentSpec ) {
				// uppercase before lowercase for the case sensitive sort (same as text)
				expect( [ "b2", "B10", "a1", "A2" ].sort( "natural" ) ).toBe( [ "A2", "B10", "a1", "b2" ] );
				expect( [ "b10", "B2", "a1", "A3" ].sort( "naturalnocase" ) ).toBe( [ "a1", "A3", "B2", "b10" ] );
			});

			it( title="digit runs inside the text and several digit runs", body=function( currentSpec ) {
				expect( [ "v1.10.1", "v1.2.10", "v1.2.9", "v1.10" ].sort( "natural" ) ).toBe( [ "v1.2.9", "v1.2.10", "v1.10", "v1.10.1" ] );
				expect( [ "a10b2", "a10b10", "a2b100" ].sort( "natural" ) ).toBe( [ "a2b100", "a10b2", "a10b10" ] );
			});

			it( title="numbers bigger than long are compared correctly", body=function( currentSpec ) {
				var big1 = "x99999999999999999999999";
				var big2 = "x100000000000000000000000";
				expect( [ big2, big1 ].sort( "natural" ) ).toBe( [ big1, big2 ] );
			});

			it( title="leading zeros are deterministic, same value: fewer zeros first, independent of the input order", body=function( currentSpec ) {
				var expected = [ "a1", "a01", "a001", "a2" ];
				expect( [ "a2", "a001", "a1", "a01" ].sort( "natural" ) ).toBe( expected );
				expect( [ "a01", "a2", "a1", "a001" ].sort( "natural" ) ).toBe( expected );
				expect( [ "a001", "a01", "a2", "a1" ].sort( "natural" ) ).toBe( expected );
				// the value counts first, the zeros only decide if everything else is equal
				expect( [ "a02b", "a1c", "a01c" ].sort( "natural" ) ).toBe( [ "a1c", "a01c", "a02b" ] );
				// all zeros
				expect( [ "a10", "a0", "a00", "a1" ].sort( "natural" ) ).toBe( [ "a0", "a00", "a1", "a10" ] );
			});

			it( title="mixed text, digits only, empty and prefix values", body=function( currentSpec ) {
				expect( [ "10", "9", "100", "1" ].sort( "natural" ) ).toBe( [ "1", "9", "10", "100" ] );
				expect( [ "foo1", "foo", "", "foo01x" ].sort( "natural" ) ).toBe( [ "", "foo", "foo1", "foo01x" ] );
				// a digit run against a non digit
				expect( [ "a1", "a-", "aa", "a" ].sort( "natural" ) ).toBe( [ "a", "a-", "a1", "aa" ] );
				// numbers (not strings) in the array
				expect( [ 10, 9, 100, 1 ].sort( "natural" ) ).toBe( [ 1, 9, 10, 100 ] );
			});

			it( title="the existing sort types are unchanged", body=function( currentSpec ) {
				var data = [ "foo100", "foo1", "foo10", "foo2", "foo20" ];
				expect( duplicate( data ).sort( "text" ) ).toBe( [ "foo1", "foo10", "foo100", "foo2", "foo20" ] );
				expect( duplicate( data ).sort( "textnocase" ) ).toBe( [ "foo1", "foo10", "foo100", "foo2", "foo20" ] );
				expect( [ "10", "9", "100", "1" ].sort( "numeric" ) ).toBe( [ "1", "9", "10", "100" ] );
			});

			it( title="an invalid sort type lists the available ones", body=function( currentSpec ) {
				expect( function() {
					arraySort( [ "a" ], "naturalx" );
				}).toThrow( message="natural" );
			});

			it( title="listSort natural", body=function( currentSpec ) {
				expect( listSort( "foo100,foo1,foo10,foo2,foo20", "natural" ) ).toBe( "foo1,foo2,foo10,foo20,foo100" );
				expect( listSort( "foo100,foo1,foo10,foo2,foo20", "natural", "desc" ) ).toBe( "foo100,foo20,foo10,foo2,foo1" );
				expect( listSort( "File10;file2;File1", "natural", "asc", ";" ) ).toBe( "File1;File10;file2" );
				expect( listSort( "File10;file2;File1", "naturalnocase", "asc", ";" ) ).toBe( "File1;file2;File10" );
				expect( "foo10,foo2".listSort( "natural" ) ).toBe( "foo2,foo10" );
			});

			it( title="structSort natural", body=function( currentSpec ) {
				var sct = { a: "file10", b: "file2", c: "file1", d: "File3" };
				expect( structSort( sct, "natural" ) ).toBe( [ "d", "c", "b", "a" ] );
				expect( structSort( sct, "naturalnocase" ) ).toBe( [ "c", "b", "d", "a" ] );
				expect( structSort( sct, "naturalnocase", "desc" ) ).toBe( [ "a", "d", "b", "c" ] );
				expect( sct.sort( "naturalnocase" ) ).toBe( [ "c", "b", "d", "a" ] );
			});

			it( title="structSort natural with a path to a sub element", body=function( currentSpec ) {
				var sct = { x: { name: "img12" }, y: { name: "img2" }, z: { name: "img1" } };
				expect( structSort( sct, "natural", "asc", "name" ) ).toBe( [ "z", "y", "x" ] );
			});

			it( title="structToSorted natural sorts by key", body=function( currentSpec ) {
				var sct = { "key10": 1, "key2": 2, "key1": 3, "Key3": 4 };
				expect( structKeyArray( structToSorted( sct, "natural" ) ) ).toBe( [ "Key3", "key1", "key2", "key10" ] );
				expect( structKeyArray( structToSorted( sct, "naturalnocase" ) ) ).toBe( [ "key1", "key2", "Key3", "key10" ] );
				expect( structKeyArray( structToSorted( sct, "naturalnocase", "desc" ) ) ).toBe( [ "key10", "Key3", "key2", "key1" ] );
				expect( structKeyArray( sct.toSorted( "natural" ) ) ).toBe( [ "Key3", "key1", "key2", "key10" ] );
			});

			it( title="locale sensitive argument does not break natural", body=function( currentSpec ) {
				var arr = [ "file10", "file2", "file1" ];
				arraySort( arr, "natural", "asc", true );
				expect( arr ).toBe( [ "file1", "file2", "file10" ] );
			});

		});
	}
}
