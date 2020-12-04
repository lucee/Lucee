component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI( "LDEV3109" );
	}
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-3109", function(){
			it(title="Queryparam without cfsqltype", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1}
				);
				expect(trim(result.filecontent)).toBe(1);
			});
			it(title="Queryparam with cfsqltype=cf_sql_decimal for null value", body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 2}
				);
				expect(trim(result.filecontent)).toBe(1);
			});		
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}