<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		public function isNotSupported(){
			var orc = getCredencials();
			return structIsEmpty(orc);
		}

		function run(){
			describe( title="Test suite for LDEV-1147", skip=isNotSupported(), body=function(){
				it(title="Calling Package without parameters",  body=function(){
					var uri=createURI("LDEV1147/testcase.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					expect(result.filecontent.trim()).toBe('false');
				});

				it(title="Calling Package with parameters", body=function(){
					var uri=createURI("LDEV1147/testcase.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					expect(result.filecontent.trim()).toBe('false');
				});

				it(title="Calling synonym without parameters", body=function(){
					var uri=createURI("LDEV1147/testcase.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=3}
					);
					expect(result.filecontent.trim()).toBe('false');
				});

				it(title="Calling synonym with parameters", body=function(){
					var uri=createURI("LDEV1147/testcase.cfm");
					var result = _InternalRequest(
						template:uri,
						forms:{Scene=4}
					);
					expect(result.filecontent.trim()).toBe('false');
				});
			});
		}

		private struct function getCredencials() {
			return server.getDatasource("oracle");
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
</cfcomponent>
