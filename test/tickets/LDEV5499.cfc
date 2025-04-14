component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function run() {
		describe("cfexecute", function() {

			it(title="cfexecute environment windows",  body=function() {
				var env = {
					"LUCEE": "rocks",
					"LUCEE_LDEV5499_RANDOM": createGUID()
				};

				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? ["/c", "set"] : "-c 'set'";

				cfexecute(name=exe, timeout="1", arguments=args , environment=env, variable="variables.result");
				expect( find( env[ "LUCEE" ], result ) ).toBeGT( 0 ); // don't leak env
				expect( find( env[ "LUCEE_LDEV5499_RANDOM" ], result ) ).toBeGT( 0 ); // don't leak env

			});

			it(title="cfexecute environment should throw when environment is an array",  body=function() {
				var env = [ "LUCEE=rocks", "LUCEE_LDEV5499_RANDOM=#createGUID()#" ];

				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c set" : "-c 'set'";

				expect (function(){
					cfexecute(name=exe, timeout="1", arguments=args , environment=env, variable="variables.result");
				}).toThrow(); // should be a struct

			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}