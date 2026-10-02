component extends="org.lucee.cfml.test.LuceeTestCase" labels="internalRequest,cfcontent" {

	function run( testResults, testBox ) {
		describe( "LDEV-5952 - _internalRequest fails with 'Content was already flushed' after resetBuffer()", function() {

			it( "cfcontent should work after resetBuffer() is called", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/resetBuffer.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "filecontent" );
				expect( result ).notToHaveKey( "error" );
				expect( result.status_code ).toBe( 200 );
				expect( result.headers[ "content-type" ] ).toInclude( "application/json" );

				var content = deserializeJSON( result.filecontent );
				expect( content.success ).toBeTrue();
			});

			it( "cfcontent should work after reset() is called", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/reset.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "filecontent" );
				expect( result ).notToHaveKey( "error" );
				expect( result.status_code ).toBe( 200 );
				expect( result.headers[ "content-type" ] ).toInclude( "application/json" );

				var content = deserializeJSON( result.filecontent );
				expect( content.success ).toBeTrue();
			});

			it( "cfcontent should work without any buffer manipulation", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/simple.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "filecontent" );
				expect( result ).notToHaveKey( "error" );
				expect( result.status_code ).toBe( 200 );
				expect( result.headers[ "content-type" ] ).toInclude( "application/json" );

				var content = deserializeJSON( result.filecontent );
				expect( content.success ).toBeTrue();
			});

			it( "resetBuffer() should fail after flushBuffer()", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/resetBufferAfterFlush.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "error" );
				expect( result.error.message ).toInclude( "committed" );
			});

			it( "reset() should fail after flushBuffer()", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/resetAfterFlush.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "error" );
				expect( result.error.message ).toInclude( "committed" );
			});

			it( "cfcontent reset=true should work without prior flush", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/cfcontentReset.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "filecontent" );
				expect( result ).notToHaveKey( "error" );
				expect( result.status_code ).toBe( 200 );
				expect( result.headers[ "content-type" ] ).toInclude( "application/json" );

				var content = deserializeJSON( result.filecontent );
				expect( content.success ).toBeTrue();
			});

			it( "cfcontent reset=true should fail after cfflush", function() {
				var result = _internalRequest(
					template = createURI( "LDEV5952/cfflushThenReset.cfm" ),
					method = "GET",
					throwonerror = false
				);

				expect( result ).toHaveKey( "error" );
				expect( result.error.message ).toInclude( "flushed" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
