<cfcomponent extends="org.lucee.cfml.test.RailoTestCase">
<cfscript>

	//public function afterTests(){}
	
	public function setUp(){
		variables.has=defineDatasource();
	}

	private string function defineDatasource(){
		var mySQL=getCredencials();
		if(mySQL.count()==0) return false;
		application action="update" 
			datasource="# {
	  class: 'org.gjt.mm.mysql.Driver'
	, bundleName:'com.mysql.jdbc'
	, bundleVersion:'5.1.38'
	, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
	, username: mySQL.username
	, password: mySQL.password
}#";
	
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
</cfscript>
	<cffunction name="test">
		<cfstoredproc procedure="spweb_ZipCodes_LookupLatLon3074" cachedwithin="#CreateTimeSpan(0,0,10,0)#">
		    <cfprocparam cfsqltype="cf_sql_varchar" value="57103">
		    <cfprocresult name="rsLatLon" resultset="1">
		</cfstoredproc>

		<cfset assertEquals("","")>
	</cffunction>
</cfcomponent>