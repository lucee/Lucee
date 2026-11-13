component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	function run( testResults, testBox ) {
		describe( "LDEV-5064 struct entrySet/keySet/values are views", function() {
			it( "entrySet().iterator().remove() removes from the struct", function() {
				var s = { foo: "bar", x: 1 };
				var it = s.entrySet().iterator();
				while ( it.hasNext() ) { it.next(); it.remove(); }
				expect( structCount( s ) ).toBe( 0 );
			});
			it( "keySet().iterator().remove() removes from the struct", function() {
				var s = { foo: "bar" };
				var it = s.keySet().iterator();
				while ( it.hasNext() ) { it.next(); it.remove(); }
				expect( structCount( s ) ).toBe( 0 );
			});
			it( "entry.setValue writes through", function() {
				var s = { foo: "bar" };
				var it = s.entrySet().iterator();
				var e = it.next(); e.setValue( "baz" );
				expect( s.foo ).toBe( "baz" );
			});
			it( "ordered struct", function() {
				var s = [ a: 1, b: 2 ];
				var it = s.entrySet().iterator();
				while ( it.hasNext() ) { it.next(); it.remove(); }
				expect( structCount( s ) ).toBe( 0 );
			});
		});
	}
}
