component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-6510 - continue must not act like break when the same try/catch also contains a break", function() {

			it( title="for-in: continue in try, break in catch", body=function( currentSpec ) {
				expect( forInContinueInTryBreakInCatch() ).toBe( "a,b,c" );
			});

			it( title="index loop: continue in try, break in catch", body=function( currentSpec ) {
				expect( indexLoopContinueInTryBreakInCatch() ).toBe( "a,b,c" );
			});

			it( title="continue and break both in catch", body=function( currentSpec ) {
				expect( continueAndBreakInCatch() ).toBe( "a,b,c" );
			});

			it( title="control: continue without a break in the try/catch", body=function( currentSpec ) {
				expect( continueNoBreak() ).toBe( "a,b,c" );
			});

			it( title="while loop", body=function( currentSpec ) {
				expect( whileLoop() ).toBe( "a,b,c" );
			});

			it( title="do-while loop", body=function( currentSpec ) {
				expect( doWhileLoop() ).toBe( "a,b,c" );
			});

			it( title="try/catch/finally, the finally runs on every continue", body=function( currentSpec ) {
				expect( tryCatchFinally() ).toBe( "a,b,c fin=3" );
			});

			it( title="try/finally without catch", body=function( currentSpec ) {
				expect( tryFinally() ).toBe( "a,b,c fin=3" );
			});

			it( title="break still breaks when a continue follows it in the same try", body=function( currentSpec ) {
				expect( breakBeforeContinue() ).toBe( "a,b" );
			});

			it( title="nested loops: continue in the inner loop does not leave it", body=function( currentSpec ) {
				expect( nestedLoops() ).toBe( "1a,1b,1c,2a,2b,2c" );
			});

			it( title="nested try/finally blocks, every finally runs once per continue", body=function( currentSpec ) {
				expect( nestedTryFinally() ).toBe( "a,b,c fin=6" );
			});

			it( title="labeled continue to the outer loop", body=function( currentSpec ) {
				expect( labeledContinue() ).toBe( "1a,2a" );
			});

			it( title="labeled continue and labeled break of the same loop", body=function( currentSpec ) {
				expect( labeledSameLoop() ).toBe( "a,b,c" );
			});

			it( title="continue of the inner loop, break of the outer loop", body=function( currentSpec ) {
				expect( continueInnerBreakOuter() ).toBe( "1a,1b,1c,2a,2b,2c" );
			});

			it( title="break inside finally leaves the loop and does not run the finally again", body=function( currentSpec ) {
				expect( breakInFinally() ).toBe( "a,b fin=2" );
			});

			it( title="continue in try, break in finally", body=function( currentSpec ) {
				expect( continueInTryBreakInFinally() ).toBe( "a,b fin=2" );
			});

			it( title="retry, continue and break in the same try/catch", body=function( currentSpec ) {
				expect( retryContinueBreak() ).toBe( "a,b attempts=3" );
			});

			it( title="continue and break inside cfsilent (LDEV-935)", body=function( currentSpec ) {
				expect( silentContinueBreak() ).toBe( 3 );
			});

			it( title="tags: cfloop with cftry/cfcatch/cffinally", body=function( currentSpec ) {
				var result = _InternalRequest( template: createURI( "LDEV6510/tags.cfm" ) );
				expect( trim( result.fileContent ) ).toBe( "1,2,3|1,2,3 fin=3" );
			});

		});
	}

	private function forInContinueInTryBreakInCatch() {
		var trace = "";
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( true ) {
					continue;
				}
			}
			catch ( any e ) {
				break;
			}
		}
		return trace;
	}

	private function indexLoopContinueInTryBreakInCatch() {
		var trace = "";
		var items = [ "a", "b", "c" ];
		for ( var i = 1; i <= 3; i++ ) {
			trace = listAppend( trace, items[ i ] );
			try {
				if ( true ) {
					continue;
				}
			}
			catch ( any e ) {
				break;
			}
		}
		return trace;
	}

	private function continueAndBreakInCatch() {
		var trace = "";
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				throw( type="probe", message="x" );
			}
			catch ( any e ) {
				if ( true ) {
					continue;
				}
				break;
			}
		}
		return trace;
	}

	private function continueNoBreak() {
		var trace = "";
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( true ) {
					continue;
				}
			}
			catch ( any e ) {
				var x = 1;
			}
		}
		return trace;
	}

	private function whileLoop() {
		var trace = "";
		var items = [ "a", "b", "c" ];
		var i = 0;
		while ( i < 3 ) {
			i++;
			trace = listAppend( trace, items[ i ] );
			try {
				if ( true ) continue;
			}
			catch ( any e ) {
				break;
			}
		}
		return trace;
	}

	private function doWhileLoop() {
		var trace = "";
		var items = [ "a", "b", "c" ];
		var i = 0;
		do {
			i++;
			trace = listAppend( trace, items[ i ] );
			try {
				if ( true ) continue;
			}
			catch ( any e ) {
				break;
			}
		} while ( i < 3 );
		return trace;
	}

	private function tryCatchFinally() {
		var trace = "";
		var fin = 0;
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( true ) continue;
			}
			catch ( any e ) {
				break;
			}
			finally {
				fin++;
			}
		}
		return trace & " fin=" & fin;
	}

	private function tryFinally() {
		var trace = "";
		var fin = 0;
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( true ) continue;
				if ( false ) break;
			}
			finally {
				fin++;
			}
		}
		return trace & " fin=" & fin;
	}

	private function breakBeforeContinue() {
		var trace = "";
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( item == "b" ) break;
				continue;
			}
			catch ( any e ) {
				continue;
			}
		}
		return trace;
	}

	private function nestedLoops() {
		var trace = "";
		for ( var o in [ "1", "2" ] ) {
			for ( var item in [ "a", "b", "c" ] ) {
				trace = listAppend( trace, o & item );
				try {
					if ( true ) continue;
				}
				catch ( any e ) {
					break;
				}
			}
		}
		return trace;
	}

	private function nestedTryFinally() {
		var trace = "";
		var fin = 0;
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				try {
					if ( true ) continue;
				}
				catch ( any e ) {
					break;
				}
				finally {
					fin++;
				}
			}
			finally {
				fin++;
			}
		}
		return trace & " fin=" & fin;
	}

	private function labeledContinue() {
		var trace = "";
		outer: for ( var o in [ "1", "2" ] ) {
			for ( var item in [ "a", "b" ] ) {
				trace = listAppend( trace, o & item );
				try {
					if ( item == "a" ) continue outer;
				}
				catch ( any e ) {
					break;
				}
			}
		}
		return trace;
	}

	private function labeledSameLoop() {
		var trace = "";
		items: for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( true ) continue items;
			}
			catch ( any e ) {
				break items;
			}
		}
		return trace;
	}

	private function continueInnerBreakOuter() {
		var trace = "";
		outer: for ( var o in [ "1", "2" ] ) {
			for ( var item in [ "a", "b", "c" ] ) {
				trace = listAppend( trace, o & item );
				try {
					if ( true ) continue;
				}
				catch ( any e ) {
					break outer;
				}
			}
		}
		return trace;
	}

	private function breakInFinally() {
		var trace = "";
		var fin = 0;
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				var x = 1;
			}
			finally {
				fin++;
				// guard, so a broken build fails instead of looping forever
				if ( fin > 20 ) throw( type="LDEV6510", message="the finally block runs in an endless loop" );
				if ( item == "b" ) break;
			}
		}
		return trace & " fin=" & fin;
	}

	private function continueInTryBreakInFinally() {
		var trace = "";
		var fin = 0;
		for ( var item in [ "a", "b", "c" ] ) {
			trace = listAppend( trace, item );
			try {
				if ( item == "a" ) continue;
			}
			finally {
				fin++;
				if ( fin > 20 ) throw( type="LDEV6510", message="the finally block runs in an endless loop" );
				if ( item == "b" ) break;
			}
		}
		return trace & " fin=" & fin;
	}

	private function retryContinueBreak() {
		var trace = "";
		var attempts = 0;
		for ( var item in [ "a", "b" ] ) {
			try {
				attempts++;
				if ( attempts == 1 ) throw( type="probe", message="first attempt" );
				trace = listAppend( trace, item );
				continue;
			}
			catch ( any e ) {
				if ( attempts < 2 ) retry;
				break;
			}
		}
		return trace & " attempts=" & attempts;
	}

	private function silentContinueBreak() {
		var count = 0;
		for ( var i = 1; i <= 3; i++ ) {
			silent {
				count++;
				continue;
				if ( false ) break;
			}
		}
		return count;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
