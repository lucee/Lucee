component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {

		describe( "LDEV-3168 - labelled loops (ACF 2021)", function() {

			it( title="continue label skips to the next iteration of the labelled outer loop", body=function( currentSpec ) {
				var out = [];
				xyz : for ( var i = 110; i < 115; i++ ) {
					abc : for ( var j = 10; j < 20; j++ ) {
						out.append( "i=#i#" );
						if ( j % 3 == 0 )
							continue xyz;
						out.append( "j=#j#" );
					}
					out.append( "after-inner" ); // never reached, every inner loop continues the outer one
				}
				expect( out.toList() ).toBe(
					"i=110,j=10,i=110,j=11,i=110,"
					& "i=111,j=10,i=111,j=11,i=111,"
					& "i=112,j=10,i=112,j=11,i=112,"
					& "i=113,j=10,i=113,j=11,i=113,"
					& "i=114,j=10,i=114,j=11,i=114" );
			});

			it( title="break label inside a switch exits the labelled while loop, not just the switch", body=function( currentSpec ) {
				var out = [];
				var x = 0;
				WhileLabel: while ( x < 10 ) {
					out.append( "x is #x#" );
					switch ( x ) {
						case 1:
							break;
						case 3:
							break WhileLabel;
					}
					x++;
					out.append( "end of loop" );
				}
				out.append( "After loop, x is #x#" );
				expect( out.toList( "|" ) ).toBe( "x is 0|end of loop|x is 1|end of loop|x is 2|end of loop|x is 3|After loop, x is 3" );
				expect( x ).toBe( 3 );
			});

			it( title="break without a label inside a switch only exits the switch", body=function( currentSpec ) {
				var out = [];
				var x = 0;
				while ( x < 3 ) {
					switch ( x ) {
						case 1:
							break;
					}
					out.append( x );
					x++;
				}
				expect( out.toList() ).toBe( "0,1,2" );
			});

			it( title="break label exits the labelled outer for loop", body=function( currentSpec ) {
				var out = [];
				outer: for ( var i = 1; i <= 3; i++ ) {
					for ( var j = 1; j <= 3; j++ ) {
						if ( j == 2 )
							break outer;
						out.append( "i#i#j#j#" );
					}
				}
				expect( out.toList() ).toBe( "i1j1" );
			});

			it( title="break and continue without a label still only affect the innermost loop", body=function( currentSpec ) {
				var out = [];
				outer: for ( var i = 1; i <= 2; i++ ) {
					for ( var j = 1; j <= 3; j++ ) {
						if ( j == 2 )
							continue;
						if ( j == 3 )
							break;
						out.append( "i#i#j#j#" );
					}
				}
				expect( out.toList() ).toBe( "i1j1,i2j1" );
			});

			it( title="labels on do-while and for-in loops", body=function( currentSpec ) {
				var out = [];
				var k = 0;
				dw: do {
					k++;
					if ( k == 3 )
						continue dw;
					if ( k > 5 )
						break dw;
					out.append( "k#k#" );
				} while ( k < 10 );
				expect( out.toList() ).toBe( "k1,k2,k4,k5" );

				out = [];
				fi: for ( var item in [ 1, 2, 3, 4 ] ) {
					for ( var n in [ 1, 2 ] ) {
						if ( item == 2 )
							continue fi;
						if ( item == 4 )
							break fi;
						out.append( "item#item#n#n#" );
					}
				}
				expect( out.toList() ).toBe( "item1n1,item1n2,item3n1,item3n2" );
			});

			it( title="labels work with whitespace variations around the colon", body=function( currentSpec ) {
				var out = [];
				lb1:for ( var i = 1; i <= 3; i++ ) {
					for ( var j = 1; j <= 2; j++ ) {
						if ( i == 2 )
							continue lb1;
						out.append( "i#i#j#j#" );
					}
				}
				lb2 :while ( true ) {
					out.append( "lb2" );
					break lb2;
				}
				lb3
				:
				for ( var i = 1; i <= 2; i++ ) {
					out.append( "lb3-#i#" );
					break lb3;
				}
				expect( out.toList() ).toBe( "i1j1,i1j2,i3j1,i3j2,lb2,lb3-1" );
			});

			it( title="labelled loop inside a function", body=function( currentSpec ) {
				expect( labelledLoopInFunction() ).toBe( "a1b1,a2b1,a3" );
			});

			it( title="a label does not clash with a function listener call (x : function() {}) in the same template", body=function( currentSpec ) {
				var out = [];
				// function listener syntax, [call() : function() {}]
				var listener = getOne():function( result ) {};
				expect( listener ).notToBeEmpty();
				lbl: for ( var i = 1; i <= 3; i++ ) {
					out.append( getOne() );
					if ( i == 2 )
						break lbl;
				}
				expect( out.toList() ).toBe( "1,1" );
			});

			it( title="a variable with the same name as the label is not touched", body=function( currentSpec ) {
				var out = [];
				var same = 5;
				same: for ( var i = 1; i <= 3; i++ ) {
					if ( i == 1 )
						continue same;
					out.append( "i#i#" );
				}
				expect( out.toList() ).toBe( "i2,i3" );
				expect( same ).toBe( 5 );
			});

			it( title="break to a label no enclosing loop carries is a compile time error naming the label", body=function( currentSpec ) {
				var uri = createURI( "LDEV3168/unknownLabel.cfm" );
				expect( function() {
					_InternalRequest( template: uri );
				} ).toThrow( regex="with the label \[unknown\]" );
			});

		});
	}

	private string function labelledLoopInFunction() {
		var out = [];
		outer: for ( var a = 1; a <= 3; a++ ) {
			for ( var b = 1; b <= 3; b++ ) {
				if ( a == 3 ) {
					out.append( "a3" );
					break outer;
				}
				if ( b == 2 )
					continue outer;
				out.append( "a#a#b#b#" );
			}
		}
		return out.toList();
	}

	private function getOne() {
		return 1;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
