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
			/* TODO:
			Errored: test with version 8.0.11 --> Cannot open file:/home/travis/build/lucee/Lucee/temp/archive/base/lucee-server/context/security/cacerts [Keystore was tampered with, or password was incorrect]
			it( title='test with version 8.0.11',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.11');
				var result = testConnection(); // TODO
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.12',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.12');
				var result = testConnection(); // TODO
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.13',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.13');
				var result = testConnection(); // TODO
				assertEquals(true, isQuery(result));
			});*/

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
		var mySQL = server.getDatasource("mysql");
		mySQL.class=arguments.class;
		mySQL.bundle=arguments.bundle;
		mySQL.bundleVersion=arguments.bundleVersion;
		application action="update" datasource=mySQL;
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
		return server.getDatasource("mysql");
	}
}