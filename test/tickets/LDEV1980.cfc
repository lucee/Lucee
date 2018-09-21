component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}
	function run( testResults , testBox ) {


		describe( title="Test suite for LDEV-1980",skip=isNotSupported(), body=function() {
			it(title = "checking cfdbinfo without DB name", body = function( currentSpec ) {
				var ds=getDatasource();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL");
				expect(IsQuery(return_variable)).toBe('true');
				//systemOutput(return_variable,1,1);
			});

			it(title = "checking cfdbinfo with DB name",skip=isNotSupported(), body = function( currentSpec ) {
				var ds=getDatasource();
				tableCreation(ds);
				cfdbinfo(datasource=ds,name="local.pre",type= "columns",table="TestDsnTBL");
				
				cfdbinfo(datasource=ds,name="local.return_variable",type= "columns",table="TestDsnTBL",dbname=pre.TABLE_CAT);
				expect(IsQuery(return_variable)).toBe('true');
			});
		});
	}

	private function tableCreation(ds){
		query name="test" datasource=ds {
			echo("DROP TABLE IF EXISTS `TestDsnTBL`");
		}
		query name="test" datasource=ds {
			echo( "
				create table `TestDsnTBL`(id varchar(10),Personname varchar(10))"
				);
		}
	}

	function afterAll(){
		if(isNotSupported()) return;
		query name="test" datasource=getDatasource() {
			echo( "
					DROP DATABASE IF EXISTS `LDEV1980DB` 
				");
		}
	}

	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql) && structCount(mySql)){
			return false;
		} else{
			return true;
		}
	}

	private function getDatasource() {
		var cred=getCredentials();
		if(structCount(cred)>0){
			return {
				class: 'com.mysql.cj.jdbc.Driver'
				, bundleName:'com.mysql.cj'
				, connectionString: 'jdbc:mysql://'&
					cred.server&':'&
					cred.port&'/'&
					cred.database&'?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GB&useLegacyDatetimeCode=true'
				, username: cred.username
				, password: cred.password
				,storage:true
			};
		}
		return {};
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

		mySQLStruct.server="localhost";
		mySQLStruct.username="root";
		mySQLStruct.password="encrypted:aa88d4e79d84789e13ae74313d9bd97a7eacb5d15a1d5d07";
		mySQLStruct.port="3306";
		mySQLStruct.database="test";


		return mySQLStruct;
	}
}