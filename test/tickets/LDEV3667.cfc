component extends="org.lucee.cfml.test.LuceeTestCase" labels="execute" {

	function beforeAll(){
		variables.exe = isWindows() ? "cmd" : "bash";
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath());
	}

	function run() {
		describe("cfexecute with onError and onProgress", function() {

			beforeEach(function(currentSpec){
				_logger("", true);
				_logger(currentSpec, true);
			});

			it(title="cfexecute progress and error listeners",  body=function() {

				var args = isWindows() ? "/c dir Issue*.cfc" : "-c 'ls -lH Issue*.cfc'";

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result",
					onError=onErrorListener,
					onProgress=onProgressListener
				);
				expect( result.exitCode ).toBe( 0 );

			});

			it(title="cfexecute progress and error listeners, with error",  body=function() {

				var args = isWindows() ? "/c dir xxx" : "-c 'cat xxxx'";

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result",
					onError=onErrorListener,
					onProgress=onProgressListener
				);
				expect( result.exitCode ).toBe( 1 );

			});
		});

		// when there is a only an onProgress listener, the error stream is combined
		describe("cfexecute only onProgress", function() {

			beforeEach(function(currentSpec){
				_logger("", true);
				_logger(currentSpec, true);
			});
	
			it(title="cfexecute only progress listeners",  body=function() {

				var args = isWindows() ? "/c dir Issue*.cfc" : "-c 'ls -lH Issue*.cfc'";

				var outputLog = [];
				var onCombinedProgressListener = function( output ){
					_logger(arguments, true );
					arrayAppend(outputLog, output);
					_logger("COMBINED PROGRESS " & arguments.output, true );
				};

				cfexecute(name=exe, timeout="3", arguments=args , directory=dir,
					result="local.result",
					onProgress=onCombinedProgressListener
				);
				expect( result.exitCode ).toBe( 0 );
				expect( outputLog ).notToBeEmpty();
			});

			it(title="cfexecute only progress listeners, with error",  body=function() {

				var args = isWindows() ? "/c echo hello && echo This is an error message 1>&2" : "-c 'echo hello && echo ''This is an error message'' >&2'";

				var outputLog = [];
				var onCombinedProgressListener = function( output ){
					arrayAppend( outputLog, output );
					_logger("COMBINED PROGRESS " & arguments.output, true );
				};

				cfexecute(name=exe, timeout="3", arguments=args , directory=dir,
					result="local.result",
					onProgress=onCombinedProgressListener
				);
				expect( result.exitCode ).toBe( 0 );
				expect( outputLog ).notToBeEmpty();
			});
		});
		
		// returning false from a listener will cancel the execution
		describe("cfexecute canceling via listener", function() {

			it(title="cfexecute progress and error listeners with cancel",  body=function() {

				var args = isWindows() ? "/c dir Issue*.cfc" : "-c 'ls -lH Issue*.cfc'";

				var outputLog = [];
				var onProgressListenerCancel = function ( output ){
					var out = arguments.output ?: "null";
					arrayAppend( outputLog, out );
					_logger("CANCEL PROGRESS " & out, true );
					return false; // returning false here, cancels process execution
				};

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result",
					onError=onErrorListener,
					onProgress=onProgressListenerCancel
				);
				// expect( result.exitCode ).toBe( 0 ); this can be 1 or 0.... hmmmm
				expect( arrayLen( outputLog ) ).toBe( 1 ); // as we cancel the process only the line is output

			});

			it(title="cfexecute progress listeners with cancel",  body=function() {

				var args = isWindows() ? "/c dir Issue*.cfc" : "-c 'ls -lH Issue*.cfc'";

				var outputLog = [];
				var onProgressListenerCancel = function ( output ){
					var out = arguments.output ?: "null";
					arrayAppend( outputLog, out );
					_logger("CANCEL PROGRESS " & out, true );
					return false; // returning false here, cancels process execution
				};

				cfexecute(name=exe, timeout="1", arguments=args , directory=dir,
					result="local.result",
					onProgress=onProgressListenerCancel
				);
				// expect( result.exitCode ).toBe( 0 ); this can be 1 or 0.... hmmmm
				expect( arrayLen( outputLog ) ).toBe( 1 ); // as we cancel the process only the line is output

			});
		});

	}

	private function _logger( mess, nl, err=false ){
		// uncomment for debugging
		systemOutput( mess, nl, err );
	}

	private function onErrorListener ( errorOutput ){
		var err = arguments.errorOutput ?: "null";
		_logger("ERROR " & err, true, true );
	};

	private function onProgressListener ( output ){
		var out = arguments.output ?: "null";
		_logger("PROGRESS " & out, true );
	};

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}