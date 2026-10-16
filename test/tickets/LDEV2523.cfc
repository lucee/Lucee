component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {
	function run() {
		describe( title = "Test suite for LDEV2523", body = function() {
			it( title = 'Test case for Array map member function skips null values',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = arraymap(a,function(item) {
					return "lucee";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("lucee",b[1]);
			});

			it( title = 'Test case for Array map member function skips null values for member function',body = function( currentSpec ) {
				a = [JavaCast("null","")];
				b = a.map(function(item) {
					return "lucee_core_dev";
				});
				assertEquals("1",arraylen(a));
				assertEquals("false",arrayisempty(b));
				assertEquals("lucee_core_dev",b[1]);
			});

			it( title = 'map() visits null elements between other elements', body = function( currentSpec ) {
				var a = [ 1, nullValue(), 3, nullValue() ];
				var b = a.map( function( item ) {
					return isNull( arguments.item ) ? "null" : arguments.item;
				});
				expect( arrayLen( b ) ).toBe( 4 );
				expect( arrayToList( b ) ).toBe( "1,null,3,null" );
			});
		});
	}
}
