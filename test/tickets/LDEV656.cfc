component extends="org.lucee.cfml.test.LuceeTestCase" labels="operator" {

	function run( testResults, testBox ) {
		describe( "LDEV-656 compound assignment and increment on a java.util.Map", function() {

			function newMap() {
				var map = createObject( "java", "java.util.LinkedHashMap" ).init();
				map.put( "foo", 10 );
				return map;
			}

			it( "supports +=", function() {
				var map = newMap();
				map[ "foo" ] += 5;
				expect( map.get( "foo" ) ).toBe( 15 );
			});

			it( "supports -=", function() {
				var map = newMap();
				map[ "foo" ] -= 4;
				expect( map.get( "foo" ) ).toBe( 6 );
			});

			it( "supports *= and /=", function() {
				var map = newMap();
				map[ "foo" ] *= 3;
				expect( map.get( "foo" ) ).toBe( 30 );
				map[ "foo" ] /= 2;
				expect( map.get( "foo" ) ).toBe( 15 );
			});

			it( "supports post increment and decrement", function() {
				var map = newMap();
				var before = map[ "foo" ]++;
				expect( before ).toBe( 10 );
				expect( map.get( "foo" ) ).toBe( 11 );
				map[ "foo" ]--;
				expect( map.get( "foo" ) ).toBe( 10 );
			});

			it( "supports pre increment and decrement", function() {
				var map = newMap();
				var after = ++map[ "foo" ];
				expect( after ).toBe( 11 );
				expect( map.get( "foo" ) ).toBe( 11 );
				--map[ "foo" ];
				expect( map.get( "foo" ) ).toBe( 10 );
			});

			it( "supports &= on a map entry", function() {
				var map = createObject( "java", "java.util.LinkedHashMap" ).init();
				map.put( "foo", "a" );
				map[ "foo" ] &= "b";
				expect( map.get( "foo" ) ).toBe( "ab" );
			});

			it( "still works on a struct", function() {
				var sct = { "foo": 10 };
				sct[ "foo" ] += 5;
				sct[ "foo" ]++;
				expect( sct.foo ).toBe( 16 );
			});
		});
	}
}
