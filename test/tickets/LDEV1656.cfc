component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1656");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1656", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:1}
				);
				expect(local.result.filecontent.trim()).toBe('XX,YY,ZZ');
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:2}
				);
				expect(local.result.filecontent.trim()).toBe('ZZ,YY,XX');
			});
			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(local.result.filecontent.trim()).toBe('1,2,3');
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:4}
				);
				expect(local.result.filecontent.trim()).toBe('3,2,1');
			});
		});
		
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
