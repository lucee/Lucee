component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4955", function() {
			it( title="check environment var placeholder expansion works for mulitple tokens", body=function( currentSpec ){

				var raw ="";
				var tokens = "";
				var c = 1;
				for ( key in server.system.environment ){
					if ( len( server.system.environment[ key ] ) gt 2
						or key contains "pass"
						or key contains "secret"
						or key contains "s3"
						or key contains "token") continue;
					tokens &= ",{env:#key#}";
					raw &= ",#server.system.environment[ key ]#";
					// check single
					expect( _expand( "${#key#}" ) ).toBe( server.system.environment[ key ] );
					expect( _expand( "{env:#key#}" ) ).toBe( server.system.environment[ key ] );
					if ( c gt 3 ) break;
					c++;
				}
				var expanded = _expand( tokens );
				expect( ( expanded eq raw ) ).toBeTrue(); // manual compare as we don't want to leak env vars
			});

			it( title="check system properties placeholder expansion works for mulitple tokens", body=function( currentSpec ){

				var raw ="";
				var tokens = "";
				var c = 1;
				for ( key in server.system.properties ){
					if ( len( server.system.properties[ key ] ) gt 2
						or key contains "pass"
						or key contains "secret"
						or key contains "s3"
						or key contains "token") continue;
					tokens &= ",{system:#key#}";
					raw &= ",#server.system.properties[ key ]#";
					// check single
					expect( _expand( "{system:#key#}" ) ).toBe( server.system.properties[ key ] );
					if ( c gt 3 ) break;
					c++;
				}
				var expanded = _expand( tokens );
				expect( ( expanded eq raw ) ).toBeTrue(); // manual compare as we don't want to leak env vars
			});

			it( title="check ${envvar:default_value) placeholder expansion fallback to default", body=function( currentSpec ){
				// $$$ is an invalid name, so we always fallback to default
				expect( _expand( "${$$$:haha}" ) ).toBe( "haha" );
			});
		});

	}

	private function _expand ( str ){
		var webFactory = createObject("java", "lucee.runtime.config.ConfigWebFactory");
		return webFactory.replaceConfigPlaceHolder(str );
	}	
}