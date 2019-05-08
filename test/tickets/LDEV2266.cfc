component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasource();
	}

	public void function testMySQLWithBSTTimezone(){
		if(!variables.has) return;
		
		query name="local.qry1" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}

		sleep(1500);

		query name="local.qry2" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}


		assertNotEquals(qry1.a,qry2.a);
		
	}


	private string function defineDatasource(){
		var sct=getDatasource();
		if(sct.count()==0) return false;
		application action="update" datasource=sct;
		return true;
	}

	private struct function getDatasource(){
			var mySQL=getCredencials();
			if(mySQL.count()==0) return {};
			
			return {
			  class: 'org.gjt.mm.mysql.Driver'
			, bundleName:'com.mysql.jdbc'
			, bundleVersion:'5.1.38'
			, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
			, username: mySQL.username
			, password: mySQL.password
			};
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
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
		// getting the credetials from the system variables
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