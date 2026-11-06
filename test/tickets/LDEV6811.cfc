component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults, testBox ) {
		describe( "LDEV-6811 faster arrays review fixes", function() {

			it( title="arrayDeleteAt with index -1 throws instead of removing the last element", body=function( currentSpec ) {
				var arr = [ "a", "b", "c" ];
				var threw = false;
				try {
					arrayDeleteAt( arr, -1 );
				}
				catch ( any e ) {
					threw = true;
				}
				expect( threw ).toBeTrue();
				expect( arr ).toBe( [ "a", "b", "c" ] );
			});

			it( title="arrayDeleteAt with index 0 throws", body=function( currentSpec ) {
				var arr = [ "a", "b", "c" ];
				var threw = false;
				try {
					arrayDeleteAt( arr, 0 );
				}
				catch ( any e ) {
					threw = true;
				}
				expect( threw ).toBeTrue();
				expect( arr ).toBe( [ "a", "b", "c" ] );
			});

			it( title="pop still removes the last element", body=function( currentSpec ) {
				var arr = [ "a", "b", "c" ];
				expect( arr.pop() ).toBe( "c" );
				expect( arr ).toBe( [ "a", "b" ] );
			});

			it( title="List.subList returns a copy, not a live Slice view", body=function( currentSpec ) {
				var arr = [ "a", "b", "c", "d" ];
				var sub = arr.subList( 1, 3 ); // 0-based List API: elements at indexes 1 and 2 -> b, c
				expect( sub.size() ).toBe( 2 );
				expect( sub.get( 0 ) ).toBe( "b" );
				expect( sub.get( 1 ) ).toBe( "c" );
				// independent copy: mutating the parent leaves the sublist unchanged
				arrayAppend( arr, "e" );
				arrayDeleteAt( arr, 1 );
				expect( sub.size() ).toBe( 2 );
				expect( sub.get( 0 ) ).toBe( "b" );
				expect( sub.get( 1 ) ).toBe( "c" );
				// returned list allows structural modification (unlike the previous Slice)
				sub.add( "x" );
				expect( sub.size() ).toBe( 3 );
				expect( arrayLen( arr ) ).toBe( 4 );
			});

			it( title="List.addAll(index, collection) inserts the whole block at the index", body=function( currentSpec ) {
				var arr = [ "a", "b", "c", "d" ];
				var extra = createObject( "java", "java.util.ArrayList" ).init();
				extra.add( "X" );
				extra.add( "Y" );
				extra.add( "Z" );
				var changed = arr.addAll( 2, extra ); // 0-based: insert before "c"
				expect( changed ).toBeTrue();
				expect( arr ).toBe( [ "a", "b", "X", "Y", "Z", "c", "d" ] );
			});

			it( title="List.addAll(index, collection) with an empty collection leaves the array unchanged", body=function( currentSpec ) {
				var arr = [ "a", "b" ];
				var changed = arr.addAll( 1, createObject( "java", "java.util.ArrayList" ).init() );
				expect( changed ).toBeFalse();
				expect( arr ).toBe( [ "a", "b" ] );
			});

		});
	}
}
