component extends="org.lucee.cfml.test.LuceeTestCase" labels="error" {

	function beforeAll(){

		variables.uri = createURI("LDEV3739");
		// stash default config
		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="variables.errorConfig";
		systemOutput(variables.errorConfig, true);

		// setup test config
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
			returnVariable="variables.errorConfig2";
		systemOutput(variables.errorConfig2, true);

	}

	function afterAll(){
		// restore default config
		admin
			action="updateError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			template500=variables.errorConfig.str.500
			template404=variables.errorConfig.str.404
			statuscode= variables.errorConfig.doStatusCode;
	}

	function run( testResults , testBox ) {
		describe( title='LDEV-3739' , body=function(){
			it( title='test that the test error templates are configured' , body=function() {
				expect( fileExists( variables.errorConfig.templates.404 ) ).toBe( "#uri#/500.cfm" );
				expect( fileExists( variables.errorConfig.templates.500 ) ).toBe( "#uri#/404.cfm" );
			});

			it( title='test 500 error page has error variable' , body=function() {
				var req = _InternalRequest(
					template: "#uri#/throw.cfm",  // throw an error and trigger 500.cfm
					throwonerror: false
				);
				//fileWrite( uri & "/500_req.json", req.toJson() );
				//fileWrite( uri & "/500.json", req.filecontent );
				systemOutput( req.filecontent, true );
				systemOutput( req, true );
				expect( isJson( req.filecontent ) ).toBeTrue();
				var result = deserializeJSON( req.filecontent ); 
				expect( req.status_code ).toBe( 500 );

				expect( result ).toHaveKey( "error" );
				expect( result ).toHaveKey( "catch" );
				expect( result ).toHaveKey( "cfcatch" );

				loop list="browser,datetime,diagnostics,GeneratedContent,HTTPReferer,mailto,QueryString,RemoteAddress,RootCause,Template" item="k" {
					expect( result.error ).toHaveKey( k );
				}
			});
			
			xit( title='test 404 error page has error variable', body=function() {
				var req = _InternalRequest(
					template : "#uri#/missing.cfm", // trigger 404.cfm
					throwonerror: false
				);
				return; // testing this with internalRequest is difficult

				//fileWrite( uri & "/404_req.json", req.toJson() );
				//fileWrite( uri & "/404.json", req.filecontent );
				expect( req.status_code ).toBe( 404 );
				systemOutput( req.filecontent, true );
				systemOutput( req, true );
				expect( isJson( req.filecontent ) ).toBeTrue();
				var result = deserializeJSON( req.filecontent );


				expect( result ).toHaveKey( "error" );
				expect( result ).toHaveKey( "catch" );
				expect( result ).toHaveKey( "cfcatch" );
				
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

}