component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-5922 loops inside a finally block", function() {

			it( title="for-in with try/catch inside finally (ticket pattern) compiles and runs", body=function( currentSpec ) {
				var cfc = new LDEV5922.ForInFinally();
				expect( cfc.ticketPattern() ).toBe( "ok" );
			});

			it( title="for-in inside finally runs on the normal and on the exception path", body=function( currentSpec ) {
				var cfc = new LDEV5922.ForInFinally();
				expect( cfc.forInInFinally( false ) ).toBe( "a,b" );
				expect( cfc.forInInFinally( true ) ).toBe( "catch,a,b" );
			});

			it( title="while with try/catch inside finally compiles and runs", body=function( currentSpec ) {
				var cfc = new LDEV5922.ForInFinally();
				expect( cfc.whileInFinally() ).toBe( "1,2" );
			});

			it( title="break and continue in a loop inside finally", body=function( currentSpec ) {
				var cfc = new LDEV5922.ForInFinally();
				expect( cfc.breakContinueInFinally() ).toBe( "a,c" );
			});

		});
	}
}
