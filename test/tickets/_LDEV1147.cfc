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
			var orc={};

			if(
				!isNull(server.system.environment.ORACLE_SERVER) && 
				!isNull(server.system.environment.ORACLE_USERNAME) && 
				!isNull(server.system.environment.ORACLE_PASSWORD) && 
				!isNull(server.system.environment.ORACLE_PORT) && 
				!isNull(server.system.environment.ORACLE_DATABASE)
			) {
				// getting the credentials from the environment variables
				orc.server=server.system.environment.ORACLE_SERVER;
				orc.username=server.system.environment.ORACLE_USERNAME;
				orc.password=server.system.environment.ORACLE_PASSWORD;
				orc.port=server.system.environment.ORACLE_PORT;
				orc.database=server.system.environment.ORACLE_DATABASE;
			} else if(
				// getting the credentials from the system variables
				!isNull(server.system.properties.ORACLE_SERVER) && 
				!isNull(server.system.properties.ORACLE_USERNAME) && 
				!isNull(server.system.properties.ORACLE_PASSWORD) && 
				!isNull(server.system.properties.ORACLE_PORT) && 
				!isNull(server.system.properties.ORACLE_DATABASE)
			) {
				orc.server=server.system.properties.ORACLE_SERVER;
				orc.username=server.system.properties.ORACLE_USERNAME;
				orc.password=server.system.properties.ORACLE_PASSWORD;
				orc.port=server.system.properties.ORACLE_PORT;
				orc.database=server.system.properties.ORACLE_DATABASE;
			}
			return orc;
		}

		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>
</cfcomponent>
