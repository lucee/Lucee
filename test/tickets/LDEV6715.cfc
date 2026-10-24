component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven,http" {

	variables.deadProxy = "127.0.0.1:1";

	function beforeAll() {
		variables.uri = createURI( "LDEV6715" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6715 Maven downloads triggered by a request use the application proxy (this.proxy)", function() {

			it( "mavenLoad uses this.proxy", function() {
				var err = _request( "load", true );
				expect( err ).toInclude( variables.deadProxy, "should have tried the application proxy, got [#err#]" );
			});

			it( "dependencies resolved on worker threads use this.proxy", function() {
				var err = _request( "import", true );
				expect( err ).toInclude( variables.deadProxy, "should have tried the application proxy, got [#err#]" );
			});

			it( "mavenLoad without this.proxy does not use it", function() {
				var err = _request( "load", false );
				expect( err ).notToInclude( variables.deadProxy, "should not have used the proxy of another application, got [#err#]" );
			});

		});
	}

	private string function _request( required string action, required boolean proxy ) {
		var result = _internalRequest(
			template: "#variables.uri#/ldev6715.cfm",
			url: { action: arguments.action, proxy: arguments.proxy }
		);
		var out = trim( result.filecontent );
		expect( out ).notToInclude( "no error", "the download should have failed: [#out#]" );
		return out;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
