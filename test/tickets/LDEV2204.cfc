component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2204");
	}
	function run( testResults , testBox ) {
		describe( "Test case for LDEV2204", function() {
			it( title='StructEach is not accessing key-value accurately for arguments using deserializejson()', body=function( currentSpec ) {
			 	local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).tobe("key: number value: 2204;key: text value: testcase;");
			});
			it( title='StructEach is not accessing key-value accurately for arguments', body=function( currentSpec ) {
			 	local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).tobe("key: number value: 2204;key: text value: testcase;");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}	
}