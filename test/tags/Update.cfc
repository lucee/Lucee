component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		if(isNotSupported()) return;
		request.mySQL = getCredentials();
		variables.str = {
					class: 'com.mysql.cj.jdbc.Driver'
					, bundleName:'com.mysql.cj'
					, bundleVersion:'8.0.15'
					, connectionString: 'jdbc:mysql://'&request.mySQL.server&':'&request.mySQL.port&'/'&request.mySQL.database
					&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true&serverTimezone=CET'
					, username: request.mySQL.username
					, password: request.mySQL.password
					,storage:true
				}
		tableCreation();
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for CFupdate",skip=isNotSupported(), body=function() {
			it(title = "checking CFUPDATE tag", body = function( currentSpec ) {
				form.id =1; 
				form.myValue ="LuceeTestCase";
				cfupdate(tableName = "cfupdatetbl" formFields = "id,myValue" datasource=str);
				query datasource=str name="testQry"{
					echo("SELECT * FROM `cfupdatetbl`");
				}
				expect(testQry.myValue).toBe('LuceeTestCase');
			});
		});
	}


	private function tableCreation(){
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfupdatetbl`");
		}
		query datasource=str{
			echo( "
				create table `cfupdatetbl`(id varchar(10) NOT NULL PRIMARY KEY,myValue varchar(50))"
				);
		}
		query datasource=str{
			echo( "
				INSERT INTO `cfupdatetbl` values(1,'testCase')"
				);
		}
	}


	private boolean function isNotSupported() {
		var cred=getCredentials();
		return isNull(cred) || structCount(cred)==0;
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		var mySQLStruct={};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQLStruct.server=server.system.environment.MYSQL_SERVER;
			mySQLStruct.username=server.system.environment.MYSQL_USERNAME;
			mySQLStruct.password=server.system.environment.MYSQL_PASSWORD;
			mySQLStruct.port=server.system.environment.MYSQL_PORT;
			mySQLStruct.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQLStruct.server=server.system.properties.MYSQL_SERVER;
			mySQLStruct.username=server.system.properties.MYSQL_USERNAME;
			mySQLStruct.password=server.system.properties.MYSQL_PASSWORD;
			mySQLStruct.port=server.system.properties.MYSQL_PORT;
			mySQLStruct.database=server.system.properties.MYSQL_DATABASE;
		}
		/*
		mySQLStruct = {
			server:'localhost'
			, port:'3306'
			, database:'test'
			, username: 'root'
			, password: "..."
		};*/



		return mySQLStruct;
	}


	Function afterAll(){
		if(isNotSupported()) return;
		query datasource=str{
			echo("DROP TABLE IF EXISTS `cfupdatetbl`");
		}
	}
}