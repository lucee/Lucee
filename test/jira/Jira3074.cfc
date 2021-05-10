<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
<cfscript>

	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasource();
	}

	private string function defineDatasource(){
		var mySQL=getCredencials();
		if(mySQL.count()==0) return false;
		application action="update" 
			datasource="#server.getDatasource("mysql")#";
	
	return true;
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

	public void function testCachedWithin(){
		if(!variables.has) return;
		
		query {
			echo("DROP PROCEDURE IF EXISTS `proc_INOUT`");
		}
		query {
			echo("
CREATE PROCEDURE `proc_INOUT` (INOUT var1 INT)
BEGIN
    SET var1 = var1 * 2;
END
			");
		}

		storedproc procedure="proc_INOUT" cachedwithin="#CreateTimeSpan(0,0,10,0)#" {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		storedproc procedure="proc_INOUT" cachedwithin="#CreateTimeSpan(0,0,10,0)#" {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		assertEquals(20,res);
		
	}


</cfscript>
</cfcomponent>