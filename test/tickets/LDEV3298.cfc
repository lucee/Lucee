component extends="org.lucee.cfml.test.LuceeTestCase" labels="forward" {

	function run( testResults, testBox ) {
		describe( "LDEV-3298 cfforward ends the request like cflocation", function() {

			it( title="code after cfforward does not run, the forwarded output is delivered", body=function() {
				var result = request( "basic.cfm", "forward" );
				expect( result ).toInclude( "[TARGET]" );
				expect( result ).notToInclude( "[AFTER]" );
			} );

			it( title="script form of the forward tag ends the request", body=function() {
				var result = request( "scriptForm.cfm", "forward" );
				expect( result ).toInclude( "[TARGET]" );
				expect( result ).notToInclude( "[AFTER]" );
			} );

			it( title="tag form of the forward tag ends the request", body=function() {
				var result = request( "tagForm.cfm", "forward" );
				expect( result ).toInclude( "[TARGET]" );
				expect( result ).notToInclude( "[AFTER]" );
			} );

			it( title="inside try/catch/finally behaves like cflocation", body=function() {
				var forwarded = request( "tryCatchFinally.cfm", "forward" );
				var located = request( "tryCatchFinally.cfm", "location" );
				expect( forwarded ).toInclude( "[TARGET]" );
				expect( forwarded ).notToInclude( "[IN_TRY_AFTER]" );
				expect( forwarded ).notToInclude( "[CAUGHT]" );
				expect( forwarded ).notToInclude( "[AFTER]" );
				expect( markers( forwarded ) ).toBe( markers( located ) );
			} );

			it( title="inside a function behaves like cflocation", body=function() {
				var forwarded = request( "inFunction.cfm", "forward" );
				var located = request( "inFunction.cfm", "location" );
				expect( forwarded ).toInclude( "[TARGET]" );
				expect( forwarded ).notToInclude( "[IN_FUNCTION_AFTER]" );
				expect( forwarded ).notToInclude( "[AFTER]" );
				expect( markers( forwarded ) ).toBe( markers( located ) );
			} );

			it( title="a cfabort in the forwarded template ends the request", body=function() {
				var result = request( "forwardToAbort.cfm", "forward" );
				expect( result ).toInclude( "[TARGET]" );
				expect( result ).notToInclude( "[AFTER]" );
			} );

			it( title="onRequestEnd and onAbort behave like cflocation", body=function() {
				var forwarded = request( "app/basic.cfm", "forward" );
				var located = request( "app/basic.cfm", "location" );
				expect( forwarded ).toInclude( "[TARGET]" );
				expect( forwarded ).notToInclude( "[AFTER]" );
				expect( markers( forwarded ) ).toBe( markers( located ) );
			} );

		} );
	}

	private string function request( required string template, required string mode ) {
		var result = internalRequest(
			template: "/test/tickets/LDEV3298/" & arguments.template,
			urls: { "mode": arguments.mode }
		);
		return result.filecontent;
	}

	// the markers in the output without the one of the forwarded template, in order
	private string function markers( required string content ) {
		var found = reMatch( "\[[A-Z_]+\]", arguments.content );
		var result = [];
		loop array=found item="local.marker" {
			if ( marker != "[TARGET]" ) arrayAppend( result, marker );
		}
		return arrayToList( result );
	}

}
