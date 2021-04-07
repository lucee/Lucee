component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasources();
	}


	public void function testH2(){
		
		query datasource="h2" name="local.qry1" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select DATEDIFF('MILLISECOND', DATE '1970-01-01', CURRENT_TIMESTAMP()) as a"); // gives seconds since 1970
		}

		sleep(10);

		query datasource="h2" name="local.qry2" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select DATEDIFF('MILLISECOND', DATE '1970-01-01', CURRENT_TIMESTAMP()) as a"); // gives seconds since 1970
		}
		assertEquals(qry1.a,qry2.a);
		
	}

	public void function testMySQL(){
		if(!variables.has) return;
		
		query datasource="mysql" name="local.qry1" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}

		sleep(1500);

		query datasource="mysql" name="local.qry2" cachedWithin=createTimespan(0, 0, 1, 0) {
			echo("select UNIX_TIMESTAMP() as a"); // gives seconds since 1970
		}
		assertEquals(qry1.a,qry2.a);
		
	}



	private function defineDatasources() {
		var ds={};

		// H2
		var ds['h2'] = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/dbh2" );
		
		// MySQL
		var mySQL=getCredencials();
		var has=false;
		if(mySQL.count()>0) {
			var ds['mysql']={
				  class: 'org.gjt.mm.mysql.Driver'
				, bundleName:'com.mysql.jdbc'
				, bundleVersion:'5.1.38'
				, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
				, username: mySQL.username
				, password: mySQL.password
			};
			has=true;
		}

		application action="update" datasources=ds;

		return has;
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