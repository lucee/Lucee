component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function run() {
		describe("cfexecute", function() {

			it(title="cfexecute progress listeners",  body=function() {
				
				var exe = isWindows() ? "cmd" : "bash";
				var args = isWindows() ? "/c dir" : "-c 'ls -lH'";

				var onErrorListener = function( error ){
					systemOutput("ERROR " & arguments.error, true, true );
				};

				var onProgressListener = function( output ){
					systemOutput("PROGRESS " & arguments.output, true );
				};

				var dir = getDirectoryFromPath(getCurrentTemplatePath());

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result", 
					onError=onErrorListener, 
					onProgress=onProgressListener
				);
				expect( result.exitCode ).toBe( 0 ); 

			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}