component extends = "org.lucee.cfml.test.LuceeTestCase" labels="http" skip="true"{

	// disabled until Lucee's HTTP resource provider reliably supports HEAD
	// (LDEV-4861); the exists check does a HEAD request. httpbin /status/{code}
	// supports GET and HEAD, so this can be re-enabled once that is fixed.
	variables.httpbin = server.getTestService("httpbin");
	variables.httpbinUrl = structCount(variables.httpbin) > 0 ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
	variables.endpoint = variables.httpbinUrl & "/status/";
	
	function run( testresults , testbox ) {
		describe( "testcase for LDEV-3947, http resource provider should error with non 20x status codes", function () {
			it( title="Check reading an http resource",body = function ( currentSpec ) {
				expect( fileRead("#variables.endpoint#200") ).notToBeEmpty();
			});

			it( title="Check reading an http resource with 200 status code",body = function ( currentSpec ) {
				expect( fileRead("#variables.endpoint#200") ).notToBeEmpty();
			});
			
			it( title="Check reading an http resource with 401 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#401");
				}).toThrow();
			});
			
			it( title="Check reading an http resource with 403 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#403");
				}).toThrow();
			});

			it( title="Check reading an http resource with 404 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#404");
				}).toThrow();
			});
			
			it( title="Check reading an http resource with 500 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#500");
				}).toThrow();
			});

			it( title="Check reading an http resource with 503 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#503");
				}).toThrow();
			});

		});
	}

}