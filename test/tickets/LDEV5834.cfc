component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfexecute" {
	function run( testResults, textbox ) {
		describe("Test case for LDEV-5834 - cfexecute handlers lose function scope", function() {

			it( title = "cfexecute onProgress handler should have access to local variables", skip = isWindows(), body = function( currentSpec ) {
				var localVar = "localValue";
				var capturedLocal = "";

				cfexecute(
					name = "echo",
					arguments = "test output",
					timeout = 1,
					onProgress = function(line, process) {
						// This should be able to access localVar
						capturedLocal = localVar;
						return true;
					}
				);

				expect( capturedLocal ).toBe( "localValue" );
			});

			it( title = "cfexecute onError handler should have access to local variables", skip = isWindows(), body = function( currentSpec ) {
				var localVar = "localValue";
				var hasError = false;
				var capturedLocal = "";

				// ls writes to stderr for a missing path, so onError gets called; the non zero exit code throws
				try {
					cfexecute(
						name = "ls",
						arguments = "/ldev5834-#createUUID()#",
						timeout = 5,
						onError = function( errorLine ) {
							hasError = true;
							capturedLocal = localVar;
							return true;
						}
					);
				}
				catch ( any e ) {}

				expect( hasError ).toBe( true );
				expect( capturedLocal ).toBe( "localValue" );
			});

			it( title = "cfexecute with only onError still returns the output", skip = isWindows(), body = function( currentSpec ) {
				var called = false;
				cfexecute(
					name = "echo",
					arguments = "ldev5834",
					variable = "local.out",
					timeout = 5,
					onError = function( errorLine ) {
						called = true;
						return true;
					}
				);
				expect( trim( out ) ).toBe( "ldev5834" );
				expect( called ).toBe( false );
			});

			it( title = "cfexecute handlers should have access to function arguments", skip = isWindows(), body = function( currentSpec ) {
				runTestWithArgs("testValue", "testFilter");
			});

			it( title = "cfexecute handlers should be able to modify variables in parent scope", skip = isWindows(), body = function( currentSpec ) {
				variables.sharedVar = "initial";
				var capturedValue = "";

				cfexecute(
					name = "echo",
					arguments = "test",
					timeout = 1,
					onProgress = function(line, process) {
						// Should be able to read and modify variables scope
						capturedValue = variables.sharedVar;
						variables.sharedVar = "modified";
						return true;
					}
				);

				expect( capturedValue ).toBe( "initial" );
				expect( variables.sharedVar ).toBe( "modified" );
			});
		});
	}

	private function runTestWithArgs(required string argValue, required string argFilter) {
		var capturedArg1 = "";
		var capturedArg2 = "";

		cfexecute(
			name = "echo",
			arguments = "test",
			timeout = 1,
			onProgress = function(line, process) {
				// inside the closure "arguments" is the closure's own scope (line, process),
				// the arguments of the enclosing function are reached unscoped
				capturedArg1 = argValue;
				capturedArg2 = argFilter;
				return true;
			}
		);

		expect( capturedArg1 ).toBe( arguments.argValue );
		expect( capturedArg2 ).toBe( arguments.argFilter );
	}

	private boolean function isWindows() {
		return findNoCase( "windows", server.os.name ) > 0;
	}
}
