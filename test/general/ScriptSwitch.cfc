component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "script switch", function() {

			it( title="the switch expression is evaluated first, then cases in source order until one matches", body=function() {
				var log = [];
				var track = function( value, label ) {
					arrayAppend( log, label );
					return value;
				};
				var r = "";
				switch ( track( "b", "expr" ) ) {
					case track( "a", "c1" ): r = "c1"; break;
					case track( "b", "c2" ): r = "c2"; break;
					case track( "c", "c3" ): r = "c3"; break;
				}
				expect( r ).toBe( "c2" );
				expect( arrayToList( log ) ).toBe( "expr,c1,c2" );
			});

			it( title="every case is evaluated when nothing matches", body=function() {
				var log = [];
				var track = function( value, label ) {
					arrayAppend( log, label );
					return value;
				};
				var r = "";
				switch ( track( "z", "expr" ) ) {
					case track( "a", "c1" ): r = "c1"; break;
					case track( "b", "c2" ): r = "c2"; break;
					default: r = "default";
				}
				expect( r ).toBe( "default" );
				expect( arrayToList( log ) ).toBe( "expr,c1,c2" );
			});

			it( title="a case after the match is not evaluated, even if it would throw", body=function() {
				var r = "";
				switch ( "a" ) {
					case "a": r = "a"; break;
					case undefinedVariableForScriptSwitch: r = "undefined"; break;
				}
				expect( r ).toBe( "a" );
			});

			it( title="switch( true ) stops at the first true guard", body=function() {
				var log = [];
				var guard = function( value, label ) {
					arrayAppend( log, label );
					return value;
				};
				var r = "";
				switch ( true ) {
					case guard( false, "g1" ): r = "g1"; break;
					case guard( true, "g2" ): r = "g2"; break;
					case guard( true, "g3" ): r = "g3"; break;
				}
				expect( r ).toBe( "g2" );
				expect( arrayToList( log ) ).toBe( "g1,g2" );
			});

			it( title="stacked case labels share a body", body=function() {
				var r = "";
				switch ( "b" ) {
					case "a":
					case "b":
					case "c": r = "abc"; break;
					default: r = "default";
				}
				expect( r ).toBe( "abc" );
			});

			it( title="fall through from the last case runs default", body=function() {
				var r = "";
				switch ( 1 ) {
					case 1: r &= "1";
					default: r &= "d";
				}
				expect( r ).toBe( "1d" );
			});

			it( title="first match wins when two case expressions hold the same value", body=function() {
				var a = "x";
				var b = "x";
				var r = "";
				switch ( "x" ) {
					case a: r = "a"; break;
					case b: r = "b"; break;
				}
				expect( r ).toBe( "a" );
			});

			it( title="string match is case-insensitive", body=function() {
				var r = "";
				switch ( "ABC" ) {
					case "abc": r = "abc"; break;
					default: r = "default";
				}
				expect( r ).toBe( "abc" );
			});

			it( title="numeric and numeric string match both ways, small and large literals", body=function() {
				expect( numberSwitch( "1" ) ).toBe( "one" );
				expect( numberSwitch( 1 ) ).toBe( "one" );
				expect( numberSwitch( "21" ) ).toBe( "twentyone" );
				expect( numberSwitch( 21 ) ).toBe( "twentyone" );
				expect( numberSwitch( 1000 ) ).toBe( "thousand" );
				expect( numberSwitch( "1000" ) ).toBe( "thousand" );
				expect( numberSwitch( 2 ) ).toBe( "default" );

				var r = "";
				switch ( 1 ) {
					case "1": r = "string one"; break;
					default: r = "default";
				}
				expect( r ).toBe( "string one" );
			});

			it( title="switch( true ) with boolean case expressions, first true wins", body=function() {
				var str = "bmw-audi-house";
				var r = "";
				switch ( true ) {
					case reFind( "^house", str ) > 0: r = "house"; break;
					case reFind( "^car", str ) > 0: r = "car"; break;
					case reFind( "^audi|^bmw", str ) > 0: r = "audi & bmw"; break;
					case reFind( "house", str ) > 0: r = "contains house"; break;
					default: r = "nothing";
				}
				expect( r ).toBe( "audi & bmw" );
			});

			it( title="boolean switch value matches numeric cases", body=function() {
				var r = "";
				switch ( true ) {
					case 0: r = "zero"; break;
					case 1: r = "one"; break;
					default: r = "default";
				}
				expect( r ).toBe( "one" );

				switch ( false ) {
					case 1: r = "one"; break;
					case 0: r = "zero"; break;
					default: r = "default";
				}
				expect( r ).toBe( "zero" );
			});

			it( title="default is taken when nothing matches", body=function() {
				var r = "";
				switch ( "z" ) {
					case "a": r = "a"; break;
					case "b": r = "b"; break;
					default: r = "default";
				}
				expect( r ).toBe( "default" );
			});

			it( title="no match and no default runs nothing", body=function() {
				var r = "untouched";
				switch ( "z" ) {
					case "a": r = "a"; break;
				}
				expect( r ).toBe( "untouched" );
			});

			it( title="only a default", body=function() {
				var r = "";
				switch ( "z" ) {
					default: r = "default";
				}
				expect( r ).toBe( "default" );
			});

			it( title="falls through without break", body=function() {
				var r = "";
				switch ( 1 ) {
					case 1: r &= "1";
					case 2: r &= "2";
					case 3: r &= "3"; break;
					case 4: r &= "4";
				}
				expect( r ).toBe( "123" );
			});

			it( title="break leaves the switch, not the enclosing loop", body=function() {
				var r = "";
				loop from=1 to=3 index="local.i" {
					switch ( i ) {
						case 2: r &= "two"; break;
						default: r &= i;
					}
				}
				expect( r ).toBe( "1two3" );
			});

			it( title="continue inside a switch continues the enclosing loop", body=function() {
				var r = "";
				loop from=1 to=3 index="local.i" {
					switch ( i ) {
						case 2: continue;
						default: r &= i;
					}
					r &= ";";
				}
				expect( r ).toBe( "1;3;" );
			});

			it( title="a null case value is skipped", body=function() {
				var r = "";
				switch ( "" ) {
					case nullValue(): r = "null"; break;
					default: r = "default";
				}
				expect( r ).toBe( "default" );
			});

			it( title="a complex case value is skipped, not thrown", body=function() {
				var s = {};
				var r = "";
				switch ( "a" ) {
					case s: r = "struct"; break;
					case "a": r = "a"; break;
				}
				expect( r ).toBe( "a" );
			});

			it( title="a complex switch value goes to default, not thrown", body=function() {
				var s = { a: 1 };
				var r = "";
				switch ( s ) {
					case "a": r = "a"; break;
					default: r = "default";
				}
				expect( r ).toBe( "default" );
			});

		});
	}

	private function numberSwitch( required any value ) {
		switch ( arguments.value ) {
			case 1: return "one";
			case 21: return "twentyone";
			case 1000: return "thousand";
			default: return "default";
		}
	}

}
