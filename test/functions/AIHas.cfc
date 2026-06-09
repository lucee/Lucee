component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.aimock = server.getTestService( "aimock" );
		variables.connections = structKeyExists( variables.aimock, "connections" ) ? variables.aimock.connections : {
			openai: "aimock-openai",
			claude: "aimock-claude",
			gemini: "aimock-gemini"
		};
	}

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for AIHas()", skip=skipAimock, body=function() {

			it( title="returns true for the registered mock OpenAI connection", body=function() {
				expect( AIHas( variables.connections.openai ) ).toBeTrue();
			});

			it( title="returns true for the registered mock Claude connection", body=function() {
				expect( AIHas( variables.connections.claude ) ).toBeTrue();
			});

			it( title="returns true for the registered mock Gemini connection", body=function() {
				expect( AIHas( variables.connections.gemini ) ).toBeTrue();
			});

			it( title="returns false for an unknown connection", body=function() {
				expect( AIHas( "missing-connection" ) ).toBeFalse();
			});

		});
	}

}
