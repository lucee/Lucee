component extends = "org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-3947", function () {
			it( title="Check reading an http resource with different status codes",body = function ( currentSpec ) {

				expect( fileRead("https://update.lucee.org/rest/update/provider/echoGet") ).notToBeEmpty();

				expect( fileRead("https://update.lucee.org/rest/update/provider/echoGet?statusCode=200") ).notToBeEmpty();

				expect(function(){
					fileRead("https://update.lucee.org/rest/update/provider/echoGet?statusCode=404");
				}).toThrow();

				expect(function(){
					fileRead("https://update.lucee.org/rest/update/provider/echoGet?statusCode=500");
				}).toThrow();

				expect(function(){
					fileRead("https://update.lucee.org/rest/update/provider/echoGet?statusCode=401");
				}).toThrow();

				expect(function(){
					fileRead("https://update.lucee.org/rest/update/provider/echoGet?statusCode=301");
				}).toThrow();

			});

		});
	}

}