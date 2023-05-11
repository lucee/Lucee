component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "Checking XMLTransform()", function() {
			it('With xsl:include',  function( currentSpec ) {
				try {
					uri=createURI("LDEV0228/test.cfm");
					local.result = _InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				}
				catch(any e) {
					assertEquals("", e.Message);
				}
			});
			it('Without xsl:include',  function( currentSpec ) {
				try {
					uri=createURI("LDEV0228/test.cfm");
					local.result = _InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				}
				catch(any e) {
					assertEquals("", e.Message);
				}
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}