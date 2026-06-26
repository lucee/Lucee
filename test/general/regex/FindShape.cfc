// reFind return shape: scalar position by default, struct with pos[]/len[]/match[]
// arrays when returnsubexpressions=true, array of structs when scope="all".
// Locks both the success shape and the no-match shape.
// Supersedes test/tickets/LDEV3532.cfc (no-match struct shape) and
// test/tickets/LDEV3703.cfc (.* trailing-empty divergence on java).
component extends="org.lucee.cfml.test.LuceeTestCase" labels="regex" {

	function beforeTests() { variables._regex = new _engines(); variables._regex.setup( this ); }
	function afterTests()  { variables._regex.teardown(); }


	public void function testScalarPositionDefault() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 6, reFind( "[0-9]+", "test 123!" ) );
		});
	}

	public void function testScalarReturnsZeroOnNoMatch() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( 0, reFind( "[0-9]+", "no digits here" ) );
		});
	}

	public void function testStructShapeOnMatch() {
		variables._regex.eachEngine( function( engine ) {
			var r = reFind( "(f)(oo)", "foobar", 1, true );
			assertEquals( 3, structCount( r ) );
			assertEquals( 3, arrayLen( r.pos ) );
			assertEquals( 3, arrayLen( r.len ) );
			assertEquals( 3, arrayLen( r.match ) );
			assertEquals( "foo", r.match[ 1 ] );
			assertEquals( "f",   r.match[ 2 ] );
			assertEquals( "oo",  r.match[ 3 ] );
		});
	}

	public void function testStructShapeOnNoMatch() {
		variables._regex.eachEngine( function( engine ) {
			// no-match shape: zeros + empty string, single-element arrays
			var r = reFind( "(f)(oo)", "bar", 1, true );
			assertEquals( 3, structCount( r ) );
			assertEquals( 0,  r.len[ 1 ] );
			assertEquals( 0,  r.pos[ 1 ] );
			assertEquals( "", r.match[ 1 ] );
		});
	}

	public void function testScopeAllReturnsArrayOfStructs() {
		variables._regex.eachEngine( function( engine ) {
			var r = reFind( "[0-9]+", "1 cat 22 dogs 333 fish", 1, true, "all" );
			assertEquals( 3, arrayLen( r ) );
			assertEquals( "1",   r[ 1 ].match[ 1 ] );
			assertEquals( "22",  r[ 2 ].match[ 1 ] );
			assertEquals( "333", r[ 3 ].match[ 1 ] );
		});
	}

	public void function testScopeAllNoMatchShape() {
		variables._regex.eachEngine( function( engine ) {
			var r = reFind( "(f)(oo)", "bar", 1, true, "all" );
			assertEquals( 1, arrayLen( r ) );
			assertEquals( 0,  r[ 1 ].len[ 1 ] );
			assertEquals( 0,  r[ 1 ].pos[ 1 ] );
			assertEquals( "", r[ 1 ].match[ 1 ] );
		});
	}

	public void function testReMatchArrayShape() {
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "1", "22", "333" ], reMatch( "[0-9]+", "1 cat 22 dogs 333 fish" ) );
		});
	}

	public void function testDotStarEmptyInputDivergence() {
		// LDEV-3703: java appends a trailing empty match for non-empty input.
		// Empty-string input is [""] on both engines.
		variables._regex.eachEngine( function( engine ) {
			assertEquals( [ "" ], reMatchNoCase( ".*", "" ) );

			var rSpace = reMatchNoCase( ".*", " " );
			if ( engine == "perl" ) assertEquals( [ " " ],      rSpace );
			else                    assertEquals( [ " ", "" ],  rSpace );
		});
	}
}
