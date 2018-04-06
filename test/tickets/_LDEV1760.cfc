component extends="org.lucee.cfml.test.LuceeTestCase"{
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1760", body=function() {
			it( title='checking Dialect ORM with mySQL without setting in ORM settings',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/App1/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});

			it( title='checking Dialect ORM with mySQL with dialect="MySQLwithInnoDB" in ORM settings',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1760");
				var result = _InternalRequest(
					template:"#uri#/App2/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('');
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables

		var mySQL={};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return mysql;
	}
}