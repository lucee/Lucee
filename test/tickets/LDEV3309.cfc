component extends="org.lucee.cfml.test.LuceeTestCase" labels="customtag" {

	function run( testResults, testBox ) {
		describe( "LDEV-3309 caller[ ""scope.key"" ] in a custom tag", function() {

			it( title="caller[ ""url.key"" ] = value sets the url scope", body=function( currentSpec ) {
				structDelete( url, "ldev3309_set" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="set", target="url.ldev3309_set", value="url.set from custom tag" );
				expect( url.ldev3309_set ?: "" ).toBe( "url.set from custom tag" );
				expect( structKeyExists( variables, "url.ldev3309_set" ) ).toBeFalse();
				expect( structKeyExists( local, "url.ldev3309_set" ) ).toBeFalse();
			});

			it( title="caller[ ""request.a.b"" ] = value creates the nested struct", body=function( currentSpec ) {
				structDelete( request, "ldev3309" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="set", target="request.ldev3309.sub", value="nested" );
				expect( request.ldev3309.sub ?: "" ).toBe( "nested" );
			});

			it( title="caller[ ""variables.key"" ] = value sets the variables scope", body=function( currentSpec ) {
				structDelete( variables, "ldev3309_var" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="set", target="variables.ldev3309_var", value="var" );
				expect( variables.ldev3309_var ?: "" ).toBe( "var" );
			});

			it( title="caller[ ""local.key"" ] = value sets the local scope of the calling function", body=function( currentSpec ) {
				cfmodule( template="LDEV3309/cusTag.cfm", mode="set", target="local.ldev3309_loc", value="loc" );
				expect( local.ldev3309_loc ?: "" ).toBe( "loc" );
			});

			it( title="cfparam name=""caller.url.key"" sets the url scope", body=function( currentSpec ) {
				structDelete( url, "ldev3309_param" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="param", target="url.ldev3309_param", value="url.param from custom tag" );
				expect( url.ldev3309_param ?: "" ).toBe( "url.param from custom tag" );
			});

			it( title="setVariable( ""caller.url.key"" ) sets the url scope", body=function( currentSpec ) {
				structDelete( url, "ldev3309_sv" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="setVariable", target="url.ldev3309_sv", value="url.setVariable from custom tag" );
				expect( url.ldev3309_sv ?: "" ).toBe( "url.setVariable from custom tag" );
			});

			it( title="a plain key is still set in the variables scope", body=function( currentSpec ) {
				structDelete( variables, "ldev3309_plain" );
				cfmodule( template="LDEV3309/cusTag.cfm", mode="set", target="ldev3309_plain", value="plain" );
				expect( variables.ldev3309_plain ?: "" ).toBe( "plain" );
			});
		});
	}
}
