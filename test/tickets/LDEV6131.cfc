component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	function run( testResults, testBox ) {

		describe( "LDEV-6131 AI connection struct in place of the connection name", function() {

			it( title="createAISession, loadAISession, aiHas, aiGetMetaData and aiGetModels accept a connection struct and reuse the engine", body=function( currentSpec ) {
				var r = callTemplate( "LDEV6131/struct.cfm" );
				// canonical form, the session is created by the engine of the struct
				expect( r.answer1 ).toBe( "[be terse] hello" );
				expect( r.answer2 ).toBe( "[other] hello" );
				expect( r.answer3 ).toBe( "[third] hello" );
				// the engine is created once for the same definition, key order and case do not matter
				expect( r.initsAfterFirst ).toBe( 1 );
				expect( r.initsAfterSecond ).toBe( 1 );
				expect( r.initsAfterThird ).toBe( 1 );
				expect( r.id2 ).toBe( r.id1 );
				// a different definition is a different engine
				expect( r.initsAfterOther ).toBe( 2 );
				expect( r.idOther ).notToBe( r.id1 );
				// flat form: the engine properties are set directly in the struct
				expect( r.answer5 ).toBe( "[flat] hello" );
				expect( r.flatLabel ).toInclude( "-flat" );
				expect( r.label ).toInclude( "ldev6131-" );
				expect( r.sessionEngineLabel ).toBe( r.label );
				// the other functions
				expect( r.typeModel ).toBe( "ldev6131-model" );
				expect( r.has ).toBeTrue();
				expect( r.hasFlat ).toBeTrue();
				expect( r.hasName ).toBeFalse();
				expect( r.loadedAnswer ).toBe( "[be terse] again" );
				expect( r.models ).toBeTrue();
				// the engine of a struct can be used by its id as well
				expect( r.engineById ).toBe( "[by id] x" );
			});

			it( title="the tag LuceeAI accepts a connection struct for the attribute name", body=function( currentSpec ) {
				var r = callTemplate( "LDEV6131/tag.cfm" );
				expect( r.answer ).toBe( "[be terse] hello" );
				expect( r.label ).toInclude( "ldev6131-tag-" );
			});

			it( title="an invalid struct throws, a missing name behaves as before", body=function( currentSpec ) {
				var r = callTemplate( "LDEV6131/invalid.cfm" );
				expect( r.noClass ).notToBe( "no exception" );
				expect( r.noClassMessage ).toInclude( "type" );
				expect( r.badType ).notToBe( "no exception" );
				expect( r.badComponent ).toBe( "exception" );
				expect( r.badName ).toBe( "exception" );
			});

			it( title="a struct with a type works against an OpenAI compatible service", skip=skipAimock(), body=function( currentSpec ) {
				var mock = server.getTestService( "aimock" );
				var r = callTemplate( "LDEV6131/aimock.cfm", { server: mock.server, port: mock.port } );
				expect( r.answer ).toBe( "ping" );
				expect( r.same ).toBeTrue();
			});

		});

		describe( "LDEV-6131 cfapplication action=update re-reads the attribute ai", function() {

			it( title="defines, replaces and validates the connections of the application (Application.cfc)", body=function( currentSpec ) {
				assertUpdate( callTemplate( "LDEV6131/update.cfm" ) );
			});

			it( title="defines, replaces and validates the connections of the application (no Application.cfc)", body=function( currentSpec ) {
				assertUpdate( callTemplate( "LDEV6131classic/update.cfm" ) );
			});

		});

	}

	private function assertUpdate( r ) {
		expect( r.hasBefore ).toBeFalse();
		expect( r.hasAfter ).toBeTrue();
		expect( r.answer ).toBe( "[be terse] hello" );
		expect( r.label ).toInclude( "ldev6131-" );
		expect( r.listed ).toBe( "application" );
		expect( r.hasFirstAfterReplace ).toBeFalse();
		expect( r.hasSecond ).toBeTrue();
		expect( r.invalid ).toBe( "exception" );
		// the connections are untouched by the failed update
		expect( r.hasSecondAfterInvalid ).toBeTrue();
	}

	private function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	private struct function callTemplate( required string template, struct urls={} ) {
		var res = _InternalRequest( template: createURI( arguments.template ), urls: arguments.urls );
		return deserializeJSON( res.fileContent.trim() );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
