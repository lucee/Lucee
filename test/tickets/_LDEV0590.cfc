<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Checking existence of a variable in variables scope", function() {
				it('Calling direct function & checking its variables scope',  function( currentSpec ) {
					local.result = MakeRequest("1_1");
					expect(left(result.filecontent.trim(), 100)).notToBe("");
				});
				it("Calling direct function & checking its closure's variables scope",  function( currentSpec ) {
					local.result = MakeRequest("1_2");
					expect(left(result.filecontent.trim(), 100)).notToBe("");
				});
				it("Calling indirect function & checking its variables scope",  function( currentSpec ) {
					local.result = MakeRequest("2_1");
					expect(left(result.filecontent.trim(), 100)).notToBe("");
				});
				it("Calling indirect function & checking its closure's variables scope",  function( currentSpec ) {
					local.result = MakeRequest("2_2");
					expect(left(result.filecontent.trim(), 100)).notToBe("");
				});
			});
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}

		private any function MakeRequest(Scene){
			uri=createURI("LDEV0590/test.cfm");
			local.result = _InternalRequest(
				template:uri,
				forms:{Scene=arguments.Scene}
			);
			return local.result;
		}
	</cfscript>
</cfcomponent>