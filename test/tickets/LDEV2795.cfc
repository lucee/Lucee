component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for sameFormFieldsAsArray LDEV-2795", body=function() {
			it( title='sameFormFieldsAsArray=true',body=function( currentSpec ) {
				var uri = createURI("LDEV2795");
				var result = _InternalRequest(
					template:"#uri#/enabled/index.cfm",
					form: "a=&b=1&a=&b=2"
				);
				expect(result.filecontent.trim()).toBe('{"B":[1,2],"A":["",""]}');
			});

			it( title='sameFormFieldsAsArray=false',body=function( currentSpec ) {
				var uri = createURI("LDEV2795");
				var result = _InternalRequest(
					template:"#uri#/disabled/index.cfm",
					form: "a=&b=1&a=&b=2"
				);
				expect(result.filecontent.trim()).toBe('{"B":"1,2","A":","}');
			});

		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
