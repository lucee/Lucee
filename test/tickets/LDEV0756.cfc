component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-756 nested try-catch inside finally", function() {

			it( title="try/return + finally with empty try-catch (ticket repro)", body=function() {
				expect( broken() ).toBe( 1 );
			});

			it( title="try/return + finally with try-catch that has a body", body=function() {
				expect( brokenWithBody() ).toBe( 2 );
			});

			it( title="try/throw + finally with try-catch still rethrows", body=function() {
				var caught = false;
				try { throwsThenFinally(); }
				catch ( any e ) { caught = true; }
				expect( caught ).toBeTrue();
			});

			it( title="try/return + finally with nested try/finally", body=function() {
				expect( nestedFinally() ).toBe( 3 );
			});

			it( title="try/catch/finally where finally has try-catch", body=function() {
				expect( withCatchAndFinally() ).toBe( "ok" );
			});

			it( title="double-nested try-catch in finally", body=function() {
				expect( doubleNested() ).toBe( 4 );
			});

			it( title="finally try-catch that itself returns is invalid path but compile with assign", body=function() {
				expect( finallySetsVar() ).toBe( 5 );
			});
		});
	}

	private function broken() {
		try { return 1; }
		finally { try { } catch (Any e) {} }
	}

	private function brokenWithBody() {
		try { return 2; }
		finally {
			try { var x = 1; }
			catch ( any e ) { var y = e; }
		}
	}

	private function throwsThenFinally() {
		try { throw "boom"; }
		finally { try { } catch ( any e ) {} }
	}

	private function nestedFinally() {
		try { return 3; }
		finally {
			try { var a = 1; }
			finally { var b = 2; }
		}
	}

	private function withCatchAndFinally() {
		try { throw "x"; }
		catch ( any e ) { /* swallow */ }
		finally { try { } catch ( any e2 ) {} }
		return "ok";
	}

	private function doubleNested() {
		try { return 4; }
		finally {
			try {
				try { } catch ( any e ) {}
			}
			catch ( any e2 ) {}
		}
	}

	private function finallySetsVar() {
		var out = 0;
		try { out = 5; }
		finally {
			try { out = out; }
			catch ( any e ) { out = -1; }
		}
		return out;
	}
}
