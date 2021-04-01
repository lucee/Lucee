component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2298");
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV2298", function(){

			it( title="queryExecute() param with Struct & column accept null value it returns default date", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1,tablename = 'ldev2298_null'}
				);
				expect(result.filecontent).toBe("{ts '1900-01-01 00:00:00'}");
			});

			it( title="queryExecute() param with Array of Struct & column doesn't accept null value", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2,tablename = 'ldev2298_notnull'}
				);
				expect(result.filecontent).toBe("Error");
			});

			it( title="queryExecute() param with Struct & column doesn't accept null value", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 3,tablename = 'ldev2298_notnull'}
				);
				expect(result.filecontent).tobe("Error");
			});

			it( title="queryExecute() param with Array of Struct & column accept null value it returns default date", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 4,tablename = 'ldev2298_null'}
				);
				expect(result.filecontent).toBe("{ts '1900-01-01 00:00:00'}");
			}); 
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}