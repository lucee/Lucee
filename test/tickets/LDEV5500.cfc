component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function run() {
		describe("cfexecute", function() {
			
			it(title="cfexecute result variable with exit code",  body=function() {
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c exit /B 7" : "-c 'exit 7'";

				cfexecute(name=exe, timeout="1", arguments=args , result="local.result");
				expect( result.exitCode ).toBe( 7 );
				expect( result ).toHaveKey( "output" );
				expect( result ).toHaveKey( "error" );
			});

			it(title="cfexecute exitCode variable",  body=function() {
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c exit /B 7" : "-c 'exit 7'";

				cfexecute(name=exe, timeout="1", arguments=args , variable="local.result", exitCodeVariable="local.exitCode");
				expect( exitCode ).toBe( 7 );

			});

			it(title="cfexecute exitCode variable doesn't leak into the next call, LDEV-6725",  body=function() {
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c exit /B 7" : "-c 'exit 7'";

				cfexecute(name=exe, timeout="1", arguments=args , variable="local.result", exitCodeVariable="local.first");
				expect( first ).toBe( 7 );
				structDelete( local, "first" );

				var echoArgs = isWindows() ? "/c echo hello" : "-c 'echo hello'";
				savecontent variable="local.out" {
					cfexecute(name=exe, timeout="1", arguments=echoArgs);
				}
				expect( trim( out ) ).toBe( "hello" );
				expect( local ).notToHaveKey( "first" );
			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}