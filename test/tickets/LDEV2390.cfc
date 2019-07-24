component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		variables.has = defineDatasource();
	}

	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2390", function() {
			it(title = "checking if functions get lost", body = function( currentSpec ) {
				if(!variables.has) return;
				

				query name="local.q" {
					echo("SELECT '' AS foo");
				}
				var objValidate = createObject('component','LDEV2390.Test');
				loop query="q" {
					objValidate.foo();
					objValidate.test();
				}



				//expect(local.result.filecontent.trim()).toBe("true");
			});
		});
	}

	private boolean function defineDatasource(){
		var sct=getDatasource();
		if(sct.count()==0) return false;
		application action="update" datasource=sct;
		return true;
	}

	private struct function getDatasource(){
		var mySQL=getCredencials();
		if(mySQL.count()==0) return {};
		
		return {
		  type= 'mysql'
		, host=mySQL.server
		, port=mySQL.port
		, database=mySQL.database
		, username= mySQL.username
		, password= mySQL.password
	 	, custom= { useUnicode:true }
		};
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

