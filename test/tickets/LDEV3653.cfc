component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfheader" {

	function run( testResults, testBox ) {
		describe( "LDEV-3653 cfheader throwOnError after the response was flushed", function() {

			it( title="throwOnError=false does not throw after cfflush (script)", body=function() {
				var result = request( "suppressed.cfm" );
				expect( result.filecontent ).toInclude( "[BEFORE]" );
				expect( result.filecontent ).toInclude( "[COMMITTED:true]" );
				expect( result.filecontent ).toInclude( "[AFTER:2]" );
				expect( result.filecontent ).notToInclude( "already committed" );
			} );

			it( title="throwOnError=false does not throw after cfflush (tag)", body=function() {
				var result = request( "suppressedTag.cfm" );
				expect( result.filecontent ).toInclude( "[BEFORE]" );
				expect( result.filecontent ).toInclude( "[COMMITTED:true]" );
				expect( result.filecontent ).toInclude( "[AFTER:2]" );
				expect( result.filecontent ).notToInclude( "already committed" );
			} );

			it( title="the default still throws after cfflush", body=function() {
				var result = request( "default.cfm" );
				expect( result.filecontent ).toInclude( "[COMMITTED:true]" );
				expect( result.filecontent ).toInclude( "already committed" );
				expect( result.filecontent ).notToInclude( "[AFTER:2]" );
			} );

			it( title="throwOnError=true still throws after cfflush", body=function() {
				var result = request( "explicitTrue.cfm" );
				expect( result.filecontent ).toInclude( "[COMMITTED:true]" );
				expect( result.filecontent ).toInclude( "already committed" );
				expect( result.filecontent ).notToInclude( "[AFTER:2]" );
			} );

			it( title="throwOnError=false still sets the header when the response is not committed", body=function() {
				var result = request( "notCommitted.cfm" );
				expect( result.filecontent ).toInclude( "[COMMITTED:false]" );
				expect( result.filecontent ).toInclude( "[AFTER:2]" );
				expect( result.headers ).toHaveKey( "X-LDEV3653" );
				expect( result.headers[ "X-LDEV3653" ] ).toBe( "early" );
			} );

		} );
	}

	private struct function request( required string template ) {
		return _internalRequest(
			template: "/test/tickets/LDEV3653/" & arguments.template,
			throwonerror: false
		);
	}

}
