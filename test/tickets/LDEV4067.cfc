component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {
	function beforeAll() {
		variables.uri = createURI("LDEV4067");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4067", function() {
			it( title="checking this scope calling from closure without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:1}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from closure without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:2}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from lambda without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:3}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from lambda without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:4}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from closure with ORM entity", skip="true", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:5}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from closure with ORM entity", skip="true", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:6}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from lambda with ORM entity", skip="true", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:7}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from lambda with ORM entity", skip=true, body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:8}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
		});
	}
	
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
}
