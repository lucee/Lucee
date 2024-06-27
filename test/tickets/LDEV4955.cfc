component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4955", function() {
			it( title="check env var string expansion works for mulitple tokens", body=function( currentSpec ){
				var raw ="";
				var tokens = "";
				var c = 1;
				for ( key in server.system.environment ){
					if ( len( server.system.environment[ key ] ) gt 2
						or key contains "pass"
						or key contains "secret"
						or key contains "s3"
						or key contains "token") continue;
					tokens &= ",{env.#key#}";
					raw &= ",#server.system.environment[ key ]#";
					if ( c gt 3 ) break;
					c++;
				}
				//systemoutput( "", true );
				//systemoutput( tokens, true );
				//systemoutput( raw, true );
				var expanded = expandPath( tokens );
				//systemoutput( expanded , true );
				expect( ( expanded eq raw ) ).toBeTrue(); // manual compare as we don't want to leak env vars
			});
		});
	}

}