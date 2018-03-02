component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1655");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1655", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:1}
				);
				expect(local.result.filecontent.trim()).toBe('AA,BB,CC,DD');
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:2}
				);
				expect(local.result.filecontent.trim()).toBe('DD,CC,BB,AA');
			});
			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(local.result.filecontent.trim()).toBe('1,2,3,4');
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:4}
				);
				expect(local.result.filecontent.trim()).toBe('4,3,2,1');
			});
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:5}
				);
				expect(local.result.filecontent.trim()).toBe('AA,BB,CC,DD');
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:6}
				);
				expect(local.result.filecontent.trim()).toBe('DD,CC,BB,AA');
			});
			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:7}
				);
				expect(local.result.filecontent.trim()).toBe('1,2,3,4');
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:8}
				);
				expect(local.result.filecontent.trim()).toBe('4,3,2,1');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

