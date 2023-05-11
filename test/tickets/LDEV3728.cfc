component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll(){
		variables.testUrl = "https://update.lucee.org/rest/update/provider/light/5.3.10.97"; // returns a 302 to lucee cdn
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3728", function() {
			it( title="Checking cfhttp returns redirect locations, with redirect=true", skip=true, body=function( currentSpec ) {
				http url=variables.testUrl result="local.res" redirect=true;
				expect( res).toHaveKey( "locations" );
				expect( res.locations ).toBeArray();
				expect( len( res.locations ) ).toBeGT( 1 );
			});

			it( title="Checking cfhttp doesn't return redirect locations, redirect=false", body=function( currentSpec ) {
				http url=variables.testUrl result="local.res" redirect=false;
				expect( res ).notToHaveKey( "locations" );
			});
		});
	}

}
