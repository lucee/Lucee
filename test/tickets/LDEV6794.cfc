component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	/*
	 * LDEV-6794 admin overview charts silently stop updating after a single failed chartAjax poll
	 *
	 * The poll loop lives in the javascript of the admin overview page and there is no browser in the
	 * test suite, so this checks the structure of the script: the next poll must be scheduled from the
	 * error path and from a finally block of the success handler (early return or exception included),
	 * not only at the end of the success handler.
	 * The behaviour itself (back off on failure, reset on success, stop after navigating away) was
	 * verified by running the extracted script against a stubbed jQuery.ajax.
	 */

	variables.overviewFile = getDirectoryFromPath( getCurrentTemplatePath() ) & "../../core/src/main/cfml/context/admin/overview.cfm";

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-6794, admin overview chart polling", function() {

			it( title="the admin overview source is available", body=function( currentSpec ) {
				expect( fileExists( variables.overviewFile ) ).toBeTrue( "missing #variables.overviewFile#" );
			});

			it( title="an error response schedules the next poll", body=function( currentSpec ) {
				var errorHandler = bodyAfter( requestDataBody(), "error: function" );
				expect( errorHandler ).toInclude( "scheduleNextRequest()" );
			});

			it( title="the success handler schedules the next poll in a finally block", body=function( currentSpec ) {
				var successHandler = bodyAfter( requestDataBody(), "success: function" );
				expect( successHandler ).toInclude( "finally" );
				var finallyBlock = bodyAfter( successHandler, "finally" );
				expect( finallyBlock ).toInclude( "scheduleNextRequest()" );
			});

			it( title="an unexpected (non object) response is not an early exit from the loop", body=function( currentSpec ) {
				var successHandler = bodyAfter( requestDataBody(), "success: function" );
				// every return has to be inside the try, the finally still reschedules
				expect( find( "return", successHandler ) ).toBeGT( find( "try", successHandler ) );
				expect( find( "return", successHandler ) ).toBeLT( find( "finally", successHandler ) );
			});

			it( title="there is a single place which sets the timer, and it respects navigating away", body=function( currentSpec ) {
				var src = fileRead( variables.overviewFile );
				expect( arrayLen( reMatch( "setTimeout\(requestData", src ) ) ).toBe( 1 );
				var scheduler = bodyAfter( src, "function scheduleNextRequest" );
				expect( scheduler ).toInclude( "chartTimer === null" );
				expect( scheduler ).toInclude( "setTimeout(requestData" );
			});

			it( title="failed polls back off but the delay is capped", body=function( currentSpec ) {
				var scheduler = bodyAfter( fileRead( variables.overviewFile ), "function scheduleNextRequest" );
				expect( scheduler ).toInclude( "chartPollMaxInterval" );
				expect( scheduler ).toInclude( "chartPollFailures" );
			});

		});
	}

	private string function requestDataBody() {
		return bodyAfter( fileRead( variables.overviewFile ), "function requestData" );
	}

	/**
	* returns the text between the first opening brace after the marker and its matching closing brace
	*/
	private string function bodyAfter( required string src, required string marker ) {
		var pos = find( arguments.marker, arguments.src );
		expect( pos ).toBeGT( 0, "[#arguments.marker#] not found" );
		var start = find( "{", arguments.src, pos );
		expect( start ).toBeGT( 0, "no block after [#arguments.marker#]" );
		var depth = 0;
		var len = len( arguments.src );
		for ( var i = start; i <= len; i++ ) {
			var c = mid( arguments.src, i, 1 );
			if ( c == "{" ) depth++;
			else if ( c == "}" ) {
				depth--;
				if ( depth == 0 ) return mid( arguments.src, start + 1, i - start - 1 );
			}
		}
		throw( message="unbalanced braces after [#arguments.marker#]" );
	}

}
