component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	variables.columns = "BUNDLENAME,BUNDLEVERSION,CLASS,CONNECTIONTIMEOUT,CONVERSATIONSIZELIMIT,DEFAULT,LABEL,MODEL,NAME,SOCKETTIMEOUT,SOURCE,TEMPERATURE";

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
		describe( "Test suite for AIGetConnections()", function() {

			it( title="returns a query with the documented columns, also without any connection", body=function() {
				var qry = AIGetConnections();
				expect( isQuery( qry ) ).toBeTrue();
				expect( listSort( qry.columnList, "textnocase" ) ).toBe( variables.columns );
			});

			it( title="lists the connections defined in the Application.cfc", body=function() {
				var result = getConnections();
				expect( result.isQuery ).toBeTrue();
				expect( result.columns ).toBe( variables.columns );

				var conn = findRow( result.rows, "aigetconnections-test" );
				expect( conn ).notToBeEmpty();
				expect( conn.source ).toBe( "application" );
				expect( conn.default ).toBe( "aigetconnectionstest" );
				expect( conn.model ).toBe( "aigetconnections-model" );
				expect( conn.class ).toBe( "lucee.runtime.ai.openai.OpenAIEngine" );
				expect( conn.label ).toBe( "ChatGPT" );
				expect( conn.conversationSizeLimit ).toBe( 7 );
				expect( conn.connectionTimeout ).toBe( 1234 );
				expect( conn.socketTimeout ).toBe( 5678 );

				var noDefault = findRow( result.rows, "aigetconnections-nodefault" );
				expect( noDefault ).notToBeEmpty();
				expect( noDefault.default ).toBe( "" );
				expect( noDefault.model ).toBe( "aigetconnections-model-2" );
			});

			it( title="the listed default resolves to the connection with 'default:name'", body=function() {
				var result = getConnections();
				var conn = findRow( result.rows, "aigetconnections-test" );
				expect( result.nameForDefault ).toBe( conn.name );
			});

			it( title="never returns secrets", body=function() {
				var raw = _InternalRequest( template: createURI( "AIGetConnections/index.cfm" ) ).fileContent;
				expect( raw ).notToInclude( "aigetconnections-secret-value" );
				expect( raw ).notToInclude( "secretKey" );
				expect( raw ).notToInclude( "custom" );
			});

		});

		describe( title="AIGetConnections() with the connections of the config (aimock)", skip=skipAimock(), body=function() {

			it( title="lists the connections configured in the server config", body=function() {
				var qry = AIGetConnections();
				var names = queryColumnData( qry, "name" );
				expect( names ).toInclude( variables.connections.openai );
				var row = arrayFindNoCase( names, variables.connections.openai );
				expect( qry.source[ row ] ).toBe( "server" );
				expect( qry.model[ row ] ).toBe( "gpt-4" );
				// the aimock connection is configured with the secret key "mock"
				expect( serializeJSON( qry ) ).notToInclude( '"mock"' );
			});

			it( title="a connection of the application replaces the one with the same name from the config", body=function() {
				var result = getConnections( variables.connections.openai );
				var matches = result.rows.filter( function( r ) {
					return r.name == variables.connections.openai;
				});
				expect( matches.len() ).toBe( 1 );
				expect( matches[ 1 ].source ).toBe( "application" );
				expect( matches[ 1 ].model ).toBe( "aigetconnections-override" );
			});

		});
	}

	private struct function getConnections( string override = "" ) {
		var result = _InternalRequest(
			template: createURI( "AIGetConnections/index.cfm" ),
			url: { override: arguments.override }
		);
		return deserializeJSON( result.fileContent.trim() );
	}

	private struct function findRow( required array rows, required string name ) {
		for ( var r in arguments.rows ) {
			if ( r.name == arguments.name ) return r;
		}
		return {};
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
