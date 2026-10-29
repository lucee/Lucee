component extends="org.lucee.cfml.test.LuceeTestCase" labels="parser" {

	function run( testResults, testBox ) {
		describe( "LDEV-2146 syntax error message for a control statement without parentheses", function() {

			it( title="if without parentheses reports the missing parenthesis", body=function( currentSpec ) {
				var message = "";
				try {
					include "LDEV2146/if.cfm";
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "Missing [(] after the [if] keyword" );
			});

			it( title="while without parentheses reports the missing parenthesis", body=function( currentSpec ) {
				var message = "";
				try {
					include "LDEV2146/while.cfm";
				}
				catch ( any e ) {
					message = e.message;
				}
				expect( message ).toInclude( "Missing [(] after the [while] keyword" );
			});

			it( title="control statements with parentheses still work", body=function( currentSpec ) {
				include "LDEV2146/valid.cfm";
				expect( request.ldev2146 ).toBe( 1 );
			});

		});
	}

}
