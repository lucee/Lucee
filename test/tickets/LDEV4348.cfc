component extends = "org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function beforeAll(){
		variables.uri = createURI("LDEV4348");
	}	

	function run( testresults , testbox ) {
		
		describe( "check combined xmlFeatures getApplicationSettings", function () {

			it( title="Check xmlFeatures default",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4348.cfm",
					forms :	{
						scene: "default"
					}
				).filecontent.deserializeJson();
				expect( result.secure ).toBeTrue();
				expect( result.disallowDoctypeDecl  ).toBeTrue();
				expect( result.externalGeneralEntities ).toBeFalse();
			});
			
			it( title="Check xmlFeatures all secure",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4348.cfm",
					forms :	{
						scene: "all-secure"
					}
				).filecontent.deserializeJson();
				expect( result.secure ).toBeTrue();
				expect( result.disallowDoctypeDecl  ).toBeTrue();
				expect( result.externalGeneralEntities ).toBeFalse();
			});

			it( title="Check xmlFeatures all insecure, bad xml",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4348.cfm",
					forms :	{
						scene: "all-insecure"
					}
				).filecontent.deserializeJson();
				expect( result.secure ).toBeFalse();
				expect( result.disallowDoctypeDecl  ).toBeFalse();
				expect( result.externalGeneralEntities ).toBeTrue();
			});

			it( title="Check xmlFeatures, check pass thru",body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV4348.cfm",
					forms :	{
						scene: "testPassthru"
					}
				).filecontent.deserializeJson();
				expect( result.secure ).toBeFalse();
				expect( result.disallowDoctypeDecl  ).toBeFalse();
				expect( result.externalGeneralEntities ).toBeTrue();
				expect( result["http://apache.org/xml/features/validation/id-idref-checking"] ).toBeTrue();
			});

		});

	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}


