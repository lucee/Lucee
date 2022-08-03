component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.adm = new Administrator("web", request.WEBADMINPASSWORD?:server.WEBADMINPASSWORD);
		variables.uri = createURI("LDEV3777/LDEV3777.cfm");	
    }
    
	function run( testResults , testBox ) {
		describe( "test case for LDEV-3777", function() {
			it(title="cfsavecontent with Whitespace management = none", body=function( currentSpec ) {
				adm.updateOutputSetting(cfmlWriter="regular");
				local.result = _InternalRequest(
					template : uri,
					forms : {scene = 1}
				);
				expect(trim(result.filecontent)).toBe("20");
			});
			it(title="cfsavecontent with simple Whitespace management", body=function( currentSpec ) {
				adm.updateOutputSetting(cfmlWriter="white-space");
				local.result = _InternalRequest(
					template : uri,
					forms: {scene = 2}
				);
				expect(trim(result.filecontent)).toBe("0");
			});
			it(title="cfsavecontent with smart Whitespace management", body=function( currentSpec ) {
				adm.updateOutputSetting(cfmlWriter="white-space-pref");
				local.result = _InternalRequest(
					template : uri,
					forms: {scene = 3}
				);
				expect(trim(result.filecontent)).toBe("0");
			});
			it(title="cfsavecontent with cfprocessingdirective suppresswhitespace=true", skip=true, body=function( currentSpec ) {
				adm.updateOutputSetting(cfmlWriter="regular");
				local.result = _InternalRequest(
					template : uri,
					forms: {scene = 4}
				);
				expect(trim(result.filecontent)).toBe("0");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}