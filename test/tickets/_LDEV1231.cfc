component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1231");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1231", function() {
			it( title='Checking static function will gives result when set output="true" via customTag/module', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe("displays before call static function/displays after call static function");
			});

			it( title='Checking static function will suppress the result when set output="false" via customTag/module', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe("displays before call static function/displays after call static function");
			});

			it( title='Checking static function will gives result when set output="true"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe("displays before call static function/displays after call static function");
			});

			it( title='Checking static function will gives result when set output="false"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe("displays before call static function/displays after call static function");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}