component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	function isNotSupported() {
		variables.mySql = getCredentials();
		if (len(variables.mySql)){
			return false;
		} else{
			return true;
		}
	}

	function beforeAll(){
		variables.originalTZ=getTimeZone();
	}

	function afterAll(){
		setTimezone(variables.originalTZ);
	}
	
	function run( testResults , testBox ) {
		if(!hasCredentials()) return;
		describe( "Checking MYSQL JDBC drivers", function() {
			/*
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
			*/

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
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.16');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.17',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.17');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.18',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.18');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.24',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.24');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.3.0',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.3.0');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.4.0',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.4.0');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});

		describe( title="Checking MYSQL JDBC drivers with PDT timeZone", body=function() {

			beforeEach( function( currentSpec ) {
				application action="update" timezone="PDT";
				setTimeZone("PDT");
			});

			afterEach( function( currentSpec ) {
				application action="update" timezone="#variables.originalTZ#";
				setTimeZone("#variables.originalTZ#");
			});
			/*
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
			*/

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

			it( title='test with version 8.0.24',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.24');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});
	
		describe( title="Checking MYSQL JDBC drivers with CEST timeZone", body=function() {

			beforeEach( function( currentSpec ) {
				application action="update" timezone="CEST";
				setTimeZone("CEST");
			});

			afterEach( function( currentSpec ) {
				application action="update" timezone="#variables.originalTZ#";
				setTimeZone("#variables.originalTZ#");
			});
			/*
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
			*/

			it( title='test with version 8.0.15',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.15');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});

			it( title='test with version 8.0.24',skip=isNotSupported(), body=function( currentSpec ) {
				defineDatasource('com.mysql.cj.jdbc.Driver',  'com.mysql.cj', '8.0.24');
				var result = testConnection();
				assertEquals(true, isQuery(result));
			});
		});
	}

	private void function defineDatasource(class,bundle,bundleVersion){
		var mySQL = server.getDatasource("mysql");
		mySQL.class=arguments.class;
		mySQL.bundle=arguments.bundle;
		mySQL.bundleVersion=arguments.bundleVersion;
		application action="update" datasource=mySQL;

		var info = server.verifyDatasource( mySQL );
		// check via dbinfo that the bundle version matches
		expect( info ).toInclude( arguments.bundleVersion );
		//systemOutput("MySQL " & arguments.bundleVersion & ": " & server.verifyDatasource( mySQL ), true );
	}

	private query function testConnection(){
		query name="local.qry" {
			echo("SELECT now()");
		}
		return local.qry;
	}

	private boolean function hasCredentials() {
		return structCount(getCredentials());
	}

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}
}