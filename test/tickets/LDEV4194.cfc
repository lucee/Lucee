component extends="org.lucee.cfml.test.LuceeTestCase" labels="error" {

	function beforeAll(){
		variables.uri = createURI( "LDEV4194" );
		variables.defaultTemplate = "/lucee/templates/error/error.cfm";
		variables.generalMarker = "LDEV4194-general-template";
		variables.missingMarker = "LDEV4194-missing-template";

		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="variables.errorConfigBefore";
	}

	function afterAll(){
		// only store a value again when it differs from the default, so "not set" stays "not set"
		var before500 = variables.errorConfigBefore.str.500 == variables.defaultTemplate ? "" : variables.errorConfigBefore.str.500;
		var before404 = variables.errorConfigBefore.str.404 == variables.defaultTemplate ? "" : variables.errorConfigBefore.str.404;
		admin
			action="updateError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			template500=before500
			template404=before404
			statuscode=variables.errorConfigBefore.doStatusCode;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-4194 general error template is used for missing templates unless a missing template error template is set", body=function(){

			it( title="only the general error template is set, a missing template uses the general error template", body=function() {
				setErrorTemplates( general: "#uri#/general.cfm", missing: "" );

				expect( getMissingTemplateErrorTemplate() ).toBe( "#uri#/general.cfm" );
				expect( getGeneralErrorTemplate() ).toBe( "#uri#/general.cfm" );
			});

			it( title="only the general error template is set, an exception still uses the general error template", body=function() {
				setErrorTemplates( general: "#uri#/general.cfm", missing: "" );

				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( req.filecontent ).toInclude( variables.generalMarker );
			});

			it( title="both are set, a missing template uses the missing template error template", body=function() {
				setErrorTemplates( general: "#uri#/general.cfm", missing: "#uri#/missing.cfm" );

				expect( getMissingTemplateErrorTemplate() ).toBe( "#uri#/missing.cfm" );
				expect( getGeneralErrorTemplate() ).toBe( "#uri#/general.cfm" );
			});

			it( title="both are set, an exception uses the general error template", body=function() {
				setErrorTemplates( general: "#uri#/general.cfm", missing: "#uri#/missing.cfm" );

				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( req.filecontent ).toInclude( variables.generalMarker );
				expect( req.filecontent ).notToInclude( variables.missingMarker );
			});

			it( title="only the missing template error template is set, a missing template uses it and an exception keeps the default", body=function() {
				setErrorTemplates( general: "", missing: "#uri#/missing.cfm" );

				expect( getMissingTemplateErrorTemplate() ).toBe( "#uri#/missing.cfm" );
				expect( getGeneralErrorTemplate() ).toBe( variables.defaultTemplate );

				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( req.filecontent ).notToInclude( variables.missingMarker );
				expect( req.filecontent ).notToInclude( variables.generalMarker );
				expect( req.filecontent ).toInclude( "LDEV-4194 test error" );
			});

			it( title="neither is set, the default is unchanged for missing templates and exceptions", body=function() {
				setErrorTemplates( general: "", missing: "" );

				var config = getErrorConfig();
				expect( config.str.500 ).toBe( variables.defaultTemplate );
				expect( config.str.404 ).toBe( variables.defaultTemplate );

				expect( getMissingTemplateErrorTemplate() ).toBe( variables.defaultTemplate );
				expect( getGeneralErrorTemplate() ).toBe( variables.defaultTemplate );

				var req = _InternalRequest(
					template: "#uri#/throw.cfm",
					throwonerror: false
				);
				expect( req.status_code ).toBe( 500 );
				expect( req.filecontent ).notToInclude( variables.generalMarker );
				expect( req.filecontent ).notToInclude( variables.missingMarker );
				expect( req.filecontent ).toInclude( "LDEV-4194 test error" );
			});

			it( title="getError reports the general error template for 404 when only the general error template is set", body=function() {
				setErrorTemplates( general: "#uri#/general.cfm", missing: "" );
				var config = getErrorConfig();
				expect( config.str.500 ).toBe( "#uri#/general.cfm" );
				expect( config.str.404 ).toBe( "#uri#/general.cfm" );

				setErrorTemplates( general: "#uri#/general.cfm", missing: "#uri#/missing.cfm" );
				config = getErrorConfig();
				expect( config.str.500 ).toBe( "#uri#/general.cfm" );
				expect( config.str.404 ).toBe( "#uri#/missing.cfm" );
			});

		});
	}

	// the error template used for a missing template (status 404), resolved by the config like in PageContextImpl.handlePageException.
	// This is checked on the config, because a request with a missing template is only handled with status 404 if the missing template
	// is requested without any other template on the stack (see PageContextImpl.getStatusCode), which can not be done from a test.
	private string function getMissingTemplateErrorTemplate() {
		return getPageContext().getConfig().getErrorTemplate( 404 );
	}

	private string function getGeneralErrorTemplate() {
		return getPageContext().getConfig().getErrorTemplate( 500 );
	}

	private function setErrorTemplates( required string general, required string missing ) {
		admin
			action="updateError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			template500=arguments.general
			template404=arguments.missing
			statuscode="true";
	}

	private struct function getErrorConfig() {
		admin
			action="getError"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="local.result";
		return result;
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
