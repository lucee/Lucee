component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql"{

	function beforeAll(){
		variables.uri = createURI("LDEV2819");
	}

	private function isNotSupported() {
		var mssql = server.getDatasource("mssql");
		if(!isNull(mssql) && structCount(mssql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2819", function(){
			it( title = "Checking new query() result must be NULL = false ", skip=isNotSupported(), body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {scene = 1}
				)
				expect(trim(result.filecontent)).toBe(false);
			});

			it( title = "Checking new query() should return 'inserted id'", skip=isNotSupported(), body = function( currentSpec ){
				local.resultOne = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {scene = 2}
				)
				expect(trim(result.filecontent)).toBe(1);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}