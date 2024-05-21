component extends = "org.lucee.cfml.test.LuceeTestCase" labels="http" {

	// requires LDEV-4861 to support rest httpmethod="get,head" and on update provider
	// as the exists check does a HEAD request
	variables.endpoint = "https://update.lucee.org/rest/update/provider/echoGet?";
	
	function run( testresults , testbox ) {
		describe( "testcase for LDEV-3947, http resource provider should error with non 20x status codes", function () {
			it( title="Check reading an http resource",body = function ( currentSpec ) {
				expect( fileRead("#variables.endpoint#") ).notToBeEmpty();
			});

			it( title="Check reading an http resource with 200 status code",body = function ( currentSpec ) {
				expect( fileRead("#variables.endpoint#&statusCode=200") ).notToBeEmpty();
			});
			
			it( title="Check reading an http resource with 401 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#&statusCode=401");
				}).toThrow();
			});
			
			it( title="Check reading an http resource with 403 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#&statusCode=403");
				}).toThrow();
			});

			it( title="Check reading an http resource with 404 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#&statusCode=404");
				}).toThrow();
			});
			
			it( title="Check reading an http resource with 500 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#&statusCode=500");
				}).toThrow();
			});

			it( title="Check reading an http resource with 503 status code",body = function ( currentSpec ) {
				expect(function(){
					fileRead("#variables.endpoint#&statusCode=503");
				}).toThrow();
			});

		});
	}

}