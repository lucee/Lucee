component extends="org.lucee.cfml.test.LuceeTestCase" skip="true"{
	function run( testResults , testBox ) {
		describe(title="Testcase for LDEV-4122", body=function() {
			it(title="checking UDF has multiple arguments with same name", body=function( currentSpec ) {
				try {
					var result = _InternalRequest(
					template:"#createURI("LDEV4122")#/LDEV4122.cfm"
					).filecontent;
				}
				catch(any e){
					var result = "must throw an error";
				}
				
				expect(trim(result)).tobe("must throw an error")
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}