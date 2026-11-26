component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6997" {

	function run( testResults, testBox ) {
		describe( "LDEV-6997 try/catch inside a finally block", function() {

			it( "cftry/cfcatch inside cffinally (ticket pattern) compiles and runs", function() {
				include "LDEV6997/tag.cfm";
				expect( result ).toBe( "tf" );
			});

			it( "try/catch inside finally in script compiles and runs", function() {
				include "LDEV6997/script.cfm";
				expect( result ).toBe( "tf" );
			});

			it( "try/catch inside finally runs on the exception path", function() {
				include "LDEV6997/exception.cfm";
				expect( result ).toBe( "tcfi-outer" );
			});

			it( "break, continue and retry in a try inside finally", function() {
				include "LDEV6997/flowControl.cfm";
				expect( result ).toBe( "tf2ffr" );
			});

			it( "cfbreak and cfretry in a cftry inside cffinally", function() {
				include "LDEV6997/tagFlowControl.cfm";
				expect( result ).toBe( "t1f2ffr" );
			});
		});
	}
}
