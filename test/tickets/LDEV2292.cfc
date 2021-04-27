component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2292");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2292", function() {
			it(title = "SQL is removed after execute", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2292.cfm",
					forms :	{scene=1}
				);
				expect("select * from LDEV2292 where id=2").toBe(trim(local.result.filecontent));
			});

			it(title = "SQL is available before execute", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2292.cfm",
					forms :	{scene=2}
				);
				expect("select * from LDEV2292 where id=2").toBe(trim(local.result.filecontent));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}