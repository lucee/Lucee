component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		if(hasCredentials()) {
			describe( "Testing ORMReload with multiple foreign key props to point single column", function() {
				it('Second property without insert="false" & update="false"',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals('Repeated column in mapping for entity: SupportTicket column: companyUserID (should be mapped with insert="false" update="false")',result.filecontent.trim());
				});
				it('Second property with insert="false" & update="false"',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				});
				it('Removed second property',  function( currentSpec ) {
					uri=createURI("LDEV0752/index.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=3}
					);
					assertEquals("",left(result.filecontent.trim(), 100));
				});
			});
		}
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		// getting the credentials from the enviroment variables
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			return true;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			return true;
		}
		return false;
	}
}