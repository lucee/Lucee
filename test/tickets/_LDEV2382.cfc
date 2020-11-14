component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV2382");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-2382",function() {
			it( title = "Query of Query -With upper() method",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = 'upper',scene = '1'}
				)
				expect(trim(result.filecontent)).tobe('ABC');
			});
			it( title = "Query of Query - upper() method recordcount",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = 'upper',scene = '2'}
				)
				expect(trim(result.filecontent)).tobe('3');
			});

			it( title = "Query of Query - with lower() method",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = "lower",scene = '1'}
				)
				expect(trim(result.filecontent)).tobe('abc');
			});
			it( title = "Query of Query - with lower() method recordcount",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = "lower",scene = '2'}
				)
				expect(trim(result.filecontent)).tobe('3');
			});

			it( title = "Query of Query - without used lower() and upper() method",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = '',scene = '1'}
				)
				expect(trim(result.filecontent)).tobe('ABC');
			});
			it( title = "Query of Query - without used lower() and upper() method recordcount",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : {type = '',scene = '2'}
				)
				expect(trim(result.filecontent)).tobe('3');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}