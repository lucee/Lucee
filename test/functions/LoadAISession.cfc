component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for LoadAISession()", skip=skipAimock, body=function() {

			it( title="restores a session from serialized JSON", body=function() {
				var ais = createAISession( name="aimock", systemMessage="loaded" );
				inquiryAISession( ais, "stored" );
				var loaded = LoadAISession( "aimock", SerializeAISession( ais ) );
				expect( isObject( loaded ) ).toBeTrue();
				expect( inquiryAISession( loaded, "restored" ) ).toBe( "restored" );
			});

			it( title="restores a session from a struct", body=function() {
				var ais = createAISession( name="aimock" );
				inquiryAISession( ais, "struct-load" );
				var data = deserializeJSON( SerializeAISession( ais ) );
				var loaded = LoadAISession( "aimock", data );
				expect( isObject( loaded ) ).toBeTrue();
				expect( inquiryAISession( loaded, "continued" ) ).toBe( "continued" );
			});

		});
	}

}
