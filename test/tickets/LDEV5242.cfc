component extends="org.lucee.cfml.test.LuceeTestCase" labels="error" {

	function beforeAll(){
		variables.uri = createURI( "LDEV5242" );
		variables.dir = expandPath( variables.uri );
		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="variables.errorConfigBefore";
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
		describe( title="LDEV-5242 error template configured with an absolute file path", body=function(){

			it( title="the absolute path is used as is", body=function() {
				var tmpl = variables.dir & "/500.cfm";
				admin
					action="updateError"
					type="server"
					password="#request.SERVERADMINPASSWORD#"
					template500="#tmpl#"
					template404="#tmpl#"
					statuscode="true";

				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( trim( req.filecontent ) ).toBe( "LDEV5242-ERROR-TEMPLATE" );
			});

		});
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
