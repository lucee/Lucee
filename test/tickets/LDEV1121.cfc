<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-1121", function() {
				it("checking object instance created by createDynamic Proxy()", function( currentSpec ) {
					var uri = createURI("LDEV1121/test.cfm");
					var result = _InternalRequest(
						template:uri
					);
					expect(result.filecontent.trim()).toBe("true-true");
				});
			});
		}
		// private function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
</cfcomponent>