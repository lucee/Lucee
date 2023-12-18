<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "thread with closure", function() {
				it('Calling closure thread page using _InternalRequest()',  function( currentSpec ) {
					uri=createURI("LDEV0578/test1.cfm");
					_InternalRequest(
						template:uri
					);
				});

				it('Calling simple thread page using _InternalRequest()',  function( currentSpec ) {
					uri=createURI("LDEV0578/test2.cfm");
					_InternalRequest(
						template:uri
					);
				});

				xit('Calling simple thread page using http',  function( currentSpec ) {
					// Please change the http url correspondingly
					http url="http://testbox.lucee.dev/test/testcases/LDEV0578/test2.cfm" result="local.res";
					writeDump(local.res);
				});
			});
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
</cfcomponent>