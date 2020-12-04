component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2980");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-2980", function() {

			it( title = "Administrator.updateDatasource() without passwordEncrypted ", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#variables.uri#/LDEV2980.cfm",
					forms : {Scene = 1}
				)
				expect(trim(result.filecontent)).tobe("success");
			});


			it( title = "Administrator.updateDatasource() with passwordEncrypted ", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#variables.uri#/LDEV2980.cfm",
					forms : {Scene = 2}
				)
				expect(trim(result.filecontent)).tobe("success");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}