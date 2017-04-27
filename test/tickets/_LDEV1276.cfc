component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1276");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1276", function() {
			describe( "checking server Mapping", function() {
				it( title='checking custom tag calling via tag', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=1}
					);
					expect(result.filecontent.trim()).toBe('<span style="color:red">custom tag used in tag</span>');
				});

				it( title='checking custom tag calling via cfscript', body=function( currentSpec ) {
					var result = _InternalRequest(
						template:"#variables.uri#/test.cfm",
						forms:{Scene=2}
					);
					expect(result.filecontent.trim()).toBe('<span style="color:red">custom tag used inside cfscript</span>');
				});
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}