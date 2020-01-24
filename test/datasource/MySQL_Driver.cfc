component extends="org.lucee.cfml.test.LuceeTestCase"{
	function isNotSupported() {
		variables.mySql = getCredencials();
		if(!isNull(variables.mySql)){
			return false;
		} else{
			return true;
		}

	}

	function beforeAll(){
		variables.originalTZ=getTimeZone();
	}

	
	function run( testResults , testBox ) {
		if(!hasCredencials()) return;
		describe( "Checking MYSQL JDBC drivers", function() {
			it( title='test with version 5.1.20',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.20');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.38',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.38');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.40',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.40');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 6.0.5',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.5');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			xit( title='test with version 6.0.6',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.6');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.15',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.16',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.17',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.18',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});

		describe( "Checking MYSQL JDBC drivers with PDT timeZone", function() {

			beforeEach( function( currentSpec ) {
				application action="update" timezone="PDT";
				setTimeZone("PDT");
			});

			afterEach( function( currentSpec ) {
				application action="update" timezone="#variables.originalTZ#";
				setTimeZone("#variables.originalTZ#");
			});

			it( title='test with version 5.1.20',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.20');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.38',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.38');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.40',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.40');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 6.0.5',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.5');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			xit( title='test with version 6.0.6',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.6');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.15',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.11',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.11');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.12',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.12');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.13',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.13');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.14',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.14');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.15',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});
	
		describe( "Checking MYSQL JDBC drivers with CEST timeZone", function() {

			beforeEach( function( currentSpec ) {
				application action="update" timezone="CEST";
				setTimeZone("CEST");
			});

			afterEach( function( currentSpec ) {
				application action="update" timezone="#variables.originalTZ#";
				setTimeZone("#variables.originalTZ#");
			});

			it( title='test with version 5.1.20',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.20');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.38',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.38');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 5.1.40',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.jdbc.Driver',  'com.mysql.jdbc', '5.1.40');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 6.0.5',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.5');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			xit( title='test with version 6.0.6',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '6.0.6');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.15',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});
	}

	private void function defineDatasource(class,bundle,version){
		application action="update"
			datasource={
				  class: arguments.class
				, bundleName: arguments.bundle
				, bundleVersion:arguments.version
				, connectionString: 'jdbc:mysql://'&variables.mySQL.server&':'&variables.mySQL.port&'/'&variables.mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
				, username: variables.mySQL.username
				, password: variables.mySQL.password
		};
	}

	private query function testConnection(){
		query name="local.qry" {
			echo("SELECT now()");
		}

		return local.qry;
	}


	private boolean function hasCredencials() {
		return structCount(getCredencials());
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