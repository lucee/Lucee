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

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		defineDatasource();
		createTable();
	}

	function afterAll() {
		if(isNotSupported()) return;
		query {
	        echo("DROP TABLE `test`");
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1292", body=function() {
			it(title="checking this.datasource, dataBase name ends with '?' ", skip=isNotSupported(), body = function( currentSpec ) {
				var uri = createURI("LDEV1292")
				var result = _InternalRequest(
					template:"#uri#/test1/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('1');
			});

			it(title="checking this.datasource, with dataBase only", skip=isNotSupported(), body = function( currentSpec ) {
				var uri = createURI("LDEV1292")
				var result = _InternalRequest(
					template:"#uri#/test2/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('1');
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

	private string function defineDatasource(){
		var mySql = getCredentials();
		application action="update" 
		datasource="#{
			class: 'org.gjt.mm.mysql.Driver'
			, bundleName:'com.mysql.jdbc'
			, bundleVersion:'5.1.38'
			, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
			, username: mySQL.username
			, password: mySQL.password
		}#";
	}

	private function createTable() {
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (`id` int)");
		}
		query {
	        echo("INSERT INTO `test` VALUES (1);");
		}
	}
}