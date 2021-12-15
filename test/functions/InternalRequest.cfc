component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function beforeAll(){
		variables.uri = createURI("InternalRequest");
	}
	
	function run( testResults , testBox ) {
		describe( "test case for InternalRequest", function() {
			it(title = "checking url scope as string", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					urls : "test=1"
				);
			 	expect(result.filecontent).toBe('{"test":"1"}{}')
			});
			it(title = "checking url scope as struct", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					urls : {'test':1}
				);
			 	expect(result.filecontent).toBe('{"test":"1"}{}')
			});

			it(title = "checking url scope as string multiple times same name as list", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					urls : "test=1&test=2&test=3"
				);
			 	expect(result.filecontent).toBe('{"test":"1,2,3"}{}')
			});

			it(title = "checking url scope as string multiple times same name as array", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					urls : "test=1&test=2&test=3&sameURLFieldsAsArray=true"
				);
			 	expect(result.filecontent).toBe('{"test":["1","2","3"],"sameURLFieldsAsArray":"true"}{}')
			});
			it(title = "checking form scope as string multiple times same name as list", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					forms : "test=1&test=2&test=3"
				);
			 	expect(result.filecontent).toBe('{}{"test":"1,2,3","fieldnames":"test"}')
			});

			it(title = "checking form scope as string multiple times same name as array", body = function( currentSpec ) {
				local.result = _InternalRequest (
					template : "#uri#\index.cfm",
					forms : "test=1&test=2&test=3&sameFormFieldsAsArray=true"
				);
			 	expect(result.filecontent).toBe('{}{"test":"1,2,3","sameFormFieldsAsArray":"true","fieldnames":"test,sameFormFieldsAsArray"}')
			});
		});	
	}


	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}