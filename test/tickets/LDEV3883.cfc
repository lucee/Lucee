component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV3883/orm");
		variables.uriNoOrm = createURI("LDEV3883/noorm");
		variables.expected = "onApplicationStart,onSessionStart,onRequestStart,onRequestEnd";
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-3883 application lifecycle order without ORM", body=function() {

			it( title="baseline - no application scope access in pseudo constructor", body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#uriNoOrm#/LDEV3883.cfm",
					urls = { appName: "test-3883-noorm-baseline_#createUniqueID()#" }
				);
				expect( cleanOutput( result.fileContent ) ).toBe( expected );
			});

			it( title="accessing application scope in pseudo constructor", body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#uriNoOrm#/LDEV3883.cfm",
					urls = { useAppScope: true, appName: "test-3883-noorm-appscope_#createUniqueID()#" }
				);
				expect( cleanOutput( result.fileContent ) ).toBe( expected );
			});

		});

		describe( title="LDEV-3883 application lifecycle order with ORM", skip=noOrm(), body=function() {

			xit( title="using application scope in ORM event handler", body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#uri#/LDEV3883.cfm",
					urls = { type: "handler", appName: "test-3883-orm-handler_#createUniqueID()#" }
				);
				expect( cleanOutput( result.fileContent ) ).toBe( expected );
			});

			xit( title="using application scope in component", body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#uri#/LDEV3883.cfm",
					urls = { type: "cfc", appName: "test-3883-orm-cfc_#createUniqueID()#" }
				);
				expect( cleanOutput( result.fileContent ) ).toBe( expected );
			});

		});

	}

	private string function cleanOutput( required string str ) {
		return reReplace( trim( arguments.str ), "\s+", "", "all" );
	}

	private function noOrm() {
		return ( structCount( server.getTestService( "orm" ) ) eq 0 );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
