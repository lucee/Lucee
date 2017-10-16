component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1173_1");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1173", function() {
			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as boolean", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe('{"ROWCOUNT":2,"COLUMNS":["FOO","BAR"],"DATA":{"FOO":[1,3],"BAR":[2,4]}}');
			});

			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe([{"FOO":"1","BAR":"2"},{"FOO":"3","BAR":"4"}]);
			});

			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as row", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe({"COLUMNS":["FOO","BAR"],"DATA":[["1","2"],["3","4"]]});
			});

			it(title = "checking serializeJSON function with attribute serializeQueryByColumns value as column", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe({"ROWCOUNT":2,"COLUMNS":["FOO","BAR"],"DATA":{"FOO":["1","3"],"BAR":["2","4"]}});
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
