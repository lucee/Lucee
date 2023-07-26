component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.uri = createURI("LDEV4651");
		//systemOutput(" ", true);
	}	

	function run( testresults , testbox ) {
		describe( "testcase for LDEV-4651", function () {
			
			it( title="Check xmlFeatures disallowDoctypeDecl=true",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-True",
						doctype: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE");
			});

			it( title="Check xmlFeatures disallowDoctypeDecl=true, with xmlparse too",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-True",
						doctype: true,
						xmlParseThenSearch: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE");
			});
			

			it( title="Check xmlFeatures disallowDoctypeDecl=false",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-False",
						doctype: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("lucee");
			});

			it( title="Check xmlFeatures disallowDoctypeDecl=false, with xmlparse too",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-False",
						doctype: true,
						xmlParseThenSearch: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("lucee");
			});


			it( title="Check xmlFeatures disallowDoctypeDecl=false, via url",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-False",
						doctype: true,
						cfapplicationOverride: true,
						cfapplicationOverrideState: false
					}
				).filecontent;
				expect( trim( result ) ).toInclude("lucee");
			});

			it( title="Check xmlFeatures disallowDoctypeDecl=true, via url",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{ 
						scene: "disallowDoctypeDecl-true",
						doctype: true,
						cfapplicationOverride: true,
						cfapplicationOverrideState: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE");
			});

		});

		describe( "check combined xmlFeatures directives", function () {

			it( title="Check xmlFeatures default, good xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{
						scene: "default",
						doctype: false
					}
				).filecontent;
				expect( trim( result ) ).toBe("lucee");
			});

			it( title="Check xmlFeatures default, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{
						scene: "default",
						doctype: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE is disallowed when the feature");
			});

			it( title="Check xmlFeatures all secure, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{
						scene: "all-secure",
						doctype: true
					}
				).filecontent;
				expect( trim( result ) ).toInclude("DOCTYPE is disallowed when the feature");
			});

			it( title="Check xmlFeatures all secure, good xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4651.cfm",
					forms :	{
						scene: "all-secure",
						doctype: false
					}
				).filecontent;
				expect( trim( result ) ).toBe("lucee");
			});

		});

	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
