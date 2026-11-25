component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6988" );
		structDelete( server, "ldev6988_hits" );
		structDelete( server, "ldev6988_classic_hits" );
		structDelete( server, "ldev6988_end_hits" );
	}

	function afterAll() {
		structDelete( server, "ldev6988_hits" );
		structDelete( server, "ldev6988_classic_hits" );
		structDelete( server, "ldev6988_end_hits" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6988: Direct requests to Application.cfc, Application.cfm and OnRequestEnd.cfm behave as missing templates", function() {

			beforeEach( function() {
				server.ldev6988_hits = 0;
				server.ldev6988_classic_hits = 0;
				server.ldev6988_end_hits = 0;
			});

			it( "rejects direct requests to Application.cfc with missinginclude without executing it", function() {
				var missing = _InternalRequest( template: "#variables.uri#/modern/does_not_exist.cfm", throwonerror: false );
				server.ldev6988_hits = 0;

				var res = _InternalRequest( template: "#variables.uri#/modern/Application.cfc", throwonerror: false );
				expect( res ).toHaveKey( "error" );
				expect( res.error.type ).toBe( "missinginclude" );
				expect( res.status_code ).toBe( missing.status_code );
				expect( res.status_code ).notToBe( 200 );
				expect( server.ldev6988_hits ).toBe( 0 );

				var resMethod = _InternalRequest( template: "#variables.uri#/modern/Application.cfc", url: { method: "ping" }, throwonerror: false );
				expect( resMethod ).toHaveKey( "error" );
				expect( resMethod.error.type ).toBe( "missinginclude" );
				expect( resMethod.status_code ).notToBe( 200 );
				expect( trim( resMethod.fileContent ) ).notToBe( "pong" );
				expect( server.ldev6988_hits ).toBe( 0 );

				var resMeta = _InternalRequest( template: "#variables.uri#/modern/Application.cfc", url: "json", throwonerror: false );
				expect( resMeta ).toHaveKey( "error" );
				expect( resMeta.error.type ).toBe( "missinginclude" );
				expect( resMeta.status_code ).notToBe( 200 );
				expect( server.ldev6988_hits ).toBe( 0 );
			});

			it( "rejects case and path variants and subfolder Application.cfc", function() {
				var paths = [
					"#variables.uri#/modern/application.cfc",
					"#variables.uri#/modern//Application.cfc",
					"#variables.uri#/modern/./Application.cfc",
					"#variables.uri#/modern/sub/Application.cfc"
				];
				for ( var p in paths ) {
					var res = _InternalRequest( template: p, throwonerror: false );
					expect( res ).toHaveKey( "error", "Expected error for #p#" );
					expect( res.error.type ).toBe( "missinginclude", "Expected missinginclude for #p#" );
					expect( res.status_code ).notToBe( 200, "Expected non-200 for #p#" );
				}
				expect( server.ldev6988_hits ).toBe( 0 );
			});

			it( "throws missinginclude when throwonerror is true", function() {
				expect( function() {
					_InternalRequest( template: "#variables.uri#/modern/Application.cfc", throwonerror: true );
				}).toThrow( type="missinginclude" );
			});

			it( "rejects direct requests to Application.cfm and OnRequestEnd.cfm", function() {
				var paths = [
					"#variables.uri#/classic/Application.cfm",
					"#variables.uri#/classic/application.cfm",
					"#variables.uri#/classic/OnRequestEnd.cfm",
					"#variables.uri#/classic/onrequestend.cfm",
					"#variables.uri#/classic/sub/Application.cfm",
					"#variables.uri#/classic/sub/OnRequestEnd.cfm"
				];
				for ( var p in paths ) {
					var res = _InternalRequest( template: p, throwonerror: false );
					expect( res ).toHaveKey( "error", "Expected error for #p#" );
					expect( res.error.type ).toBe( "missinginclude", "Expected missinginclude for #p#" );
					expect( res.status_code ).notToBe( 200, "Expected non-200 for #p#" );
				}
				expect( server.ldev6988_classic_hits ).toBe( 0 );
				expect( server.ldev6988_end_hits ).toBe( 0 );
			});

			it( "keeps normal pages, lifecycle execution, createObject and cfinclude working", function() {
				var mod = _InternalRequest( template: "#variables.uri#/modern/index.cfm", throwonerror: false );
				expect( mod.status_code ).toBe( 200 );
				expect( trim( mod.fileContent ) ).toBe( "ok|pong" );
				expect( server.ldev6988_hits ).toBeGT( 0 );

				var sub = _InternalRequest( template: "#variables.uri#/modern/sub/index.cfm", throwonerror: false );
				expect( sub.status_code ).toBe( 200 );
				expect( trim( sub.fileContent ) ).toBe( "sub-ok" );

				var modInc = _InternalRequest( template: "#variables.uri#/modern/include_app.cfm", throwonerror: false );
				expect( modInc.status_code ).toBe( 200 );
				expect( trim( modInc.fileContent ) ).toBe( "included" );

				server.ldev6988_classic_hits = 0;
				server.ldev6988_end_hits = 0;
				var cls = _InternalRequest( template: "#variables.uri#/classic/index.cfm", throwonerror: false );
				expect( cls.status_code ).toBe( 200 );
				expect( trim( cls.fileContent ) ).toBe( "classic|end" );
				expect( server.ldev6988_classic_hits ).toBe( 1 );
				expect( server.ldev6988_end_hits ).toBe( 1 );

				server.ldev6988_classic_hits = 0;
				var clsInc = _InternalRequest( template: "#variables.uri#/classic/include_app.cfm", throwonerror: false );
				expect( clsInc.status_code ).toBe( 200 );
				expect( trim( clsInc.fileContent ) ).toBe( "included|end" );
				expect( server.ldev6988_classic_hits ).toBe( 2 );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
