component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function run() {
		describe("cfexecute", function() {

			it(title="cfexecute progress listeners",  body=function() {
				
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c dir" : "-c 'ls'";

				var onError = function(error){
					systemOutput("ERROR " & arguments.error, true, true );
				};

				var onProgress = function( output ){
					systemOutput("PROGRESS " & arguments.output, true );
				};

				var dir = getDirectoryFromPath(getCurrentTemplatePath());

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result", onError=onError, onProgress=onProgress);
				expect( result.exitCode ).toBe( 0 ); 

			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}