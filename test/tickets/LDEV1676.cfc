component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.uri = createURI("LDEV1676");
		//systemOutput(" ", true);
	}	

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-1676", function () {
			it( title="Check xmlFeatures externalGeneralEntities=true, secure: false",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "externalGeneralEntities-True" }
				).filecontent;
				expect( trim( result ) ).toInclude("http://update.lucee.org/rest/update/provider/echoGet/cgi");
			});

			it( title="Check xmlFeatures externalGeneralEntities=false",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "externalGeneralEntities-False" }
				).filecontent;
				expect( trim( result ) ).toInclude("security restrictions set by XMLFeatures");
				expect( trim( result ) ).toInclude("NullPointerException");
			});
			
			it( title="Check xmlFeatures disallowDoctypeDecl=true",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "disallowDoctypeDecl-True" }
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE");
			});
		});

		describe( "check combined xmlFeatures directives", function () {

			it( title="Check xmlFeatures default, good xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "default",
						doctype: false,
						entity: false,
					}
				).filecontent;
				expect( trim( result ) ).toBe("lucee");
			});

			it( title="Check xmlFeatures default, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "default",
						doctype: true,
						entity: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE is disallowed when the feature");
			});

			it( title="Check xmlFeatures all secure, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "all-secure",
						doctype: true,
						entity: true,
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE is disallowed when the feature");
			});

			it( title="Check xmlFeatures all insecure, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "all-insecure",
						doctype: true,
						entity: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("http://update.lucee.org/rest/update/provider/echoGet/cgi");
			});

			it( title="Check xmlFeatures all secure, good xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "all-secure",
						doctype: false,
						entity: false
					}
				).filecontent;
				expect( trim( result ) ).toBe("lucee");
			});

			// check if we can inline disable the settings back to the old behavior
			it( title="Check xmlFeatures default, bad xml, cfapplication override",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{
						scene: "default",
						doctype: true,
						entity: true,
						cfapplicationOverride: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("http://update.lucee.org/rest/update/provider/echoGet/cgi");
			});

		});

		describe( "check bad config handling", function () {

			it( title="Check xmlFeatures invalidConfig secure",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "invalidConfig-secure" }
				).filecontent;
				expect( trim( result ) ).toInclude( "casterException" );
			});

			it( title="Check xmlFeatures invalidConfig docType",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "invalidConfig-docType" }
				).filecontent;
				expect( trim( result ) ).toInclude( "casterException" );
			});

			it( title="Check xmlFeatures invalidConfig Entities",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV1676.cfm",
					forms :	{ scene: "invalidConfig-Entities" }
				).filecontent;
				expect( trim( result ) ).toInclude( "casterException" );
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
