component extends="org.lucee.cfml.test.LuceeTestCase" labels="error" {

	function beforeAll(){
		variables.uri = createURI( "LDEV3739" );
		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="variables.errorConfigBefore";

		admin
			action="updateError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			template500="#uri#/500.cfm"
			template404="#uri#/404.cfm"
			statuscode="true";

		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="variables.errorConfig";
	}

	function afterAll(){
		admin
			action="updateError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			template500=variables.errorConfigBefore.str.500
			template404=variables.errorConfigBefore.str.404
			statuscode=variables.errorConfigBefore.doStatusCode;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-3739 site-wide error template gets the error variable", body=function(){

			it( title="the test error templates are configured", body=function() {
				expect( variables.errorConfig.str.500 ).toBe( "#uri#/500.cfm" );
				expect( fileExists( variables.errorConfig.templates.500 ) ).toBeTrue();
			});

			it( title="500 error template has error, catch and cfcatch", body=function() {
				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( isJson( req.filecontent ) ).toBeTrue( req.filecontent );
				var result = deserializeJSON( req.filecontent );

				expect( result ).toHaveKey( "error" );
				expect( result ).toHaveKey( "catch" );
				expect( result ).toHaveKey( "cfcatch" );

				loop list="browser,datetime,diagnostics,GeneratedContent,HTTPReferer,mailto,message,QueryString,RemoteAddress,RootCause,Template" item="local.k" {
					expect( result.error ).toHaveKey( k );
				}
				expect( result.error.message ).toBe( "LDEV-3739 test error" );
			});

		});
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
